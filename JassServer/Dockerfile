FROM openjdk:8-alpine

COPY . /JassServer
WORKDIR /JassServer

RUN ./gradlew build && cp build/libs/jasserver.jar .

CMD java -jar jasserver.jar
