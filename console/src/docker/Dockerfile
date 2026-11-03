FROM jetty:11-jre17

# remove jetty embedded "JavaMail", we have our own.
# fix #3692 : ClassNotFoundException: javax.activation.DataSource
# https://github.com/eclipse/jetty.docker/issues/10
USER root
RUN #rm -r /usr/local/jetty/lib/mail
RUN mkdir -p /docker-entrypoint.d
RUN chown jetty:jetty /docker-entrypoint.d
#install bash for entrypoint script
RUN #apk add --no-cache bash
USER jetty

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-modules=jmx,jmx-remote,http-forwarded

COPY --chown=jetty:jetty . /

ENTRYPOINT [ "/docker-entrypoint.sh" ]

VOLUME [ "/tmp" ]

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

# OCI image labels (https://github.com/opencontainers/image-spec/blob/main/annotations.md)
LABEL org.opencontainers.image.title="geOrchestra Console"
LABEL org.opencontainers.image.description="geOrchestra Console"
LABEL org.opencontainers.image.authors="PSC <psc@georchestra.org>"
LABEL org.opencontainers.image.vendor="geOrchestra"
LABEL org.opencontainers.image.url="https://www.georchestra.org"
LABEL org.opencontainers.image.documentation="https://docs.georchestra.org/"
LABEL org.opencontainers.image.source="https://github.com/georchestra/georchestra"
LABEL org.opencontainers.image.licenses="GPL-3.0-only"
