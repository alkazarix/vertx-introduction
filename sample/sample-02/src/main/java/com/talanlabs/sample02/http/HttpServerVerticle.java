package com.talanlabs.sample02.http;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.talanlabs.sample02.core.ApplicationConfig;
import com.talanlabs.sample02.core.CreateQuestionCommand;
import com.talanlabs.sample02.core.ErrorCode;
import com.talanlabs.sample02.core.SearchQuestionRequest;
import com.talanlabs.sample02.database.QuestionDatabaseService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.ext.web.api.*;
import io.vertx.ext.web.api.validation.*;
import io.vertx.ext.web.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class HttpServerVerticle extends AbstractVerticle {

    private static Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    private static final Integer HTTP_SERVER_PORT = 9000;

    private static final String SWAGGER_ENDPOINT = "/doc/*";
    private static final String GET_QUESTIONS_ENDPOINT = "/questions";
    private static final String GET_QUESTION_BY_UID_ENDPOINT = "/questions/:questionid";
    private static final String DELETE_QUESTION_ENDPOINT = "/questions/:questionid";
    private static final String CREATE_QUESTION_ENDPOINT = "/questions";

    private final QuestionDatabaseService service;
    private String createQuestionValidationSchema;

    public HttpServerVerticle(QuestionDatabaseService service) {
        this.service = service;
    }


    @Override
    public void start(Promise<Void> promise) throws Exception {

        loadValidationSchema();

        Integer listenPort = config().getInteger(ApplicationConfig.LISTEN_PORT_PROPERTY, HTTP_SERVER_PORT);
        logger.info("start http verticle");
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

        router.get(GET_QUESTION_BY_UID_ENDPOINT)
                .handler(this.questionUIDValidationHandler())
                .handler(this::getQuestionHandler);

        router.delete(DELETE_QUESTION_ENDPOINT)
                .handler(this.questionUIDValidationHandler())
                .handler(this::deleteQuestionHandler);

        router.post(CREATE_QUESTION_ENDPOINT)
                .handler(this.createQuestionValidationHandler())
                .handler(this::createQuestionHandler);


        router.route().handler(ctx -> ctx.fail(404, new RuntimeException("resource not found")));

        server
                .requestHandler(router)
                .listen(listenPort, ar -> {
                    if (ar.succeeded()) {
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
        this.service.getByID(UUID.fromString(questionId), ar -> {
            if (ar.succeeded()) {
                try {
                    context.response().end(QuestionResponse.fromDTO(ar.result()).asEmbedded().serialize());
                } catch (JsonProcessingException e) {
                    context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getCause());
                }
            } else if (ar.cause() instanceof ReplyException &&  ((ReplyException) ar.cause()).failureCode() == ErrorCode.QUESTION_NOT_FOUND.getCode()) {
                context.fail(HttpResponseStatus.NOT_FOUND.code(), new RuntimeException("question do not exist"));
            } else {
                context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ar.cause());
            }
        });
    }

    private void deleteQuestionHandler(RoutingContext context) {
        String questionId = context.request().getParam("questionid");
        this.service.delete(UUID.fromString(questionId), ar -> {
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

        final String message = (context.get("message") != null) ? context.get("message") : (context.failure() != null) ? context.failure().getMessage() : null;
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
