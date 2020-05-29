package com.talanlabs.sample00;

import io.vertx.core.Vertx;

public class Runner {

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx(new VertxOptions());
    vertx.deployVerticle(new MainVerticle());

  }


}