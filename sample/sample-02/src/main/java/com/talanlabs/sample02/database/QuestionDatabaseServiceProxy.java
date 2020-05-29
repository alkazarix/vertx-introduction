package com.talanlabs.sample02.database;

import com.talanlabs.sample02.core.CreateQuestionCommand;
import com.talanlabs.sample02.core.QuestionDTO;
import com.talanlabs.sample02.core.SearchQuestionRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestionDatabaseServiceProxy implements QuestionDatabaseService {

    public static final String HEADER_ACTION_NAME = "action";
    public static final String MESSAGE_QUERY_PROPERTY = "_query";

    private final String address = QuestionDatabaseVerticle.ADDRESS;
    private final Vertx vertx;


    public QuestionDatabaseServiceProxy(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void list(SearchQuestionRequest request, Handler<AsyncResult<List<QuestionDTO>>> resultHandler) {
        DeliveryOptions option = new DeliveryOptions()
                .addHeader(HEADER_ACTION_NAME, QuestionDatabaseService.LIST_ACTION_UID);

        JsonObject json = new JsonObject().put(MESSAGE_QUERY_PROPERTY,  request.toJson());

        this.vertx.eventBus().<JsonArray>request(address, json,  option, ar -> {
                            if (ar.succeeded()) {
                                List<QuestionDTO> questions = ar.result().body()
                                        .stream()
                                        .map(o -> (o == null) ?  null :  new QuestionDTO((JsonObject) o))
                                        .collect(Collectors.toList());
                                resultHandler.handle(Future.succeededFuture(questions));
                            } else {
                                resultHandler.handle(Future.failedFuture(ar.cause()));
                            }
                        });
    }

    @Override
    public void getByID(UUID uuid, Handler<AsyncResult<QuestionDTO>> resultHandler) {

        DeliveryOptions option = new DeliveryOptions()
                .addHeader(HEADER_ACTION_NAME, QuestionDatabaseService.GET_BY_UID_ACTION_UID);

        JsonObject json = new JsonObject().put(MESSAGE_QUERY_PROPERTY, uuid.toString());

        this.vertx.eventBus().<JsonObject>request(address, json,  option, ar -> {
            if (ar.succeeded()) {
                QuestionDTO question = new QuestionDTO(ar.result().body());
                resultHandler.handle(Future.succeededFuture(question));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void delete(UUID uuid, Handler<AsyncResult<Void>> resultHandler) {
        DeliveryOptions option = new DeliveryOptions()
                .addHeader(HEADER_ACTION_NAME, QuestionDatabaseService.DELETE_ACTION_UID);

        JsonObject json = new JsonObject().put(MESSAGE_QUERY_PROPERTY, uuid.toString());

        this.vertx.eventBus().<JsonObject>request(address, json, option, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void create(CreateQuestionCommand command, Handler<AsyncResult<QuestionDTO>> resultHandler) {
        DeliveryOptions option = new DeliveryOptions()
                .addHeader(HEADER_ACTION_NAME, QuestionDatabaseService.CREATE_ACTION_UID);

        JsonObject json = new JsonObject().put(MESSAGE_QUERY_PROPERTY, command.toJson());

        this.vertx.eventBus().<JsonObject>request(address, json, option, ar -> {
            if(ar.succeeded()) {
                QuestionDTO question = new QuestionDTO(ar.result().body());
                resultHandler.handle(Future.succeededFuture(question));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

}
