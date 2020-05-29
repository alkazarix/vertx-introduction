package com.talanlabs.sample02.database;

import com.talanlabs.sample02.core.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.UUID;

public interface QuestionDatabaseService {

    String LIST_ACTION_UID = "question.service.list";
    String GET_BY_UID_ACTION_UID = "question.service.get";
    String DELETE_ACTION_UID = "question.service.delete";
    String CREATE_ACTION_UID = "question.service.create";

    void list(SearchQuestionRequest request, Handler<AsyncResult<List<QuestionDTO>>> resultHandler);
    void getByID(UUID uuid, Handler<AsyncResult<QuestionDTO>> resultHandler);
    void delete(UUID uuid, Handler<AsyncResult<Void>> resultHandler);
    void create(CreateQuestionCommand command, Handler<AsyncResult<QuestionDTO>> resultHandler);

    static QuestionDatabaseService createProxy(Vertx vertx) {
        return new QuestionDatabaseServiceProxy(vertx);
    }

    static QuestionDatabaseService createService(Vertx vertx, JsonObject config) {
        return new QuestionDatabaseServiceImpl(vertx, config);
    }
}
