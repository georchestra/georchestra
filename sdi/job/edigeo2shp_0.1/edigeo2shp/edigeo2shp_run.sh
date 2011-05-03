cd `dirname $0`
ROOT_PATH=`pwd`
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -Xms256M -Xmx1024M -cp classpath.jar: drebretagne_geobretagne.edigeo2shp_0_1.edigeo2shp --context=Default $* 