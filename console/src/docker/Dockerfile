FROM jetty:9-jre11

# remove jetty embedded "JavaMail", we have our own.
# fix #3692 : ClassNotFoundException: javax.activation.DataSource
# https://github.com/eclipse/jetty.docker/issues/10
USER root
RUN rm -r /usr/local/jetty/lib/mail
RUN mkdir -p /docker-entrypoint.d
RUN chown jetty /docker-entrypoint.d
USER jetty

ENV XMS=512M XMX=1G

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=jmx,jmx-remote,stats,http-forwarded

COPY --chown=jetty:jetty . /

ENTRYPOINT [ "/docker-entrypoint.sh" ]

VOLUME [ "/tmp" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-Xms$XMS -Xmx$XMX \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-Djetty.httpConfig.sendServerVersion=false \
-Djetty.jmxremote.rmiregistryhost=0.0.0.0 \
-Djetty.jmxremote.rmiserverhost=0.0.0.0 \
-jar /usr/local/jetty/start.jar"]
