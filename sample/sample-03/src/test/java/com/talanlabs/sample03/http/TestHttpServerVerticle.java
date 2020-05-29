package com.talanlabs.sample03.http;

import com.talanlabs.sample03.core.*;
import com.talanlabs.sample03.database.QuestionDatabaseService;

import io.vertx.core.*;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(VertxExtension.class)
public class TestHttpServerVerticle {

    private static Integer listenPort;

    private QuestionDatabaseService databaseService;
    private String deploymentId;

    @BeforeAll
    static void preprare(Vertx vertx, VertxTestContext testContext){
        Random r = new Random();
        listenPort = r.nextInt(5000) + 3000;
        testContext.completeNow();
    }


    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        databaseService = Mockito.mock(QuestionDatabaseService.class);
        JsonObject config = new JsonObject().put("listen.port", listenPort);
        vertx.deployVerticle(new HttpServerVerticle(databaseService), new DeploymentOptions().setConfig(config), ar -> {
            if (ar.succeeded()) {
                deploymentId = ar.result();
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @AfterEach
    void undeploy_verticle(Vertx vertx, VertxTestContext testContext) {
        if (deploymentId != null) {
            vertx.undeploy(deploymentId, ar -> {
               if (ar.succeeded())
                   testContext.completeNow();
               else
                testContext.failNow(ar.cause());
            });
        } else {
            testContext.completeNow();
        }
    }

    @Test
    @DisplayName("GET /doc/ Should have swagger")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void have_swagger(Vertx vertx, VertxTestContext testContext) throws Throwable {
        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/doc/")
                .send(testContext.succeeding(response -> {
                    testContext.verify(() -> assertEquals(response.statusCode(), 200));
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("unknown endpoint should return 404")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void unknown_endpoint_should_return_404(Vertx vertx, VertxTestContext testContext) throws Throwable {
        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/unknown/")
                .send(testContext.succeeding(response -> {
                    testContext.verify(() -> {
                        assertEquals(response.statusCode(),404);
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("unknown endpoint should return json")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void unknown_endpoint_should_return_json(Vertx vertx, VertxTestContext testContext) throws Throwable {
        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/unknown/")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.bodyAsJsonObject().getInteger("status"), 404);
                            assertEquals(response.bodyAsJsonObject().getString("path"), "/unknown/");
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions - when service success should return 200")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_questions_success_return_200(Vertx vertx, VertxTestContext testContext) throws Throwable {

        List<QuestionDTO> questions = new ArrayList<>();

        Mockito.doAnswer(new Answer<AsyncResult<List<QuestionDTO>>>() {
            @Override
            public AsyncResult<List<QuestionDTO>>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<List<QuestionDTO>>>) invocation.getArguments()[1]).handle(Future.succeededFuture(questions));
                return null;
            }
        }).when(databaseService).list(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/questions")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 200);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions - when service success should return hal response")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_questions_success_return_hal_response(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        List<QuestionDTO> questions = new ArrayList<>();
        questions.add(new QuestionDTO().withUUID(uuid.toString()).withTitle("test").withAuthor("test"));

        Mockito.doAnswer(new Answer<AsyncResult<List<QuestionDTO>>>() {
            @Override
            public AsyncResult<List<QuestionDTO>>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<List<QuestionDTO>>>) invocation.getArguments()[1]).handle(Future.succeededFuture(questions));
                return null;
            }
        }).when(databaseService).list(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/questions")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.bodyAsJsonObject().getJsonObject("_links", new JsonObject()).getJsonObject("self", new JsonObject()).getString("href"), String.format("http://localhost:%d/questions", listenPort));
                            assertEquals(response.bodyAsJsonObject().getJsonObject("_embedded", new JsonObject()).getJsonArray("questions", new JsonArray()).size() ,1);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions - invalid parameter limit should return 400")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_questions_invalid_parameter_limit_return_400(Vertx vertx, VertxTestContext testContext) throws Throwable {
        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/questions")
                .addQueryParam("limit", "not a number")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 400);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions - invalid parameter page should return 400")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_questions_invalid_parameter_page_return_400(Vertx vertx, VertxTestContext testContext) throws Throwable {
        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/questions")
                .addQueryParam("page", "not a uuid")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 400);
                            testContext.completeNow();
                        })
                ));
    }


    @Test
    @DisplayName("GET /questions - when service fail should return 500")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_questions_fail_return_500(Vertx vertx, VertxTestContext testContext) throws Throwable {

        Mockito.doAnswer(new Answer<AsyncResult<List<QuestionDTO>>>() {
            @Override
            public AsyncResult<List<QuestionDTO>>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<List<QuestionDTO>>>) invocation.getArguments()[1]).handle(Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, ErrorCode.DATABASE_ERROR.getCode(), "fail to fetch")));
                return null;
            }
        }).when(databaseService).list(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/questions")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 500);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions - when service fail should return json")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_questions_fail_return_json(Vertx vertx, VertxTestContext testContext) throws Throwable {

        Mockito.doAnswer(new Answer<AsyncResult<List<QuestionDTO>>>() {
            @Override
            public AsyncResult<List<QuestionDTO>>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<List<QuestionDTO>>>) invocation.getArguments()[1]).handle(Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, ErrorCode.DATABASE_ERROR.getCode(), "fail to fetch")));
                return null;
            }
        }).when(databaseService).list(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/questions")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.bodyAsJsonObject().getInteger("status"), 500);
                            assertEquals(response.bodyAsJsonObject().getString("path"), "/questions");
                            testContext.completeNow();
                        })
                ));
    }


    @Test
    @DisplayName("GET /questions/:id - when service success should return 200")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_by_id_success_return_200(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        QuestionDTO question= new QuestionDTO().withUUID(uuid.toString()).withAuthor("test").withTitle("test");
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture(question));
                return null;
            }
        }).when(databaseService).getById(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", String.format("/questions/%s", uuid.toString()))
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 200);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions/:id - when service success should return hal response")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_by_id_success_return_hal_response(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        QuestionDTO question = new QuestionDTO().withUUID(uuid.toString()).withTitle("test").withAuthor("test");

        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture(question));
                return null;
            }
        }).when(databaseService).getById(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", String.format("/questions/%s", uuid.toString()))
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.bodyAsJsonObject().getJsonObject("_links", new JsonObject()).getJsonObject("self", new JsonObject()).getString("href"), String.format("/questions/%s", uuid.toString()));
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions/:id - invalid id should return 400")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_with_invalid_id(Vertx vertx, VertxTestContext testContext) throws Throwable {
        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", "/questions/not_a_uuid")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 400);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions/:id - when service fail with not found error should return 404")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_by_id_fail_return_404(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.failedFuture(new ServiceException(ErrorCode.QUESTION_NOT_FOUND.getCode(), "fail to fetch")));
                return null;
            }
        }).when(databaseService).getById(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", String.format("/questions/%s", uuid.toString()))
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 404);
                            testContext.completeNow();
                        })
                ));
    }


    @Test
    @DisplayName("GET /questions - when service fail with database error should return 500")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_by_id_fail_return_500(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, ErrorCode.DATABASE_ERROR.getCode(), "fail to fetch")));
                return null;
            }
        }).when(databaseService).getById(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", String.format("/questions/%s", uuid.toString()))
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 500);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("GET /questions/:id - when service fail should return json")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_by_id_fail_return_json(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<List<QuestionDTO>>>) invocation.getArguments()[1]).handle(Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, ErrorCode.DATABASE_ERROR.getCode(), "fail to fetch")));
                return null;
            }
        }).when(databaseService).getById(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .get(listenPort, "localhost", String.format("/questions/%s", uuid.toString()))
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.bodyAsJsonObject().getInteger("status"), 500);
                            assertEquals(response.bodyAsJsonObject().getString("path"), String.format("/questions/%s", uuid.toString()));
                            testContext.completeNow();
                        })
                ));
    }


    @Test
    @DisplayName("POST /questions - invalid create question command should return 400")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void create_questions_invalid_body_return_400(Vertx vertx, VertxTestContext testContext) throws Throwable {

        CreateQuestionCommand command = new CreateQuestionCommand()
                .withAuthor("a")
                .withContent("b")
                .withTitle("c");

        WebClient client = WebClient.create(vertx);
        client
                .post(listenPort, "localhost", "/questions")
                .sendJsonObject(command.toJson(), testContext.succeeding(response -> {
                    testContext.verify(() -> {
                        assertEquals(response.statusCode(), 400);
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("POST /questions - service success should return 201")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void create_question_success_return_201(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        QuestionDTO question= new QuestionDTO().withUUID(uuid.toString()).withAuthor("test").withTitle("test");
        CreateQuestionCommand command = new CreateQuestionCommand().withAuthor("test").withContent("test").withTitle("test");

        JsonObject json = command.toJson();
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture(question));
                return null;
            }
        }).when(databaseService).create(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .post(listenPort, "localhost", "/questions")
                .sendJsonObject(command.toJson(), testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 201);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("POST /questions - service success should return hal resource")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void create_question_success_return_hal_response(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        QuestionDTO question= new QuestionDTO().withUUID(uuid.toString()).withAuthor("test").withTitle("test");
        CreateQuestionCommand command = new CreateQuestionCommand().withAuthor("test").withContent("test").withTitle("test");
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture(question));
                return null;
            }
        }).when(databaseService).create(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .post(listenPort, "localhost", "/questions")
                .sendJsonObject(command.toJson(), testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.bodyAsJsonObject().getJsonObject("_links", new JsonObject()).getJsonObject("self", new JsonObject()).getString("href"), String.format("/questions/%s", uuid.toString()));
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("POST /questions - service fail should return 500")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void create_question_fail_return_500(Vertx vertx, VertxTestContext testContext) throws Throwable {

        CreateQuestionCommand command = new CreateQuestionCommand().withAuthor("test").withContent("test").withTitle("test");
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.failedFuture("fail to create question"));
                return null;
            }
        }).when(databaseService).create(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .post(listenPort, "localhost", "/questions")
                .sendJsonObject(command.toJson(), testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 500);
                            testContext.completeNow();
                        })
                ));
    }


    @Test
    @DisplayName("DELETE /questions/:id - invalid id should return 400")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_question_with_invalid_id(Vertx vertx, VertxTestContext testContext) throws Throwable {
        WebClient client = WebClient.create(vertx);
        client
                .delete(listenPort, "localhost", "/questions/not_a_uuid")
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 400);
                            testContext.completeNow();
                        })
                ));
    }


    @Test
    @DisplayName("DELETE /questions/:id - when service success should return 200")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_question_by_id_success_return_204(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        QuestionDTO question= new QuestionDTO().withUUID(uuid.toString()).withAuthor("test").withTitle("test");
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture(question));
                return null;
            }
        }).when(databaseService).delete(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .delete(listenPort, "localhost", String.format("/questions/%s", uuid.toString()))
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 204);
                            testContext.completeNow();
                        })
                ));
    }

    @Test
    @DisplayName("DELETE /questions/:id - when service fail should return 500")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_question_by_id_fail_return_500(Vertx vertx, VertxTestContext testContext) throws Throwable {

        UUID uuid = UUID.randomUUID();
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.failedFuture("fail to delete"));
                return null;
            }
        }).when(databaseService).delete(Mockito.any(), Mockito.any());

        WebClient client = WebClient.create(vertx);
        client
                .delete(listenPort, "localhost", String.format("/questions/%s", uuid.toString()))
                .send(testContext.succeeding(response ->
                        testContext.verify(() -> {
                            assertEquals(response.statusCode(), 500);
                            testContext.completeNow();
                        })
                ));
    }

}
