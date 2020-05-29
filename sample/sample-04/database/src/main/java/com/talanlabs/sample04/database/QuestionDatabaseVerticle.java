package com.talanlabs.sample04.database;

import com.talanlabs.sample04.core.api.QuestionDatabaseService;
import com.talanlabs.sample04.core.models.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.Collectors;

public class QuestionDatabaseVerticle extends AbstractVerticle {

    private static final String ADDRESS = "com.talanlabs.sample.questions.service";
    private static final String HEADER_ACTION_NAME = "action";
    private static final String MESSAGE_QUERY_PROPERTY = "_query";

    private static final Logger logger = LoggerFactory.getLogger(QuestionDatabaseVerticle.class);

    private QuestionDatabaseService service;


    public QuestionDatabaseVerticle(QuestionDatabaseService service)  {
        this.service = service;
    }


    @Override
    public void start(Promise<Void> promise) throws Exception {

        logger.info("start verticle");


        getVertx().eventBus().<JsonObject>consumer(ADDRESS, message -> {
            String method = message.headers().get(HEADER_ACTION_NAME);
            switch (method) {

                case QuestionDatabaseService.LIST_ACTION_UID:
                    this.listHandler(message);
                    break;
                case QuestionDatabaseService.GET_BY_ID_ACTION_UID:
                    this.getByIdHandler(message);
                    break;
                case  QuestionDatabaseService.DELETE_ACTION_UID:
                    this.deleteHandler(message);
                    break;
                case QuestionDatabaseService.CREATE_ACTION_UID:
                    this.createHandler(message);
                    break;
                default:
                    message.fail(ErrorCode.UNKNOWN_METHOD.getCode(), "should never append");
            }
        });

        promise.complete();



    }


    private void listHandler(Message<JsonObject> message) {
        logger.debug("receive action fetch ");
        JsonObject body = message.body().getJsonObject(MESSAGE_QUERY_PROPERTY);
        SearchQuestionRequest query = new SearchQuestionRequest(body);
        this.service.list(query, ar -> {
            if (ar.succeeded()) {
                message.reply(new JsonArray(ar.result().stream().map(r -> r == null ? null : r.toJson()).collect(Collectors.toList())));
            } else {
                logger.error(ar.cause().getMessage(), ar.cause());
                message.fail(ErrorCode.DATABASE_ERROR.getCode(), ar.cause().getMessage());
            }
        });
    }

    private void getByIdHandler(Message<JsonObject> message) {
        String uuid =   message.body().getString(MESSAGE_QUERY_PROPERTY);
        this.service.getById(UUID.fromString(uuid), ar -> {
            if (ar.succeeded() && ar.result() != null) {
                message.reply(ar.result().toJson());
            } else if (ar.succeeded()) {
                message.fail(ErrorCode.QUESTION_NOT_FOUND.getCode(), "question not found");
            } else {
                logger.error(ar.cause().getMessage(), ar.cause());
                message.fail(ErrorCode.DATABASE_ERROR.getCode(), ar.cause().getMessage());
            }
        });
    }

    private void deleteHandler(Message<JsonObject> message) {
        String uuid =   message.body().getString(MESSAGE_QUERY_PROPERTY);
        this.service.delete(UUID.fromString(uuid), ar -> {
            if (ar.succeeded()) {
                message.reply(new JsonObject());
            } else {
                logger.error(ar.cause().getMessage(), ar.cause());
                message.fail(ErrorCode.DATABASE_ERROR.getCode(), ar.cause().getMessage());
            }
        });
    }

    private void createHandler(Message<JsonObject> message) {
        JsonObject body = message.body().getJsonObject(MESSAGE_QUERY_PROPERTY);
        CreateQuestionCommand command = new CreateQuestionCommand(body);
        this.service.create(command, ar -> {
            if (ar.succeeded()) {
                message.reply(ar.result().toJson());
            } else {
                logger.error(ar.cause().getMessage(), ar.cause());
                message.fail(ErrorCode.DATABASE_ERROR.getCode(), ar.cause().getMessage());
            }
        });
    }




}
