package com.talanlabs.sample05.forum;

import com.talanlabs.sample05.core.BaseVerticle;
import com.talanlabs.sample05.forum.config.ApplicationConfig;
import com.talanlabs.sample05.forum.http.ForumApiVerticle;
import com.talanlabs.sample05.forum.service.QuestionService;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends BaseVerticle {

  private static final Integer MAX_INSTANCES = 1;

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {


    ApplicationConfig.create().get(vertx, cr -> {
      if (cr.succeeded()) {
        QuestionService service = QuestionService.createService(vertx, cr.result());
        ServiceBinder serviceBinder = new ServiceBinder(vertx);

        serviceBinder
                .setAddress(QuestionService.ADDRESS)
                .register(QuestionService.class, service);

        ForumApiVerticle forumApiVerticle = new ForumApiVerticle(QuestionService.createProxy(vertx, QuestionService.ADDRESS));

        deployVerticle(forumApiVerticle, cr.result()).setHandler(ar -> {
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
