FROM jetty:9.3-jre8

ENV XMS=1G XMX=2G

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

# add non-free for imageio
RUN echo "deb http://ftp.fr.debian.org/debian jessie contrib non-free" >> /etc/apt/sources.list && \
    echo "deb http://security.debian.org/ jessie/updates contrib non-free" >> /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y libjai-core-java libjai-imageio-core-java && \
    rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/share/java/jai_core.jar /var/lib/jetty/lib/ext/ && \
    ln -s /usr/share/java/jai_codec.jar /var/lib/jetty/lib/ext/ && \
    ln -s /usr/share/java/mlibwrapper_jai.jar /var/lib/jetty/lib/ext/ && \
    ln -s /usr/share/java/jai_imageio.jar /var/lib/jetty/lib/ext/ && \
    ln -s /usr/share/java/clibwrapper_jiio.jar /var/lib/jetty/lib/ext/

ADD . /

VOLUME [ "/mnt/geoserver_tiles", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-DGEOWEBCACHE_CACHE_DIR=/mnt/geoserver_tiles \
-Xms$XMS -Xmx$XMX \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-jar /usr/local/jetty/start.jar"]
