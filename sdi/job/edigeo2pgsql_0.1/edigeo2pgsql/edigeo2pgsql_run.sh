cd `dirname $0`
 ROOT_PATH=`pwd`
java -Xms256M -Xmx1024M -cp classpath.jar: drebretagne_geobretagne.edigeo2pgsql_0_1.edigeo2pgsql --context=Default $* 