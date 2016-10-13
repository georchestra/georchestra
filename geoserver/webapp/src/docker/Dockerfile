FROM jetty:9.2-jre7

ENV XMS=1536M XMX=8G

RUN echo "deb http://httpredir.debian.org/debian jessie contrib" >> /etc/apt/sources.list
RUN echo "deb http://security.debian.org/ jessie/updates contrib" >> /etc/apt/sources.list

RUN apt-get update && \
   apt-get install -y ttf-mscorefonts-installer  && \
   rm -rf /var/lib/apt/lists/*

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats,jndi

ADD . /

VOLUME [ "/var/local/geoserver", "/var/local/geodata", "/var/local/tiles", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java -Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-Dgeofence.dir=/etc/georchestra/geoserver/geofence \
-DGEOSERVER_DATA_DIR=/var/local/geoserver \
-DGEOWEBCACHE_CACHE_DIR=/var/local/tiles \
-DENABLE_JSONP=true \
-DENABLE_ADVANCED_PROJECTION=false \
-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2 \
-Xms$XMS -Xmx$XMX \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-jar /usr/local/jetty/start.jar" ]
