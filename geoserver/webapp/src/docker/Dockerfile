FROM jetty:9.3-jre8

RUN apt-get update && \
    apt-get install -y unzip && \
    rm -rf /var/lib/apt/lists/*

RUN echo "deb http://httpredir.debian.org/debian jessie contrib" >> /etc/apt/sources.list
RUN echo "deb http://security.debian.org/ jessie/updates contrib" >> /etc/apt/sources.list

RUN apt-get update && \
   apt-get install -y ttf-mscorefonts-installer  && \
   rm -rf /var/lib/apt/lists/*

ADD . /

VOLUME [ "/var/local/geoserver", "/var/local/geodata", "/var/local/tiles" ]

ENTRYPOINT [ "/docker-entrypoint.sh" ]
CMD [ "java","-Djava.io.tmpdir=/tmp/jetty", "-Dgeorchestra.datadir=/etc/georchestra",                     \
      "-DGEOSERVER_DATA_DIR=/var/local/geoserver", "-Dgeofence.dir=/etc/georchestra/geoserver/geofence", \
      "-DGEOWEBCACHE_CACHE_DIR=/var/local/tiles", "-Xmx2048m", "-jar","/usr/local/jetty/start.jar" ]

