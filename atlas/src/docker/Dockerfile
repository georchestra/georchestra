FROM jetty:9.3-jre8

ADD . /

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

ENTRYPOINT [ "/docker-entrypoint.sh" ]
CMD ["sh", "-c", "exec java -Djava.io.tmpdir=/tmp/jetty -Dgeorchestra.datadir=/etc/georchestra -Xmx$XMX -Xms$XMX -Dlogback.configurationFile=/etc/georchestra/atlas/logback/logback.xml -jar /usr/local/jetty/start.jar"]

