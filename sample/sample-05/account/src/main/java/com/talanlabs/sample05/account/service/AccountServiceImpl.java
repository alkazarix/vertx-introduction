package com.talanlabs.sample05.account.service;

import com.talanlabs.sample05.account.config.ApplicationConfig;
import com.talanlabs.sample05.account.model.ErrorCode;
import com.talanlabs.sample05.account.model.UserAccount;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountServiceImpl implements AccountService {

    private static Logger logger = LoggerFactory.getLogger(AccountService.class);

    private static final String GET_BY_ID_SQL = "select uuid, username, email, phone FROM account WHERE uuid=?";

    JDBCClient client;

    public AccountServiceImpl(Vertx vertx, JsonObject config) {
        JsonObject sqlConfig = new JsonObject()
                .put("url", config.getString(ApplicationConfig.JDBC_URL_PROPERTY))
                .put("driver_class", config.getString(ApplicationConfig.JDBC_DRIVER_PROPERTY))
                .put("user", config.getString(ApplicationConfig.JDBC_USERNAME_PROPERTY))
                .put("password", config.getString(ApplicationConfig.JDBC_PASSWORD_PROPERTY))
                .put("max_poll_size", 20);

        this.client = JDBCClient.createShared(vertx, sqlConfig);
    }

    @Override
    public void getById(String uuid, Handler<AsyncResult<UserAccount>> resultHandler) {

        this.client.queryWithParams(GET_BY_ID_SQL, new JsonArray().add(uuid), ar -> {
            if (ar.succeeded() && ar.result().getNumRows() > 0) {
                resultHandler.handle(Future.succeededFuture(rowToQuestion(ar.result().getResults().get(0))));
            }
            else if (ar.succeeded() && ar.result().getNumRows() == 0) {
                resultHandler.handle(Future.failedFuture(new ServiceException(ErrorCode.ACCOUNT_NOT_FOUND.getCode(), "user not found")));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });

    }

    private UserAccount rowToQuestion(JsonArray row) {
        return new UserAccount()
                .withUUID(row.getString(0))
                .withUsername(row.getString(1))
                .withEmail(row.getString(2))
                .withPhone(row.getString(3));
    }
}
