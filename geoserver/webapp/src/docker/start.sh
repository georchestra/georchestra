#!/bin/bash
cd /var/local/geoserver
if [ -d .git ]
	then
    echo 'Datadir already initialised'
else
    echo 'Initialising datadir from github'
    git clone https://github.com/georchestra/geoserver_minimal_datadir.git .
    chown -R jetty:jetty /var/local/geoserver
fi
cd /var/lib/jetty
java -Djava.io.tmpdir=/tmp/jetty -DGEOSERVER_DATA_DIR=/var/local/geoserver/ -jar /usr/local/jetty/start.jar
