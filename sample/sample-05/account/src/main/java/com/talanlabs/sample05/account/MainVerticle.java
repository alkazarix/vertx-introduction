package com.talanlabs.sample05.account;

import com.talanlabs.sample05.account.http.AccountApiVerticle;
import com.talanlabs.sample05.account.service.AccountService;
import com.talanlabs.sample05.forum.core.BaseVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.serviceproxy.ServiceBinder;

public class MainVerticle extends BaseVerticle {
    private static final Integer MAX_INSTANCES = 1;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        AccountService serviceProxy = AccountService.createProxy(vertx, AccountService.ADDRESS);
        AccountService service = AccountService.createService(vertx, config());

        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress(AccountService.ADDRESS).register(AccountService.class, service);


        publishEventBusService(AccountService.NAME, AccountService.ADDRESS, AccountService.class)
                .compose(r -> {
                    AccountApiVerticle api = new AccountApiVerticle(serviceProxy);
                    return deployVerticle(api);
                })
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        startPromise.complete();
                    } else {
                        startPromise.fail(ar.cause());
                    }
                });

    }

    private Future<Void> deployVerticle(AbstractVerticle verticle) {
        Promise<Void> promise = Promise.promise();
        vertx.deployVerticle(verticle, new DeploymentOptions().setInstances(MAX_INSTANCES).setConfig(config()), ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }


}
