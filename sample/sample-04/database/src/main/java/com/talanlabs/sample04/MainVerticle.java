package com.talanlabs.sample04;

import com.talanlabs.sample04.core.api.QuestionDatabaseService;
import com.talanlabs.sample04.database.ApplicationConfig;
import com.talanlabs.sample04.database.QuestionDatabaseServiceImpl;
import com.talanlabs.sample04.database.QuestionDatabaseVerticle;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

public class MainVerticle extends AbstractVerticle {
    private static final Integer MAX_INSTANCES = 1;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        ApplicationConfig.create().get(vertx, cr -> {
            if (cr.succeeded()) {
                QuestionDatabaseService service = new QuestionDatabaseServiceImpl(vertx, cr.result());
                QuestionDatabaseVerticle verticle = new QuestionDatabaseVerticle(service);

                deployVerticle(verticle, cr.result()).setHandler(ar -> {
                    if (ar.succeeded()) {
                        startPromise.complete();
                    } else {
                        startPromise.fail(ar.cause());
                    }
                });
            } else {
                startPromise.fail(cr.cause());
            }
        });
    }

    private Future<Void> deployVerticle(AbstractVerticle verticle, JsonObject config) {
        Promise<Void> promise = Promise.promise();
        vertx.deployVerticle(verticle, new DeploymentOptions().setInstances(MAX_INSTANCES).setConfig(config), ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();

    }
}
