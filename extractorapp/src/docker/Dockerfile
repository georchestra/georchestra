FROM jetty:9-jre8

ENV XMS=1G XMX=2G

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=jmx,jmx-remote,stats,http-forwarded

ADD . /

# Temporary switch to root
USER root

RUN apt-get update && \
   apt-get install -y libgdal-java gdal-bin && \
   rm -rf /var/lib/apt/lists/*

RUN unzip -d /var/lib/jetty/webapps/extractorapp /var/lib/jetty/webapps/extractorapp.war && \
    ln -s /usr/share/java/gdal.jar /var/lib/jetty/webapps/extractorapp/WEB-INF/lib/ && \
    rm -f /var/lib/jetty/webapps/extractorapp.war

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
-Djetty.jmxremote.rmiregistryhost=0.0.0.0 \
-Djetty.jmxremote.rmiserverhost=0.0.0.0 \
-jar /usr/local/jetty/start.jar"]
