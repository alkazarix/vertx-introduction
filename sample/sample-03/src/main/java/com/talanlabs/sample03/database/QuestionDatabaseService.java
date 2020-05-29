package com.talanlabs.sample03.database;

import com.talanlabs.sample03.core.CreateQuestionCommand;
import com.talanlabs.sample03.core.QuestionDTO;
import com.talanlabs.sample03.core.SearchQuestionRequest;
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
public interface QuestionDatabaseService {

    @GenIgnore
    String ADDRESS = "com.talanlabs.sample03.questions.service";


    void list(SearchQuestionRequest request, Handler<AsyncResult<List<QuestionDTO>>> resultHandler);
    void getById(String uuid, Handler<AsyncResult<QuestionDTO>> resultHandler);
    void delete(String uuid, Handler<AsyncResult<Void>> resultHandler);
    void create(CreateQuestionCommand command, Handler<AsyncResult<QuestionDTO>> resultHandler);

    @GenIgnore
    static QuestionDatabaseService createProxy(Vertx vertx, String address) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(address);
        return builder.build(QuestionDatabaseService.class);
    }

    @GenIgnore
    static QuestionDatabaseService createService(Vertx vertx, JsonObject config) {
        return new QuestionDatabaseServiceImpl(vertx, config);
    }
}
