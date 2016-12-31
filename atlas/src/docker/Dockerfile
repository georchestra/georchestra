FROM jetty:9.3-jre8

ENV XMS=512M XMX=2G

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

ADD . /

VOLUME [ "/tmp", "/run/jetty" ]

ENTRYPOINT [ "/docker-entrypoint.sh" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-Dlogback.configurationFile=/etc/georchestra/atlas/logback/logback.xml \
-Xms$XMS -Xmx$XMX \
${JAVA_OPTIONS} \
-jar /usr/local/jetty/start.jar"]
