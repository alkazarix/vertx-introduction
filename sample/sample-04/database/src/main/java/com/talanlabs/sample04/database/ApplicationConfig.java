package com.talanlabs.sample04.database;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ApplicationConfig {


    public static final String JDBC_URL_PROPERTY = "jdbc.url";
    public static final String JDBC_DRIVER_PROPERTY = "jdbc.driver";
    public static final String JDBC_PASSWORD_PROPERTY = "jdbc.password";
    public static final String JDBC_USERNAME_PROPERTY = "jdbc.username";

    private static final String JDBC_URL_ENVIRONMENT = "JDBC_URL";
    private static final String JDBC_DRIVER_ENVIRONMENT = "JDBC_DRIVER";
    private static final String JDBC_PASSWORD_ENVIRONMENT = "JDBC_PASSWORD";
    private static final String JDBC_USERNAME_ENVIRONMENT = "JDBC_USERNAME";


    public static ApplicationConfig create() {
        return new ApplicationConfig();
    }


    private Map<String, String> envMapping = new HashMap<String, String>();

    private ApplicationConfig() {
        envMapping.put(JDBC_URL_ENVIRONMENT, JDBC_URL_PROPERTY);
        envMapping.put(JDBC_DRIVER_ENVIRONMENT, JDBC_DRIVER_PROPERTY);
        envMapping.put(JDBC_USERNAME_ENVIRONMENT, JDBC_USERNAME_PROPERTY);
        envMapping.put(JDBC_PASSWORD_ENVIRONMENT, JDBC_PASSWORD_PROPERTY);
    }


    public void get(Vertx vertx, Handler<AsyncResult<JsonObject>> handler) {
        retriever(vertx).getConfig(cr -> {
            if (cr.succeeded()) {
                envMapping.keySet().stream().forEach(key -> {
                    if(cr.result().containsKey(key))
                        cr.result().put(envMapping.get(key), cr.result().getString(key));
                });
                handler.handle(Future.succeededFuture(cr.result()));
            } else {
                handler.handle(Future.failedFuture(cr.cause()));
            }
        });
    }


    private ConfigRetriever retriever(Vertx vertx) {
        ConfigStoreOptions file = new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setConfig(new JsonObject().put("path", "conf/config.json"))
                .setOptional(true);

        ConfigStoreOptions env = new ConfigStoreOptions()
                .setType("env")
                .setConfig(new JsonObject().put("keys", new JsonArray()
                        .add(JDBC_URL_ENVIRONMENT)
                        .add(JDBC_DRIVER_ENVIRONMENT)
                        .add(JDBC_PASSWORD_ENVIRONMENT)
                        .add(JDBC_USERNAME_ENVIRONMENT)));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(file).addStore(env);

        return ConfigRetriever.create(vertx, options);

    }
}
