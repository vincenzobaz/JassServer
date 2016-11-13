FROM openjdk:8-alpine

COPY . /JassServer
WORKDIR /JassServer

RUN /bin/ash ./gradlew build && cp build/libs/jasserver.jar .

CMD java -jar jasserver.jar
