FROM openjdk:8

COPY . /JassServer
WORKDIR /JassServer

RUN ./gradlew build

RUN source prod.sh

VOLUME logs/

RUN java -jar build/libs/jasserver.jar
