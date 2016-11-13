FROM openjdk:8-alpine

COPY . /JassServer
WORKDIR /JassServer

SHELL ["/bin/ash", "-c"]

RUN ./gradlew build && cp build/libs/jasserver.jar .

CMD java -jar jasserver.jar
