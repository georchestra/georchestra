Pour construire le war (geoserver.war) avec la securisation par les entêtes émis par le security-proxy.

Téléchargez les sources de geoserver-2.1.x:

svn co http://svn.codehaus.org/geoserver/branches/2.1.x geoserver-2.1.x
cd geoserver-2.1.x/src
mvn -Dmaven.test.skip=true install

Ensuite dans ce repertoire:

mvn -o -Dmaven.test.skip=true install
