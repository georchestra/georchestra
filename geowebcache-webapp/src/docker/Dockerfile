FROM jetty:9.3-jre8

ADD . /

VOLUME [ "/var/local/tiles" ]

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

ENTRYPOINT [ "/docker-entrypoint.sh" ]

CMD [ "java","-Djava.io.tmpdir=/tmp/jetty", "-Dgeorchestra.datadir=/etc/georchestra",         \
      "-DGEOWEBCACHE_CACHE_DIR=/var/local/tiles","-Xmx1024m","-jar","/usr/local/jetty/start.jar" ]

