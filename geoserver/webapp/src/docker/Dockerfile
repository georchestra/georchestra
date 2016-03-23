FROM jetty:9.3-jre8


RUN apt-get update && \
    apt-get install -y unzip && \
    rm -rf /var/lib/apt/lists/*

ADD . /
ADD https://github.com/georchestra/geoserver_minimal_datadir/archive/15.12.zip /tmp/datadir.zip

VOLUME [ "/var/local/geoserver", "/var/local/geodata", "/var/local/geowebcache" ]

ENTRYPOINT [ "/docker-entrypoint.sh" ]
CMD [ "java","-Djava.io.tmpdir=/tmp/jetty", "-Dgeorchestra.datadir=/etc/georchestra",                     \
      "-DGEOSERVER_DATA_DIR=/var/local/geoserver/", "-Dgeofence.dir=/etc/georchestra/geoserver/geofence", \
      "-DGEOWEBCACHE_CACHE_DIR=/var/local/geowebcache", "-jar","/usr/local/jetty/start.jar" ]

