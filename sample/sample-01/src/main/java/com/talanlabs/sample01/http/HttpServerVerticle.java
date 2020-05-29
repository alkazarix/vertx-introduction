package com.talanlabs.sample01.http;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.talanlabs.sample01.core.QuestionDTO;
import com.talanlabs.sample01.core.ApplicationConfig;
import com.talanlabs.sample01.core.SearchQuestionRequest;
import com.talanlabs.sample01.database.QuestionDatabaseVerticle;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.ext.web.api.*;
import io.vertx.ext.web.api.validation.*;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HttpServerVerticle extends AbstractVerticle {

    private static Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    private static final Integer HTTP_SERVER_PORT = 9000;

    private static String SWAGGER_ENDPOINT = "/doc/*";
    private static String GET_QUESTIONS_ENDPOINT = "/questions";


    @Override
    public void start(Promise<Void> promise) throws Exception {

        logger.info("start http verticle");
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route()
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
                .handler(this::searchBookHandler);

        router.route().handler(ctx -> ctx.fail(404));

        server
                .requestHandler(router)
                .listen(config().getInteger(ApplicationConfig.LISTEN_PORT_PROPERTY, HTTP_SERVER_PORT), ar -> {
                    if (ar.succeeded()) {
                        promise.complete();
                    } else {
                        promise.fail(ar.cause());
                    }
                });
    }



    private void searchBookHandler(RoutingContext context) {

        RequestParameters params = context.get("parsedParameters");
        SearchQuestionRequest query = this.bind(params);

        logger.info("search book handler query ", query);
        getVertx().eventBus().request(QuestionDatabaseVerticle.ADDRESS, query.toJson(), ar -> {
            if (ar.succeeded()) {
                List<QuestionDTO> questions = new JsonArray(ar.result().body().toString())
                        .stream()
                        .map(item -> (JsonObject) item)
                        .map(QuestionDTO::new)
                        .collect(Collectors.toList());

                Collection<QuestionResult> results = questions.stream().map(QuestionResult::fromDTO).collect(Collectors.toList());

                try {
                    HalRepresentation hal = HalRepresentation.paginated(
                            "questions",
                            context.request().absoluteURI(),
                            results.stream(),
                            query.getPage(),
                            query.getLimit()
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

    private HTTPRequestValidationHandler searchQuestionValidationHandler() {
        return HTTPRequestValidationHandler.create()
                .addQueryParam(SearchQuestionParameter.LIMIT.getName(), ParameterType.INT, false)
                .addQueryParam(SearchQuestionParameter.PAGE.getName(), ParameterType.INT, false);
    }

    private void failureHandler(RoutingContext context ) {

        final JsonObject json = new JsonObject()
                .put("timestamp", System.nanoTime())
                .put("status", context.statusCode())
                .put("error", HttpResponseStatus.valueOf(context.statusCode()).reasonPhrase())
                .put("path", context.request().path());

        final String message = (context.get("message") != null) ? context.get("message") : context.failure().getMessage();
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
