FROM jetty:9.4-jre11

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=jmx,jmx-remote,stats,jndi,http-forwarded

COPY --chown=jetty:jetty . /

USER root

RUN apt-get update \
    && echo ttf-mscorefonts-installer msttcorefonts/accepted-mscorefonts-eula select true | debconf-set-selections \
    && apt-get install -y ttf-mscorefonts-installer \
    && apt-get install -y gsfonts fonts-deva fonts-font-awesome fonts-freefont-ttf fonts-material-design-icons-iconfont fonts-materialdesignicons-webfont fonts-roboto\
    && apt-get install -y wget \
    && apt-get clean \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

RUN wget https://downloads.sourceforge.net/project/libjpeg-turbo/2.0.5/libjpeg-turbo-official_2.0.5_amd64.deb \
    && dpkg -i libjpeg-turbo-official_2.0.5_amd64.deb \
    && apt-get -f install \
    && rm libjpeg-turbo-official_2.0.5_amd64.deb

RUN mkdir /mnt/geoserver_datadir /mnt/geoserver_geodata /mnt/geoserver_tiles /mnt/geoserver_native_libs && \
    chown jetty:jetty /etc/georchestra /mnt/geoserver_datadir /mnt/geoserver_geodata /mnt/geoserver_tiles /mnt/geoserver_native_libs

USER jetty

# add a tweaked configuration. First use case is support of OPTIONS.
COPY --chown=jetty:jetty web.xml /var/lib/jetty/webapps/geoserver/WEB-INF/web.xml

VOLUME [ "/mnt/geoserver_datadir", "/mnt/geoserver_geodata", "/mnt/geoserver_tiles", "/mnt/geoserver_native_libs", "/tmp", "/run/jetty" ]

ENTRYPOINT [ "/docker-entrypoint.sh" ]

ENV LD_LIBRARY_PATH=/mnt/geoserver_native_libs:/opt/libjpeg-turbo/lib64

CMD ["sh", "-c", "exec java -Djava.io.tmpdir=/tmp/jetty \
-Dgeofence-ovr=file:/etc/georchestra/geoserver/geofence/geofence-datasource-ovr.properties \
-Dgwc.context.suffix=gwc \
-Dgeorchestra.datadir=/etc/georchestra \
-DGEOSERVER_DATA_DIR=/mnt/geoserver_datadir \
-DGEOWEBCACHE_CACHE_DIR=/mnt/geoserver_tiles \
-DGEOWEBCACHE_CONFIG_DIR=/mnt/geoserver_datadir/gwc \
-Dhttps.protocols=TLSv1.2,TLSv1.3 \
-XX:MaxRAMPercentage=80 -XX:+UseParallelGC \
-XX:SoftRefLRUPolicyMSPerMB=36000 \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-Djetty.httpConfig.sendServerVersion=false \
-Djetty.jmxremote.rmiregistryhost=${JMX_HOST:-127.0.0.1} \
-Djetty.jmxremote.rmiserverhost=${JMX_HOST:-127.0.0.1} \
-Dcom.sun.management.jmxremote=${JMX_ENABLE:-false} \
-Dcom.sun.management.jmxremote.host=${JMX_HOST:-127.0.0.1} \
-Dcom.sun.management.jmxremote.ssl=${JMX_SSL:-false} \
-Dcom.sun.management.jmxremote.authenticate=${JMX_AUTHENTICATE:-false} \
-jar /usr/local/jetty/start.jar" ]

ARG GS_VERSION
ENV GS_VERSION=$GS_VERSION
