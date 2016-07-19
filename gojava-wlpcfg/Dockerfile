FROM websphere-liberty:latest
MAINTAINER Erin Schnabel @ebullientworks

ENV LIBERTY_MODE run

#Set your Game On! ID and secret (you can find this information here : https://game-on.org/#/play/me) 
#<id e.g. google:12345678>
ENV GAMEON_ID google:116193734910366427141
#<your shared secret>
ENV GAMEON_SECRET 2dU3e9YlSWinA+k8FZTtJerUB9hGR0qupl1Rb4gfRUQ=

ADD https://download.elastic.co/logstash-forwarder/binaries/logstash-forwarder_linux_amd64 /opt/forwarder
ADD ./target/wlp/usr/servers/gojava-room /opt/ibm/wlp/usr/servers/defaultServer/

COPY ./forwarder.conf /opt/forwarder.conf
COPY ./startup.sh /opt/startup.sh

EXPOSE 9080

CMD ["/opt/startup.sh"]
