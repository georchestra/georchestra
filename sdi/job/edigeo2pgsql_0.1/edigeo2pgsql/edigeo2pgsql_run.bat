%~d0
 cd %~dp0
java -Xms256M -Xmx1024M -cp classpath.jar; drebretagne_geobretagne.edigeo2pgsql_0_1.edigeo2pgsql --context=Default %* 