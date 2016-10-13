FROM jetty:9.3-jre8

ENV XMS=256M XMX=1G

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

ADD . /

VOLUME [ "/var/local/tiles", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-DGEOWEBCACHE_CACHE_DIR=/var/local/tiles \
-Xms$XMS -Xmx$XMX \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-jar /usr/local/jetty/start.jar"]
