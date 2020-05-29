package com.talanlabs.sample05.forum.http;


import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.talanlabs.sample05.core.BaseVerticle;
import com.talanlabs.sample05.forum.config.ApplicationConfig;
import com.talanlabs.sample05.forum.model.*;
import com.talanlabs.sample05.forum.service.QuestionService;
import io.netty.handler.codec.http.HttpResponseStatus;

import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.ext.web.api.*;
import io.vertx.ext.web.api.validation.*;
import io.vertx.ext.web.handler.*;
import io.vertx.serviceproxy.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.stream.Collectors;

public class ForumApiVerticle extends BaseVerticle {

    private static Logger logger = LoggerFactory.getLogger(ForumApiVerticle.class);

    private static final String API_NAME = "com.talanlabs.forum.api";

    private static final Integer HTTP_SERVER_PORT = 9000;
    private static final String HTTP_PUBLIC_URL = "localhost";

    private static final String SWAGGER_ENDPOINT = "/doc/*";
    private static final String GET_QUESTIONS_ENDPOINT = "/questions";
    private static final String GET_QUESTION_BY_ID_ENDPOINT = "/questions/:questionid";
    private static final String DELETE_QUESTION_ENDPOINT = "/questions/:questionid";
    private static final String CREATE_QUESTION_ENDPOINT = "/questions";

    private QuestionService service;
    private String createQuestionValidationSchema;





    public ForumApiVerticle(QuestionService service) {
        this.service = service;
    }


    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();
        logger.info("start http verticle");

        loadValidationSchema();

        Integer port = config().getInteger("http.port", HTTP_SERVER_PORT);
        String  url  = config().getString("http.public.url", HTTP_PUBLIC_URL);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route()
                .handler(BodyHandler.create().setMergeFormAttributes(true))
                .handler(LoggerHandler.create())
                .handler(TimeoutHandler.create(5000))
                .handler(ctx -> {
                    ctx.response()
                        .putHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
                        .putHeader("Pragma", "no-cache")
                        .putHeader("Expires", "0")
                        .putHeader("Content-Type", "application/json; charset=utf-8");
                    ctx.next();
                })
                .failureHandler(this::failureHandler);

        router.route(SWAGGER_ENDPOINT)
                .handler(StaticHandler.create("webroot/swagger"));

        router.get(GET_QUESTIONS_ENDPOINT)
                .handler(this.searchQuestionValidationHandler())
                .handler(this::searchQuestionHandler);

        router.get(GET_QUESTION_BY_ID_ENDPOINT)
                .handler(this.questionUIDValidationHandler())
                .handler(this::getQuestionHandler);


        router.delete(DELETE_QUESTION_ENDPOINT)
                .handler(this.questionUIDValidationHandler())
                .handler(this::deleteQuestionHandler);

        router.post(CREATE_QUESTION_ENDPOINT)
                .handler(this.createQuestionValidationHandler())
                .handler(this::createQuestionHandler);


        router.route().handler(ctx -> ctx.fail(HttpResponseStatus.NOT_FOUND.code(), new RuntimeException("NOT FOUND")));

