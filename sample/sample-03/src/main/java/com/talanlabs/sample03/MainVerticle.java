package com.talanlabs.sample03;

import com.talanlabs.sample03.core.ApplicationConfig;
import com.talanlabs.sample03.database.QuestionDatabaseService;
import com.talanlabs.sample03.http.HttpServerVerticle;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  private static final Integer MAX_INSTANCES = 1;

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {


    ApplicationConfig.create().get(vertx, cr -> {
      if (cr.succeeded()) {
        QuestionDatabaseService service = QuestionDatabaseService.createService(vertx, cr.result());
        ServiceBinder serviceBinder = new ServiceBinder(vertx);

        serviceBinder
                .setAddress(QuestionDatabaseService.ADDRESS)
                .register(QuestionDatabaseService.class, service);

        HttpServerVerticle httpServerVerticle = new HttpServerVerticle(QuestionDatabaseService.createProxy(vertx, QuestionDatabaseService.ADDRESS));

        deployVerticle(httpServerVerticle, cr.result()).setHandler(ar -> {
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
