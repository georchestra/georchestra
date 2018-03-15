FROM jetty:9-jre8

ENV XMS=1G XMX=2G

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=jmx,jmx-remote,stats

ADD . /

# Temporary switch to root
USER root

# add non-free for imageio
RUN echo "deb http://ftp.fr.debian.org/debian stretch contrib non-free" >> /etc/apt/sources.list && \
    echo "deb http://security.debian.org/ stretch/updates contrib non-free" >> /etc/apt/sources.list && \
    apt-get update && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir /mnt/geowebcache_tiles && \
    chown jetty:jetty /etc/georchestra /mnt/geowebcache_tiles

# restore jetty user
USER jetty

VOLUME [ "/mnt/geowebcache_tiles", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java \
-Djava.io.tmpdir=/tmp/jetty \
-Dgeorchestra.datadir=/etc/georchestra \
-DGEOWEBCACHE_CACHE_DIR=/mnt/geowebcache_tiles \
-Xms$XMS -Xmx$XMX \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-jar /usr/local/jetty/start.jar"]
