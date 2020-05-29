package com.talanlabs.sample04;

import com.talanlabs.sample04.core.api.QuestionDatabaseService;
import com.talanlabs.sample04.http.HttpServerVerticle;
import com.talanlabs.sample04.service.QuestionDatabaseServiceProxy;
import io.vertx.core.*;

public class MainVerticle extends AbstractVerticle {
    private static final Integer MAX_INSTANCES = 1;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        QuestionDatabaseService serviceProxy = new QuestionDatabaseServiceProxy(vertx);
        HttpServerVerticle httpServerVerticle = new HttpServerVerticle(serviceProxy);

        deployVerticle(httpServerVerticle).setHandler(ar -> {
            if (ar.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(ar.cause());
            }
        });
    }

    private Future<Void> deployVerticle(AbstractVerticle verticle) {
        Promise<Void> promise = Promise.promise();
        vertx.deployVerticle(verticle, new DeploymentOptions().setInstances(MAX_INSTANCES), ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }
}
