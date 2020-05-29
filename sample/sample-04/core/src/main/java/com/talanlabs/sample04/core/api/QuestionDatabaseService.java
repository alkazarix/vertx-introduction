package com.talanlabs.sample04.core.api;

import com.talanlabs.sample04.core.models.*;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.UUID;

public interface QuestionDatabaseService {

    String LIST_ACTION_UID = "database.service.fetch";
    String GET_BY_ID_ACTION_UID = "database.service.get";
    String DELETE_ACTION_UID = "question.service.delete";
    String CREATE_ACTION_UID = "question.service.create";

    void list(SearchQuestionRequest request, Handler<AsyncResult<List<QuestionDTO>>> resultHandler);
    void getById(UUID uuid, Handler<AsyncResult<QuestionDTO>> resultHandler);
    void delete(UUID uuid, Handler<AsyncResult<Void>> resultHandler);
    void create(CreateQuestionCommand command, Handler<AsyncResult<QuestionDTO>> resultHandler);

}
