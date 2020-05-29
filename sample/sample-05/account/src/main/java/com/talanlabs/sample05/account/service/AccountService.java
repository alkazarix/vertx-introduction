package com.talanlabs.sample05.account.service;

import com.talanlabs.sample05.account.model.UserAccount;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;

import java.util.List;

@VertxGen
@ProxyGen
public interface AccountService {

    @GenIgnore
    String ADDRESS = "com.talanlabs.sample05.account.service";

    @GenIgnore
    String NAME = "com.talanlabs.account.service";

    void getById(String uuid, Handler<AsyncResult<UserAccount>> resultHandler);

    @GenIgnore
    static AccountService createProxy(Vertx vertx, String address) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(address);
        return builder.build(AccountService.class);
    }

    @GenIgnore
    static AccountService createService(Vertx vertx, JsonObject config) {
        return new AccountServiceImpl(vertx, config);
    }
}
