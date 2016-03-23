FROM jetty:9.3-jre8


RUN apt-get update && \
    apt-get install -y unzip && \
    rm -rf /var/lib/apt/lists/*

ADD . /
ADD https://github.com/georchestra/geoserver_minimal_datadir/archive/master.zip /tmp/datadir.zip

VOLUME [ "/var/local/geoserver", "/var/local/geodata" ]

ENTRYPOINT [ "/docker-entrypoint.sh" ]
CMD [ "java","-Djava.io.tmpdir=/tmp/jetty", "-Dgeorchestra.datadir=/etc/georchestra",                     \
      "-DGEOSERVER_DATA_DIR=/var/local/geoserver/", "-Dgeofence.dir=/etc/georchestra/geoserver/geofence", \
      "-jar","/usr/local/jetty/start.jar" ]

