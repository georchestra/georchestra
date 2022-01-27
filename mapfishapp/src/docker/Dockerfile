FROM jetty:9-jre11

ENV XMS=1G XMX=2G

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=jmx,jmx-remote,stats,http-forwarded

COPY --chown=jetty:jetty . /
# Temporary switch to root
USER root

RUN mkdir /mnt/mapfishapp_uploads && \
    chown jetty:jetty /etc/georchestra /mnt/mapfishapp_uploads

# Restore jetty user
USER jetty

VOLUME [ "/mnt/mapfishapp_uploads", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Duser.home=/mnt/mapfishapp_uploads \
-Dgeorchestra.datadir=/etc/georchestra \
-Dmapfish-print-config=/etc/georchestra/mapfishapp/print/config.yaml \
-Dorg.geotools.referencing.forceXY=true \
-Xms$XMS -Xmx$XMX \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-Djetty.httpConfig.sendServerVersion=false \
-Djetty.jmxremote.rmiregistryhost=0.0.0.0 \
-Djetty.jmxremote.rmiserverhost=0.0.0.0 \
-jar /usr/local/jetty/start.jar"]
