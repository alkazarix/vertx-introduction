FROM openjdk:11

WORKDIR /app

ENV ZOOKEEPER_HOST  localhost
ENV VERTX_CLUSTER_HOST localhost
ENV VERTX_CLUSTER_PORT 17011

COPY build/libs/http-1.0.0-fat.jar http.jar

ENTRYPOINT java   -Dzookeeper.host=${ZOOKEEPER_HOST}  -Dvertx.cluster.port=${VERTX_CLUSTER_PORT} -Dvertx.cluster.host=${VERTX_CLUSTER_HOST}   -jar  http.jar -cluster