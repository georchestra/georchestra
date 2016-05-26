FROM jetty:9.3-jre8

ADD . /

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

RUN apt-get update && \
   apt-get install -y libgdal-java gdal-bin && \
   rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/share/java/gdal.jar /var/lib/jetty/lib/ext/


VOLUME [ "/var/local/uploads" ]

ENTRYPOINT [ "/docker-entrypoint.sh" ]
CMD ["java","-Djava.io.tmpdir=/tmp/jetty", "-Dgeorchestra.datadir=/etc/georchestra","-Xmx1024m","-jar","/usr/local/jetty/start.jar"]

