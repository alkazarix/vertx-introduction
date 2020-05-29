package com.talanlabs.sample04.database;

import com.talanlabs.sample04.core.api.*;
import com.talanlabs.sample04.core.models.*;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestionDatabaseServiceImpl implements QuestionDatabaseService {

    private static final String FETCH_SQL_QUERY = "select uuid, author, title, content from  question LIMIT %d OFFSET %d;";
    private static final String GET_BY_ID_SQL_QUERY = "select uuid, author, title, content  from question WHERE uuid=?";
    private static final String DELETE_SQL_QUERY = "delete from question WHERE uuid=?";
    private static final String CREATE_SQL_QUERY = "insert into question(uuid, author, title, content) values(?,?, ?, ?); ";


    private static final Logger logger = LoggerFactory.getLogger(QuestionDatabaseServiceImpl.class);
    private SQLClient client;


    public QuestionDatabaseServiceImpl(Vertx vertx, JsonObject config) {

        JsonObject sqlConfig = new JsonObject()
                .put("url", config.getString(ApplicationConfig.JDBC_URL_PROPERTY))
                .put("driver_class", config.getString(ApplicationConfig.JDBC_DRIVER_PROPERTY))
                .put("user", config.getString(ApplicationConfig.JDBC_USERNAME_PROPERTY))
                .put("password", config.getString(ApplicationConfig.JDBC_PASSWORD_PROPERTY))
                .put("max_poll_size", 20);

        this.client = JDBCClient.createShared(vertx, sqlConfig);
    }



    @Override
    public void list(SearchQuestionRequest request, Handler<AsyncResult<List<QuestionDTO>>> resultHandler) {

        String query = String.format(FETCH_SQL_QUERY, request.getLimit(), (request.getPage() -1) * request.getLimit());

        this.client.query(query, ar -> {
            if (ar.succeeded()) {

                List<QuestionDTO> questions = ar.result()
                        .getResults()
                        .stream()
                        .map(jarray -> rowToQuestion(jarray))
                        .collect(Collectors.toList());

                resultHandler.handle(Future.succeededFuture(questions));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void getById(UUID uuid, Handler<AsyncResult<QuestionDTO>> resultHandler) {

        this.client.queryWithParams(GET_BY_ID_SQL_QUERY, new JsonArray().add(uuid.toString()), ar -> {
            if (ar.succeeded()) {
                if (ar.result().getNumRows() == 0) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    QuestionDTO question = rowToQuestion(ar.result().getResults().get(0));
                    resultHandler.handle(Future.succeededFuture(question));
                }
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void delete(UUID uuid, Handler<AsyncResult<Void>> resultHandler) {
        this.client.queryWithParams(DELETE_SQL_QUERY, new JsonArray().add(uuid.toString()), ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void create(CreateQuestionCommand command, Handler<AsyncResult<QuestionDTO>> resultHandler) {
        UUID uuid = UUID.randomUUID();

        JsonArray params = new JsonArray()
                .add(uuid.toString())
                .add(command.getAuthor())
                .add(command.getTitle())
                .add(command.getContent());

        this.client.queryWithParams(CREATE_SQL_QUERY, params, ar -> {
            if (ar.succeeded()) {

                QuestionDTO questionDTO = new QuestionDTO()
                        .withUID(uuid)
                        .withAuthor(command.getAuthor())
                        .withTitle(command.getTitle())
                        .withContent(command.getContent());

                resultHandler.handle(Future.succeededFuture(questionDTO));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private QuestionDTO rowToQuestion(JsonArray row) {
        return QuestionDTO
                .create()
                .withUID(UUID.fromString(row.getString(0)))
                .withAuthor(row.getString(1))
                .withTitle(row.getString(2))
                .withContent(row.getString(3));
    }
}