        server
                .requestHandler(router)
                .listen(config().getInteger(ApplicationConfig.LISTEN_PORT_PROPERTY,HTTP_SERVER_PORT), ar -> {
                    if (ar.succeeded()) {
                        publishHttpEndpoint(API_NAME, url, port)
                                .onSuccess(r -> {
                                    promise.complete();
                                })
                                .onFailure(er -> {
                                    promise.fail(er.getCause());
                                });
                        promise.complete();
                    } else {
                        promise.fail(ar.cause());
                    }
                });
    }



    private void searchQuestionHandler(RoutingContext context) {

        RequestParameters params = context.get("parsedParameters");
        logger.info("search book handler params" + params.queryParametersNames().toString());
        SearchQuestionRequest request = this.bind(params);

        this.service.list(request, ar -> {
            if (ar.succeeded()) {

                try {
                    Collection<QuestionResponse> results = ar.result().stream().map(QuestionResponse::fromDTO).collect(Collectors.toList());
                    HalRepresentation hal = HalRepresentation.paginated(
                            "questions",
                            context.request().absoluteURI(),
                            results.stream(),
                            request.getPage(),
                            request.getLimit()
                    ).build();

                    context.response().end(hal.serialize());

                } catch (URISyntaxException | JsonProcessingException e) {
                    context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e);
                }

            } else {
                context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ar.cause());
            }
        });
    }

    private void getQuestionHandler(RoutingContext context) {
        String questionId = context.request().getParam("questionid");
        this.service.getById(questionId, ar -> {
            if (ar.succeeded()) {
                try {

                    context.response().end(QuestionResponse.fromDTO(ar.result()).asEmbedded().serialize());
                } catch (JsonProcessingException e) {
                    context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getCause());
                }
            } else if (ar.cause() instanceof ServiceException &&  ((ServiceException) ar.cause()).failureCode() == ErrorCode.QUESTION_NOT_FOUND.getCode()) {
                context.fail(HttpResponseStatus.NOT_FOUND.code(), new RuntimeException("question do not exist"));
            } else {
                context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ar.cause());
            }
        });
    }

    private void deleteQuestionHandler(RoutingContext context) {
        String questionId = context.request().getParam("questionid");
        this.service.delete(questionId, ar -> {
            if (ar.succeeded()) {
                context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
            } else {
                context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ar.cause());
            }
        });
    }

    private void createQuestionHandler(RoutingContext context) {
        CreateQuestionCommand command = new CreateQuestionCommand(context.getBodyAsJson());
        this.service.create(command, ar -> {
            if (ar.succeeded()) {
                try {
                    context.response()
                            .setStatusCode(HttpResponseStatus.CREATED.code())
                            .end(QuestionResponse.fromDTO(ar.result()).asEmbedded().serialize());
                } catch (JsonProcessingException e) {
                    context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getCause());
                }
            } else {
                context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ar.cause());
            }
        });
    }


    private HTTPRequestValidationHandler searchQuestionValidationHandler() {
        return HTTPRequestValidationHandler.create()
                .addQueryParam(SearchQuestionParameter.LIMIT.getName(), ParameterType.INT, false)
                .addQueryParam(SearchQuestionParameter.PAGE.getName(), ParameterType.INT, false);
    }

    private HTTPRequestValidationHandler questionUIDValidationHandler() {
        return HTTPRequestValidationHandler.create()
                .addPathParam("questionid", ParameterType.UUID);
    }

    private HTTPRequestValidationHandler createQuestionValidationHandler() {
        return  HTTPRequestValidationHandler.create()
                .addJsonBodySchema(createQuestionValidationSchema);
    }

    private void loadValidationSchema() throws IOException {

        InputStream input = getClass().getResourceAsStream("/create-question-schema.json");
        InputStreamReader is = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(is);
        StringBuffer sb = new StringBuffer();
        String str;
        while((str = reader.readLine())!= null){
            sb.append(str);
        }

        this.createQuestionValidationSchema = sb.toString();
    }



    private void failureHandler(RoutingContext context ) {

        final JsonObject json = new JsonObject()
                .put("timestamp", System.nanoTime())
                .put("status", context.statusCode())
                .put("error", HttpResponseStatus.valueOf(context.statusCode()).reasonPhrase())
                .put("path", context.request().path());

        final String message = (context.get("message") != null) ? context.get("message") : (context.failure() != null) ? context.failure().getMessage() : "";
        json.put("message", message);


        context.response()
                .putHeader(HttpHeaders.CONTENT_TYPE,"application/json; charset=utf-8")
                .setStatusCode(context.statusCode())
                .end(json.encodePrettily());
    }

    public SearchQuestionRequest bind(RequestParameters parameters) {
        SearchQuestionRequest request  = new SearchQuestionRequest();

        if (parameters.queryParametersNames().contains(SearchQuestionParameter.LIMIT.getName()))
            request.withLimit(parameters.queryParameter(SearchQuestionParameter.LIMIT.getName()).getInteger());

        if (parameters.queryParametersNames().contains(SearchQuestionParameter.PAGE.getName()))
            request.withPage(parameters.queryParameter(SearchQuestionParameter.PAGE.getName()).getInteger());

        return request;
    }



}
