# Docker related targets

GEOSERVER_EXTENSION_PROFILES=colormap,mbtiles,wps-download,app-schema,control-flow,csw,feature-pregeneralized,gdal,importer,inspire,libjpeg-turbo,monitor,pyramid,wps,css,s3-geotiff
BTAG=latest

docker-pull-jetty:
	docker pull jetty:9-jre8

docker-build-ldap:
	docker pull debian:stretch
	cd ldap; \
	docker build -t georchestra/ldap:${BTAG} .

docker-build-database:
	docker pull postgres:11
	cd postgresql; \
	docker build -t georchestra/database:${BTAG} .

docker-build-gn3: docker-pull-jetty
	cd geonetwork; \
	mvn -DskipTests clean install; \
	cd web; \
	mvn -P docker -DskipTests package docker:build

docker-build-geoserver: docker-pull-jetty
	cd geoserver; \
	rm -rf geoserver-submodule/data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C mvn clean install -DskipTests -Dfmt.skip=true -P${GEOSERVER_EXTENSION_PROFILES}; \
	cd webapp; \
	mvn clean install docker:build -Pdocker,${GEOSERVER_EXTENSION_PROFILES} -DskipTests

docker-build-geoserver-geofence: docker-pull-jetty
	cd geoserver; \
	rm -fr geoserver-submodule/data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C mvn clean install -DskipTests -Dfmt.skip=true -Pgeofence-server,${GEOSERVER_EXTENSION_PROFILES} ; \
	cd webapp; \
	mvn clean install docker:build -Pdocker,geofence,${GEOSERVER_EXTENSION_PROFILES} -DskipTests

docker-build-proxy: build-deps docker-pull-jetty
	mvn clean package docker:build -Pdocker -DskipTests --pl security-proxy

docker-build-console: build-deps docker-pull-jetty
	mvn clean package docker:build -Pdocker -DskipTests --pl console

docker-build-mapfishapp: build-deps docker-pull-jetty
	mvn clean package docker:build -Pdocker -DskipTests --pl mapfishapp

docker-build-georchestra: build-deps docker-pull-jetty docker-build-database docker-build-ldap docker-build-geoserver docker-build-gn3
	mvn clean package docker:build -Pdocker -DskipTests --pl extractorapp,cas-server-webapp,security-proxy,mapfishapp,header,console,analytics,geowebcache-webapp,atlas

docker-build-smtp:
	docker pull debian:stretch
	cd docker/smtp/smtp-sink; \
	docker build -t camptocamp/smtp-sink .

docker-build-imap:
	docker pull debian:stretch
	cd docker/smtp/courier-imap; \
	docker build -t camptocamp/courier-imap .

docker-build-webmail:
	docker pull debian:stretch
	cd docker/smtp/webmail; \
	docker build -t camptocamp/sqwebmail .

docker-build-geodata:
	docker pull debian:stretch
	cd docker/ssh_data; \
	docker build -t georchestra/ssh_data .

docker-build-dev: docker-build-smtp docker-build-imap docker-build-webmail docker-build-geodata

docker-build: docker-build-dev docker-build-gn3 docker-build-geoserver docker-build-georchestra


# WAR related targets

war-build-geoserver: build-deps
	cd geoserver/geoserver-submodule/src/; \
	mvn clean install -DskipTests -Dfmt.skip=true -P${GEOSERVER_EXTENSION_PROFILES} ; \
	cd ../../..; \
	mvn clean install -pl geoserver/webapp -P${GEOSERVER_EXTENSION_PROFILES}

war-build-geoserver-geofence: build-deps
	cd geoserver/geoserver-submodule/src/; \
	mvn clean install -DskipTests -Dfmt.skip=true -Dserver=geofence-generic -Pgeofence-server,${GEOSERVER_EXTENSION_PROFILES} ; \
	cd ../../..; \
	mvn clean install -pl geoserver/webapp -Pgeofence,${GEOSERVER_EXTENSION_PROFILES}

war-build-gn3:
	mvn clean install -f geonetwork/pom.xml -DskipTests

war-build-georchestra: war-build-gn3 war-build-geoserver
	mvn -Dmaven.test.skip=true clean install


# DEB related targets

deb-build-geoserver: war-build-geoserver
	cd geoserver; \
	mvn clean package deb:package -pl webapp -PdebianPackage,${GEOSERVER_EXTENSION_PROFILES}

deb-build-geoserver-geofence: war-build-geoserver-geofence
	cd geoserver; \
	mvn clean package deb:package -pl webapp -PdebianPackage,geofence,${GEOSERVER_EXTENSION_PROFILES}

deb-build-georchestra: war-build-georchestra build-deps deb-build-geoserver
	mvn package deb:package -pl atlas,cas-server-webapp,security-proxy,header,mapfishapp,extractorapp,analytics,geoserver/webapp,console,geonetwork/web,geowebcache-webapp -PdebianPackage -DskipTests

# Base geOrchestra common modules
build-deps:
	mvn -Dmaven.test.failure.ignore clean install --non-recursive
	mvn clean install -pl commons,epsg-extension,ogc-server-statistics -Dmaven.javadoc.failOnError=false

# all
all: war-build-georchestra deb-build-georchestra docker-build
