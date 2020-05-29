FROM openjdk:11

WORKDIR /app
COPY build/libs/sample-1.0.0-fat.jar bookstore-api.jar

ENTRYPOINT java -jar  bookstore-api.jar