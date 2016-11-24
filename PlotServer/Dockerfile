FROM ubuntu:latest

RUN apt-get update && apt-get install -y python3 python3-dev python3-pip

RUN pip3 install flask uWSGI numpy matplotlib

COPY . PlotServer/
WORKDIR PlotServer

CMD uwsgi --ini Server.ini
