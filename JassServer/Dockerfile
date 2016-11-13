FROM openjdk:8-alpine

COPY . /JassServer
WORKDIR /JassServer

SHELL ["/bin/ash", "-c"]

RUN apk add bash && ./gradlew build && cp build/libs/jasserver.jar .

CMD java -jar jasserver.jar
