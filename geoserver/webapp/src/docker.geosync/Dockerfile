FROM georchestra/geoserver

ENV XMS=1536M XMX=8G

USER root
ADD 02-geosync /docker-entrypoint.d/02-geosync
RUN chmod +x /docker-entrypoint.d/02-geosync

RUN sed -i 's/<\/web-app>/<filter>\n<filter-name>cross-origin<\/filter-name>\n<filter-class>org.eclipse.jetty.servlets.CrossOriginFilter<\/filter-class>\n<\/filter>\n<filter-mapping>\n<filter-name>cross-origin<\/filter-name>\n<url-pattern>\/\*<\/url-pattern>\n<\/filter-mapping>\n<\/web-app>/g' /usr/local/jetty/etc/webdefault.xml

RUN java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=servlets

ADD . /

VOLUME [ "/mnt/geoserver_datadir", "/mnt/geoserver_geodata", "/mnt/geoserver_tiles", "/tmp", "/run/jetty" ]

CMD ["sh", "-c", "exec java -Djava.io.tmpdir=/tmp/jetty \
-Dgeofence-ovr=file:/etc/georchestra/geoserver/geofence/geofence-datasource-ovr.properties \
-DGEOSERVER_DATA_DIR=/mnt/geoserver_datadir \
-DGEOWEBCACHE_CACHE_DIR=/mnt/geoserver_tiles \
-DENABLE_JSONP=true \
-Dorg.geotools.coverage.jaiext.enabled=true \
-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2 \
-Xms$XMS -Xmx$XMX \
-XX:SoftRefLRUPolicyMSPerMB=36000 \
-XX:+UseParallelGC \
-XX:-UsePerfData \
${JAVA_OPTIONS} \
-jar /usr/local/jetty/start.jar" ]


# RUN sed -i 's/threads.max=200/threads.max=50/g' /var/lib/jetty/start.d/server.ini

