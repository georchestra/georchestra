FROM jetty:9.3-jre8

ADD . /

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

RUN apt-get update && \
   apt-get install -y libgdal-java gdal-bin && \
   rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/share/java/gdal.jar /var/lib/jetty/lib/ext/

VOLUME [ "/var/local/extracts", "/var/local/extractorapp_tmp" ]

CMD ["sh", "-c", "exec java -Djava.io.tmpdir=/var/local/extractorapp_tmp/jetty -Djava.util.prefs.userRoot=/var/local/extractorapp_tmp/userPrefs -Djava.util.prefs.systemRoot=/var/local/extractorapp_tmp/systemPrefs -Dgeorchestra.datadir=/etc/georchestra -Dextractor.storage.dir=/var/local/extracts -Xmx$XMX -Xms$XMX -jar /usr/local/jetty/start.jar"]
