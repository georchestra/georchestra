FROM jetty:9.3-jre8

RUN apt-get update && \
   apt-get install -y libgdal-java gdal-bin && \
   rm -rf /var/lib/apt/lists/*

ADD . /

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats,gzip

RUN ln -s /usr/share/java/gdal.jar /var/lib/jetty/lib/ext/

VOLUME [ "/var/local/uploads" ]

CMD ["sh", "-c", "exec java -Djava.io.tmpdir=/tmp/jetty -Dgeorchestra.datadir=/etc/georchestra -Dmapfish-print-config=/etc/georchestra/mapfishapp/print/config.yaml -Xmx$XMX -Xms$XMX -jar /usr/local/jetty/start.jar"]

