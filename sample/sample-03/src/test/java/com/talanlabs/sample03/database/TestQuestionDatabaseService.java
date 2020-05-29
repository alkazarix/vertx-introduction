package com.talanlabs.sample03.database;

import com.talanlabs.sample03.core.*;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TestQuestionDatabaseService {


    private static String SQL_CREATE_TABLE = "CREATE TABLE  question (\n" +
            "   id INT IDENTITY PRIMARY KEY,\n" +
            "   uuid VARCHAR(255) NOT NULL,\n" +
            "   author VARCHAR(255) NOT NULL,\n" +
            "   title VARCHAR(255) NOT NULL,\n" +
            "   content VARCHAR(255) NOT NULL\n" +
            ");\n";


    private static String SQL_INSERT = "INSERT INTO question (id, uuid, author, title, content) VALUES (?, ?, ?, ?, ? );";

    private static String SQL_DELETE = "DELETE FROM question";


    private static final JsonObject CONFIG =  new JsonObject()
                .put(ApplicationConfig.JDBC_URL_PROPERTY, "jdbc:hsqldb:mem:vertxsample;shutdown=true")
                .put(ApplicationConfig.JDBC_DRIVER_PROPERTY, "org.hsqldb.jdbcDriver")
                .put(ApplicationConfig.JDBC_USERNAME_PROPERTY, "SA")
                .put(ApplicationConfig.JDBC_PASSWORD_PROPERTY, "");

    private static JDBCClient client;

    @BeforeAll
    static void prepare(Vertx vertx, VertxTestContext testContext) {
        JsonObject sqlConfig = new JsonObject()
                .put("url", CONFIG.getString(ApplicationConfig.JDBC_URL_PROPERTY))
                .put("driver_class", CONFIG.getString(ApplicationConfig.JDBC_DRIVER_PROPERTY))
                .put("max_poll_size", 20);

        client = JDBCClient.createShared(vertx, sqlConfig);
        client.query(SQL_CREATE_TABLE, testContext.succeeding(ar -> testContext.completeNow()));
    }

    @BeforeEach
    void init_database(Vertx vertx, VertxTestContext testContext) {

        JsonArray question1 = new JsonArray().add(1).add("123e4567-e89b-12d3-a456-556642440001").add("author-1").add("title-1").add("content-1");
        JsonArray question2 = new JsonArray().add(2).add("123e4567-e89b-12d3-a456-556642440002").add("author-2").add("title-2").add("content-2");
        JsonArray question3 = new JsonArray().add(3).add("123e4567-e89b-12d3-a456-556642440003").add("author-3").add("title-3").add("content-3");

        List<JsonArray> params = new ArrayList<>();
        params.add(question1);
        params.add(question2);
        params.add(question3);

        client.getConnection(testContext.succeeding(connection -> {
            connection.batchWithParams(SQL_INSERT, params, testContext.succeeding(ar -> testContext.completeNow()));
        }));
    }

    @AfterEach
    void clean_database(Vertx vertx, VertxTestContext testContext) {
        client.query(SQL_DELETE, testContext.succeeding(ar -> testContext.completeNow()));
    }


    @Test
    @DisplayName("ACTION fetch_question , service ok should return list of question")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void fetch_success(Vertx vertx, VertxTestContext testContext) {
        QuestionDatabaseService service = new QuestionDatabaseServiceImpl(vertx, CONFIG);
        service.list(new SearchQuestionRequest().withLimit(10).withPage(1), testContext.succeeding(questions ->  {
            assertEquals(questions.size(), 3);
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION fetch_question with limit, service ok should return list of question")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void fetch_with_limit(Vertx vertx, VertxTestContext testContext) {
        QuestionDatabaseService service = new QuestionDatabaseServiceImpl(vertx, CONFIG);
        service.list(new SearchQuestionRequest().withLimit(1).withPage(1), testContext.succeeding(questions ->  {
            assertEquals(questions.size(), 1);
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION fetch_question with page, service ok should return list of question")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void fetch_with_page(Vertx vertx, VertxTestContext testContext) {
        QuestionDatabaseService service = new QuestionDatabaseServiceImpl(vertx, CONFIG);
        service.list(new SearchQuestionRequest().withLimit(2).withPage(2), testContext.succeeding(questions ->  {
            assertEquals(questions.size(), 1);
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION get question by id , exiting  question should return")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_exist_question(Vertx vertx, VertxTestContext testContext) {
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-556642440001");
        QuestionDatabaseService service = new QuestionDatabaseServiceImpl(vertx, CONFIG);
        service.getById(uuid.toString(), testContext.succeeding(question ->  {
            assertEquals(question.getUUID().toString(), uuid.toString());
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION get question by id , question is not present should return null")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_absent_question(Vertx vertx, VertxTestContext testContext) {
        UUID uuid = UUID.randomUUID();
        QuestionDatabaseService service = new QuestionDatabaseServiceImpl(vertx, CONFIG);
        service.getById(uuid.toString(), testContext.failing(ex ->  {
            assert ex instanceof ServiceException;
            assertEquals(((ServiceException) ex).failureCode(), ErrorCode.QUESTION_NOT_FOUND.getCode());
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION delete question , should be a success")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_question(Vertx vertx, VertxTestContext testContext) {
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-556642440001");
        QuestionDatabaseService service = new QuestionDatabaseServiceImpl(vertx, CONFIG);
        service.delete(uuid.toString(), testContext.succeeding(question ->  {
            client.query("SELECT * FROM question", testContext.succeeding(results -> {
                assertEquals(results.getNumRows(), 2);
                testContext.completeNow();
            }));
        }));
    }

    @Test
    @DisplayName("ACTION create question , should be a success")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void create_question(Vertx vertx, VertxTestContext testContext) {

        CreateQuestionCommand command = new CreateQuestionCommand()
                .withAuthor("author-4")
                .withTitle("title-4")
                .withContent("content-4");

        QuestionDatabaseService service = new QuestionDatabaseServiceImpl(vertx, CONFIG);
        service.create(command, testContext.succeeding(question ->  {
            assertNotNull(question.getUUID());
            client.query("SELECT * FROM question", testContext.succeeding(results -> {
                assertEquals(results.getNumRows(), 4);
                testContext.completeNow();
            }));
        }));
    }



}
