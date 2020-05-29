package com.talanlabs.sample02.database;

import com.talanlabs.sample02.core.CreateQuestionCommand;
import com.talanlabs.sample02.core.ErrorCode;
import com.talanlabs.sample02.core.QuestionDTO;
import com.talanlabs.sample02.core.SearchQuestionRequest;
import io.vertx.core.*;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestQuestionDatabaseVerticle {


    private QuestionDatabaseService service;
    private String deploymentId;

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        this.service = Mockito.mock(QuestionDatabaseService.class);

        QuestionDatabaseVerticle verticle = new QuestionDatabaseVerticle(service);
        vertx.deployVerticle(verticle, new DeploymentOptions(), testContext.succeeding(id -> {
            deploymentId = id;
            testContext.completeNow();
        }));
    }

    @AfterEach
    void cleanup_verticle(Vertx vertx, VertxTestContext  testContext) {
        if (deploymentId != null) {
            vertx.undeploy(deploymentId, testContext.succeeding(r -> {
                testContext.completeNow();
            }));
        } else {
            testContext.completeNow();
        }
    }


    @Test
    @DisplayName("ACTION fetch_question , service ok should return list of question")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void fetch_question_success(Vertx vertx, VertxTestContext testContext) {
        List<QuestionDTO> questions = new ArrayList<>();
        questions.add(new QuestionDTO().withUID(UUID.randomUUID()).withTitle("test").withAuthor("test"));

        Mockito.doAnswer(new Answer<AsyncResult<List<QuestionDTO>>>() {
            @Override
            public AsyncResult<List<QuestionDTO>>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<List<QuestionDTO>>>) invocation.getArguments()[1]).handle(Future.succeededFuture(questions));
                return null;
            }
        }).when(service).list(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).list(new SearchQuestionRequest().withLimit(1).withPage(1), testContext.succeeding(results -> {
            assertEquals(results.size(), 1);
            assertEquals(results.get(0).getTitle(), "test");
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION fetch_question , service fail should throw a ReplyException with database error code")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void fetch_question_fail(Vertx vertx, VertxTestContext testContext) {

        Mockito.doAnswer(new Answer<AsyncResult<List<QuestionDTO>>>() {
            @Override
            public AsyncResult<List<QuestionDTO>>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<List<QuestionDTO>>>) invocation.getArguments()[1]).handle(Future.failedFuture("database error"));
                return null;
            }
        }).when(service).list(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).list(new SearchQuestionRequest().withLimit(1).withPage(1), testContext.failing(ex -> {
           assert ex instanceof ReplyException;
           assertEquals(((ReplyException) ex).failureCode(), ErrorCode.DATABASE_ERROR.getCode());
           testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION get_question , service ok should return a question")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_success(Vertx vertx, VertxTestContext testContext) {
        QuestionDTO question = new QuestionDTO().withUID(UUID.randomUUID()).withTitle("test").withAuthor("test");

        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture(question));
                return null;
            }
        }).when(service).getByID(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).getByID(question.getUID(), testContext.succeeding(result -> {
            assertEquals(result.getUID(), question.getUID());
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION get_question , service return null should throw a ReplyException")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_empty_response(Vertx vertx, VertxTestContext testContext) {

        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture());
                return null;
            }
        }).when(service).getByID(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).getByID(UUID.randomUUID(), testContext.failing(ex -> {
            assert ex instanceof ReplyException;
            assertEquals(((ReplyException) ex).failureCode(), ErrorCode.QUESTION_NOT_FOUND.getCode());
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION get_question , service fail should throw a ReplyException with database error code")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_question_fail(Vertx vertx, VertxTestContext testContext) {

        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.failedFuture("database error"));
                return null;
            }
        }).when(service).getByID(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).getByID(UUID.randomUUID(), testContext.failing(ex -> {
            assert ex instanceof ReplyException;
            assertEquals(((ReplyException) ex).failureCode(), ErrorCode.DATABASE_ERROR.getCode());
            testContext.completeNow();
        }));
    }


    @Test
    @DisplayName("ACTION create_question , service ok should return a question")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void create_question_success(Vertx vertx, VertxTestContext testContext) {
        QuestionDTO question = new QuestionDTO().withUID(UUID.randomUUID()).withTitle("test").withAuthor("test");

        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture(question));
                return null;
            }
        }).when(service).create(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).create(new CreateQuestionCommand(), testContext.succeeding(result -> {
            assertEquals(result.getUID(), question.getUID());
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION create_question , service fail should throw a ReplyException")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void create_question_fail(Vertx vertx, VertxTestContext testContext) {

        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.failedFuture("cannot create question"));
                return null;
            }
        }).when(service).create(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).create(new CreateQuestionCommand(), testContext.failing(ex -> {
            assert ex instanceof ReplyException;
            assertEquals(((ReplyException) ex).failureCode(), ErrorCode.DATABASE_ERROR.getCode());
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION delete_question , service ok should return a empty response")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_question_success(Vertx vertx, VertxTestContext testContext) {
        UUID uuid = UUID.randomUUID();
        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.succeededFuture());
                return null;
            }
        }).when(service).delete(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).delete(uuid, testContext.succeeding(result -> {
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("ACTION delete_question , service fail should throw a ReplyException")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_question_fail(Vertx vertx, VertxTestContext testContext) {

        Mockito.doAnswer(new Answer<AsyncResult<QuestionDTO>>() {
            @Override
            public AsyncResult<QuestionDTO>answer(InvocationOnMock invocation) throws Throwable {
                ((Handler<AsyncResult<QuestionDTO>>) invocation.getArguments()[1]).handle(Future.failedFuture("cannot delete question"));
                return null;
            }
        }).when(service).delete(Mockito.any(), Mockito.any());

        QuestionDatabaseService.createProxy(vertx).delete(UUID.randomUUID(), testContext.failing(ex -> {
            assert ex instanceof ReplyException;
            assertEquals(((ReplyException) ex).failureCode(), ErrorCode.DATABASE_ERROR.getCode());
            testContext.completeNow();
        }));
    }



}
