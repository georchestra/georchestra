FROM jetty:9.3-jre8

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

ADD . /

VOLUME [ "/tmp" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-Xmx$XMX -Xms$XMX \
-XX:-UsePerfData \
-jar /usr/local/jetty/start.jar"]
