FROM jetty:9-jre8

ENV XMS=1536M XMX=8G

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats,jndi

ADD . /

# Temporary switch to root
USER root

RUN echo "deb http://httpredir.debian.org/debian stretch main contrib" > /etc/apt/sources.list \
    && echo "deb http://security.debian.org/ stretch/updates main contrib" >> /etc/apt/sources.list \
    && echo "ttf-mscorefonts-installer msttcorefonts/accepted-mscorefonts-eula select true" | debconf-set-selections \
    && apt-get update \
    && apt-get install -y ttf-mscorefonts-installer \
    && apt-get clean \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /mnt/geoserver_datadir /mnt/geoserver_geodata /mnt/geoserver_tiles && \
    chown jetty:jetty /etc/georchestra /mnt/geoserver_datadir /mnt/geoserver_geodata /mnt/geoserver_tiles

RUN sed -i 's/threads.max=200/threads.max=50/g' /var/lib/jetty/start.d/server.ini

# Restore jetty user
USER jetty

VOLUME [ "/mnt/geoserver_datadir", "/mnt/geoserver_geodata", "/mnt/geoserver_tiles", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java -Djava.io.tmpdir=/tmp/jetty \
-Dgeofence-ovr=file:/etc/georchestra/geoserver/geofence/geofence-datasource-ovr.properties \
-DGEOSERVER_DATA_DIR=/mnt/geoserver_datadir \
-DGEOWEBCACHE_CACHE_DIR=/mnt/geoserver_tiles \
-DENABLE_JSONP=true \
-Dorg.geotools.coverage.jaiext.enabled=true \
-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2 \
-Xms$XMS -Xmx$XMX \
-XX:SoftRefLRUPolicyMSPerMB=36000 \
-XX:+UseParallelGC \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-jar /usr/local/jetty/start.jar" ]
