FROM jetty:latest

ADD . /

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

RUN chown -R jetty:jetty /var/local/geoserver/

CMD ["java","-Djava.io.tmpdir=/tmp/jetty", "-DGEOSERVER_DATA_DIR=/var/local/geoserver/","-jar","/usr/local/jetty/start.jar"]

