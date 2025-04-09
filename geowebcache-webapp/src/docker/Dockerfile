FROM jetty:9.4.46-jre11

ENV GEOWEBCACHE_CONFIG_DIR=/mnt/geowebcache_datadir \
    GEOWEBCACHE_CACHE_DIR=/mnt/geowebcache_tiles

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=jmx,jmx-remote,stats,http-forwarded

COPY --chown=jetty:jetty . /

# Temporary switch to root
USER root

RUN mkdir /mnt/geowebcache_tiles /mnt/geowebcache_datadir &&                              \
    unzip -d /var/lib/jetty/webapps/geowebcache /var/lib/jetty/webapps/geowebcache.war && \
    rm -f /var/lib/jetty/webapps/geowebcache.war &&                                       \
    chown jetty:jetty /etc/georchestra /mnt/geowebcache_tiles /mnt/geowebcache_datadir

# restore jetty user
USER jetty

VOLUME [ "/mnt/geowebcache_datadir", "/mnt/geowebcache_tiles", "/tmp", "/run/jetty" ]

ENTRYPOINT [ "/docker-entrypoint.sh" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-XX:MaxRAMPercentage=80 -XX:+UseParallelGC \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-Djetty.httpConfig.sendServerVersion=false \
-Djetty.jmxremote.rmiregistryhost=${JMX_HOST:-127.0.0.1} \
-Djetty.jmxremote.rmiserverhost=${JMX_HOST:-127.0.0.1} \
-Dcom.sun.management.jmxremote=${JMX_ENABLE:-false} \
-Dcom.sun.management.jmxremote.host=${JMX_HOST:-127.0.0.1} \
-Dcom.sun.management.jmxremote.ssl=${JMX_SSL:-false} \
-Dcom.sun.management.jmxremote.authenticate=${JMX_AUTHENTICATE:-false} \
-jar /usr/local/jetty/start.jar"]
