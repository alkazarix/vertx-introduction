/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package com.talanlabs.sample03.database;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.ProxyUtils;

import java.util.List;
import com.talanlabs.sample03.core.QuestionDTO;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import com.talanlabs.sample03.core.CreateQuestionCommand;
import com.talanlabs.sample03.core.SearchQuestionRequest;
/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/

@SuppressWarnings({"unchecked", "rawtypes"})
public class QuestionDatabaseServiceVertxEBProxy implements QuestionDatabaseService {
  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public QuestionDatabaseServiceVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public QuestionDatabaseServiceVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try{
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {}
  }

  @Override
  public  void list(SearchQuestionRequest request, Handler<AsyncResult<List<QuestionDTO>>> resultHandler){
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("request", request == null ? null : request.toJson());

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "list");
    _vertx.eventBus().<JsonArray>request(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body().stream()
          .map(o -> { if (o == null) return null;
              return o instanceof Map ? new QuestionDTO(new JsonObject((Map) o)) : new QuestionDTO((JsonObject) o);
            })
          .collect(Collectors.toList())));
      }
    });
  }
  @Override
  public  void getById(String uuid, Handler<AsyncResult<QuestionDTO>> resultHandler){
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("uuid", uuid);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getById");
    _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body() == null ? null : new QuestionDTO(res.result().body())));
      }
    });
  }
  @Override
  public  void delete(String uuid, Handler<AsyncResult<Void>> resultHandler){
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("uuid", uuid);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "delete");
    _vertx.eventBus().<Void>request(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }
  @Override
  public  void create(CreateQuestionCommand command, Handler<AsyncResult<QuestionDTO>> resultHandler){
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("command", command == null ? null : command.toJson());

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "create");
    _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body() == null ? null : new QuestionDTO(res.result().body())));
      }
    });
  }
}
