FROM openjdk:8

COPY . /JassServer
WORKDIR /JassServer

RUN ./gradlew build

VOLUME logs/

RUN java -jar build/libs/jasserver.jar
