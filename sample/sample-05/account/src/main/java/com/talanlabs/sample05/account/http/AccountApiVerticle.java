package com.talanlabs.sample05.account.http;

import com.talanlabs.sample05.account.config.ApplicationConfig;
import com.talanlabs.sample05.account.model.ErrorCode;
import com.talanlabs.sample05.account.service.AccountService;
import com.talanlabs.sample05.forum.core.BaseVerticle;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.api.validation.ParameterType;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import io.vertx.serviceproxy.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountApiVerticle extends BaseVerticle {

    private static Logger logger = LoggerFactory.getLogger(AccountApiVerticle.class);

    private static final String API_NAME = "com.talanlabs.account.api";

    private static final Integer HTTP_SERVER_PORT = 9001;
    private static final String HTTP_PUBLIC_URL = "localhost";

    private static final String GET_ACCOUNT_ENDPOINT = "/account/:accountId";

    private AccountService service;

    public AccountApiVerticle(AccountService service) {
        this.service = service;
    }

    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();

        String url = config().getString(ApplicationConfig.PUBLIC_URL_PROPERTY, HTTP_PUBLIC_URL);
        Integer port = config().getInteger(ApplicationConfig.LISTEN_PORT_PROPERTY, HTTP_SERVER_PORT);

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

        router.get(GET_ACCOUNT_ENDPOINT)
                .handler(this.accountUUIDValidationHandler())
                .handler(this::getAccountHandler);

        server.listen(port, ar -> {
            if (ar.succeeded()) {
                publishHttpEndpoint(API_NAME, url, port)
                        .onSuccess(r -> promise.complete())
                        .onFailure(r -> promise.fail(r.getCause()));
            } else {
                promise.fail(ar.cause());
            }
        });
    }

    private void getAccountHandler(RoutingContext context) {
        String questionId = context.request().getParam("accountId");
        this.service.getById(questionId, ar -> {
            if (ar.succeeded()) {
                context.response().end(ar.result().toJson().encode());
            } else if (ar.cause() instanceof ServiceException && ((ServiceException) ar.cause()).failureCode() == ErrorCode.ACCOUNT_NOT_FOUND.getCode()) {
                context.fail(HttpResponseStatus.NOT_FOUND.code(), new RuntimeException("account do not exist"));
            } else {
                context.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ar.cause());
            }
        });
    }


    private HTTPRequestValidationHandler accountUUIDValidationHandler() {
        return HTTPRequestValidationHandler.create()
                .addPathParam("accountId", ParameterType.UUID);
    }

    private void failureHandler(RoutingContext context) {

        final JsonObject json = new JsonObject()
                .put("timestamp", System.nanoTime())
                .put("status", context.statusCode())
                .put("error", HttpResponseStatus.valueOf(context.statusCode()).reasonPhrase())
                .put("path", context.request().path());

        final String message = (context.get("message") != null) ? context.get("message") : (context.failure() != null) ? context.failure().getMessage() : "";
        json.put("message", message);


        context.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .setStatusCode(context.statusCode())
                .end(json.encodePrettily());
    }
}
