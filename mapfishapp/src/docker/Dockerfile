FROM jetty:9-jre8

ENV XMS=1G XMX=2G

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=jmx,jmx-remote,stats

ADD . /

# Temporary switch to root
USER root

RUN apt-get update && \
   apt-get install -y libgdal-java gdal-bin && \
   rm -rf /var/lib/apt/lists/*

RUN unzip -d /var/lib/jetty/webapps/mapfishapp /var/lib/jetty/webapps/mapfishapp.war
RUN ln -s /usr/share/java/gdal.jar /var/lib/jetty/webapps/mapfishapp/WEB-INF/lib/

RUN mkdir /mnt/mapfishapp_uploads && \
    chown jetty:jetty /etc/georchestra /mnt/mapfishapp_uploads

# Restore jetty user
USER jetty

VOLUME [ "/mnt/mapfishapp_uploads", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-Dmapfish-print-config=/etc/georchestra/mapfishapp/print/config.yaml \
-Dorg.geotools.referencing.forceXY=true \
-Xms$XMS -Xmx$XMX \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-jar /usr/local/jetty/start.jar"]
