#!/bin/bash
mkdir /var/local/geoserver 2> /dev/null
cd /var/local/geoserver
if [ -d .git ]
	then
    echo 'Datadir already initialized'
else
    echo 'Initializing datadir from georchestra/geoserver_minimal_datadir'
    unzip /tmp/datadir.zip -d /tmp
    cp -Rax /tmp/geoserver_minimal_datadir-master/* /var/local/geoserver
    chown -R jetty:jetty /var/local/geoserver
fi
cd /var/lib/jetty
exec java -Djava.io.tmpdir=/tmp/jetty -DGEOSERVER_DATA_DIR=/var/local/geoserver/ -jar /usr/local/jetty/start.jar
