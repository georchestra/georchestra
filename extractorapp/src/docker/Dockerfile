FROM jetty:9.3-jre8

ADD . /

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

VOLUME [ "/var/local/extracts" ]

ENTRYPOINT [ "/docker-entrypoint.sh" ]
CMD ["java","-Djava.io.tmpdir=/tmp/jetty", "-Dgeorchestra.datadir=/etc/georchestra", "-Dextractor.storage.dir=/var/local/extracts", "-jar","/usr/local/jetty/start.jar"]

