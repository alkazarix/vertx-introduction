package com.talanlabs.sample05.account;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {
    private static final String ZOOKEEPER_HOST_PROPERTY = "zookeeper.host";
    private static final String VERTX_CLUSTER_HOST_PROPERTY = "vertx.cluster.host";
    private static final String VERTX_CLUSTER_PORT_PROPERTY = "vertx.cluster.port";

    private static final String DEFAULT_ZOOKEEPER_HOST = "localhost";
    private static final String DEFAULT_VERTX_CLUSTER_HOST = "localhost";
    private static final String DEFAULT_VERTX_CLUSTER_PORT = "15150";

    private static Logger logger = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {

        logger.info("zookeeper host property " + System.getProperty(ZOOKEEPER_HOST_PROPERTY));

        JsonObject zkConfig = new JsonObject()
                .put("zookeeperHosts", System.getProperty(ZOOKEEPER_HOST_PROPERTY, DEFAULT_ZOOKEEPER_HOST))
                .put("rootPath", "io.vertx")
                .put("retry", new JsonObject().put("initialSleepTime", 3000).put("maxTimes", 3));


        EventBusOptions eventBusOptions = new EventBusOptions()
                .setClustered(true)
                .setHost(System.getProperty(VERTX_CLUSTER_HOST_PROPERTY, DEFAULT_VERTX_CLUSTER_HOST))
                .setClusterPublicHost(System.getProperty(VERTX_CLUSTER_HOST_PROPERTY, DEFAULT_VERTX_CLUSTER_HOST))
                .setPort(Integer.valueOf(System.getProperty(VERTX_CLUSTER_PORT_PROPERTY, DEFAULT_VERTX_CLUSTER_PORT)))
                .setClusterPublicPort(Integer.valueOf(System.getProperty(VERTX_CLUSTER_PORT_PROPERTY, DEFAULT_VERTX_CLUSTER_PORT)));


        ClusterManager mgr = new ZookeeperClusterManager(zkConfig);
        VertxOptions options = new VertxOptions()
                .setClusterManager(mgr)
                .setEventBusOptions(eventBusOptions);

        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                logger.info("Launch clustered http verticle");
                Vertx vertx = res.result();
                vertx.deployVerticle(new MainVerticle());
            } else {
                logger.error("fail to connect to zookeeper cluster", res.cause());
            }
        });

    }
}
