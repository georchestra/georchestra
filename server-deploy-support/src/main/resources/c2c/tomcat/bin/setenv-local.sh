# export DSUN_JAVA2D_OPENGL="false"
# export DJAVA_AWT_HEADLESS="true"

EXTRACTOR_DIR=$CATALINA_BASE/data/extractorapp_archives

rm -f $CATALINA_BASE/private/data/lucene/nonspatial/*.lock

export JAVA_XMX="1G"
export JAVA_XX_MAXPERMSIZE="256m"

export ADD_JAVA_OPTS="-DCUSTOM_EPSG_FILE=file://$CATALINA_BASE/conf/epsg.properties -Dorg.geotools.referencing.forceXY=true -Dextractor.storage.dir=$EXTRACTOR_DIR"
export ADD_JAVA_OPTS="$ADD_JAVA_OPTS -Djavax.net.ssl.trustStore=@keystoreFile@ -Djavax.net.ssl.trustStorePassword=@keystorePass@"
export ADD_JAVA_OPTS="$ADD_JAVA_OPTS -XX:CompileCommand=exclude,net/sf/saxon/event/ReceivingContentHandler.startElement"

#export ADD_JAVA_OPTS="$ADD_JAVA_OPTS -Xrunjdwp:transport=dt_socket,address=36367,suspend=n,server=y"