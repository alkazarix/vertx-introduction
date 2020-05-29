package com.talanlabs.sample01.database;

import com.talanlabs.sample01.core.QuestionDTO;
import com.talanlabs.sample01.core.ApplicationConfig;
import com.talanlabs.sample01.core.ErrorCode;
import com.talanlabs.sample01.core.SearchQuestionRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;


public class QuestionDatabaseVerticle extends AbstractVerticle {

    public static final String ADDRESS = "com.talanlabs.sample.questions.service";

    private static final String FETCH_QUESTION_SQL_QUERY = "select id, author, title, content from forum.question LIMIT %d OFFSET %d;";
    private static final Logger logger = LoggerFactory.getLogger(QuestionDatabaseVerticle.class);

    private SQLClient client;

    @Override
    public void start(Promise<Void> promise) throws Exception {

        logger.info("start verticle");

        JsonObject sqlConfig = new JsonObject()
                .put("url", config().getString(ApplicationConfig.JDBC_URL_PROPERTY))
                .put("driver_class", config().getString(ApplicationConfig.JDBC_DRIVER_PROPERTY))
                .put("user", config().getString(ApplicationConfig.JDBC_USERNAME_PROPERTY))
                .put("password", config().getString(ApplicationConfig.JDBC_PASSWORD_PROPERTY))
                .put("max_poll_size", 20);

        this.client = JDBCClient.createShared(vertx, sqlConfig);
        getVertx().eventBus().consumer(ADDRESS, this::handleMessage);

        promise.complete();
    }

    private void handleMessage(Message<JsonObject> message) {

        SearchQuestionRequest request = new SearchQuestionRequest(message.body());
        String query = String.format(FETCH_QUESTION_SQL_QUERY, request.getLimit(), (request.getPage() -1) * request.getLimit());

        this.client.query(query, ar -> {
            if (ar.succeeded()) {

                List<QuestionDTO> questions = ar.result()
                        .getResults()
                        .stream()
                        .map(jarray -> rowBookMapper(jarray))
                        .collect(Collectors.toList());

                JsonArray reply = new JsonArray(questions.stream().map(r -> r == null ? null : r.toJson()).collect(Collectors.toList()));
                message.reply(reply);
            } else {
                logger.error(ar.cause().getMessage(), ar.cause());
                message.fail(ErrorCode.DATABASE_ERROR.getCode(), ar.cause().getMessage());
            }
        });

    }

    private QuestionDTO rowBookMapper(JsonArray row) {

        return QuestionDTO
                .create()
                .withId(row.getInteger(0))
                .withAuthor(row.getString(1))
                .withTitle(row.getString(2))
                .withContent(row.getString(3));
    }


}
