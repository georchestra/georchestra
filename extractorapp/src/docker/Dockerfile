FROM jetty:9-jre11

ENV XMS=1G XMX=2G


RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=jmx,jmx-remote,stats,http-forwarded

COPY --chown=jetty:jetty . /

# Temporary switch to root
USER root
# remove jetty embedded "JavaMail", we have our own.
# fix #3692, #3704: ClassNotFoundException: javax.activation.DataSource
# https://github.com/eclipse/jetty.docker/issues/10
RUN rm -r /usr/local/jetty/lib/mail


RUN mkdir /mnt/extractorapp_extracts && \
    chown jetty:jetty /etc/georchestra /mnt/extractorapp_extracts

# Restore jetty user
USER jetty

VOLUME [ "/mnt/extractorapp_extracts", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Djava.util.prefs.userRoot=/tmp/userPrefs \
-Djava.util.prefs.systemRoot=/tmp/systemPrefs \
-Dgeorchestra.datadir=/etc/georchestra \
-Dextractor.storage.dir=/mnt/extractorapp_extracts \
-Dorg.geotools.referencing.forceXY=true \
-Xms$XMS -Xmx$XMX \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-Djetty.httpConfig.sendServerVersion=false \
-Djetty.jmxremote.rmiregistryhost=0.0.0.0 \
-Djetty.jmxremote.rmiserverhost=0.0.0.0 \
-jar /usr/local/jetty/start.jar"]
