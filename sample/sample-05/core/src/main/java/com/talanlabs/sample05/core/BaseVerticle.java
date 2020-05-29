package com.talanlabs.sample05.core;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseVerticle extends AbstractVerticle {

    private Set<Record> records = new ConcurrentHashSet<>();

    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;

    @Override
    public void start() throws Exception {
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

        circuitBreaker = CircuitBreaker.create(config().getString("circuit.breaker.name", "circuit-breaker"), vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(config().getInteger("circuit.breaker.failure", 5))
                        .setTimeout(config().getLong("circuit.breaker.timeout", 10000L))
                        .setFallbackOnFailure(true)
                        .setResetTimeout(config().getLong("circuit.breaker.timeout", 30000L))
        );
    }

    protected Future<Void> publishHttpEndpoint(String name, String host, int port) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/",
                new JsonObject().put("api.name", config().getString("api.name", ""))
        );
        return publish(record);
    }

    protected Future<Void> publishMessageSource(String name, String address) {
        Record record = MessageSource.createRecord(name, address);
        return publish(record);
    }

    protected Future<Void> publishEventBusService(String name, String address, Class serviceClass) {
        Record record = EventBusService.createRecord(name, address, serviceClass);
        return publish(record);
    }

    private Future<Void> publish(Record record) {
        Promise<Void> promise = Promise.promise();
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                records.add(record);
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public void stop(Promise<Void> completer) throws Exception {
        List<Future> futures = new ArrayList<>();
        records.forEach(record -> {
            Promise<Void> promise = Promise.promise();
            futures.add(promise.future());
            discovery.unpublish(record.getRegistration(), ar -> {
                if ((ar.succeeded())) {
                    promise.complete();
                } else {
                    promise.fail(ar.cause());
                }
            });
        });

        if (futures.isEmpty()) {
            discovery.close();
            completer.complete();
        } else {
            CompositeFuture.all(futures).setHandler(ar -> {
                discovery.close();
                if (ar.failed()) {
                    completer.fail(ar.cause());
                } else {
                    completer.complete();
                }
            });
        }
    }



}
