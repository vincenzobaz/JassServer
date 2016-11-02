FROM openjdk:8

COPY . /JassServer
WORKDIR /JassServer

RUN ./gradlew build && cp build/libs/jasserver.jar .

VOLUME logs/

RUN java -jar jasserver.jar
