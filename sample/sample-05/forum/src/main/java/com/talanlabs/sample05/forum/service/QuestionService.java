package com.talanlabs.sample05.forum.service;

import com.talanlabs.sample05.forum.model.CreateQuestionCommand;
import com.talanlabs.sample05.forum.model.Question;
import com.talanlabs.sample05.forum.model.SearchQuestionRequest;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;

import java.util.List;


@ProxyGen
@VertxGen
public interface QuestionService {

    @GenIgnore
    String ADDRESS = "com.talanlabs.sample03.questions.service";


    void list(SearchQuestionRequest request, Handler<AsyncResult<List<Question>>> resultHandler);
    void getById(String uuid, Handler<AsyncResult<Question>> resultHandler);
    void delete(String uuid, Handler<AsyncResult<Void>> resultHandler);
    void create(CreateQuestionCommand command, Handler<AsyncResult<Question>> resultHandler);

    @GenIgnore
    static QuestionService createProxy(Vertx vertx, String address) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(address);
        return builder.build(QuestionService.class);
    }

    @GenIgnore
    static QuestionService createService(Vertx vertx, JsonObject config) {
        return new QuestionServiceImpl(vertx, config);
    }
}
