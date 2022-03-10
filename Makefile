# Docker related targets

GEOSERVER_EXTENSION_PROFILES=colormap,mbtiles,wps-download,app-schema,control-flow,csw,inspire,libjpeg-turbo,monitor,pyramid,wps,css,s3-geotiff,jp2k,authkey,mapstore2,mbstyle,ogcapi,web-resource,flatgeobuf,sldservice
BTAG=latest

docker-pull-jetty:
	docker pull jetty:9-jre11

docker-build-ldap:
	docker pull debian:buster
	cd ldap; \
	docker build -t georchestra/ldap:${BTAG} .

docker-build-database:
	docker pull postgres:13
	cd postgresql; \
	docker build -t georchestra/database:${BTAG} .

docker-build-gn: docker-pull-jetty
	mvn install -pl security-proxy-spring-integration --also-make -DskipTests; \
	cd geonetwork; \
	mvn -DskipTests clean install; \
	cd web; \
	mvn -P docker -DskipTests package docker:build -DdockerImageTags=${BTAG}

docker-build-geoserver: docker-pull-jetty
	cd geoserver; \
	LANG=C mvn clean install -DskipTests -Dfmt.skip=true -P${GEOSERVER_EXTENSION_PROFILES}; \
	cd webapp; \
	mvn clean install docker:build -DdockerImageTags=${BTAG} -Pdocker,${GEOSERVER_EXTENSION_PROFILES} -DskipTests

docker-build-geoserver-geofence: docker-pull-jetty
	cd geoserver; \
	LANG=C mvn clean install -DskipTests -Dfmt.skip=true -Pgeofence-server,${GEOSERVER_EXTENSION_PROFILES} ; \
	cd webapp; \
	mvn clean install docker:build -DdockerImageTags=${BTAG} -Pdocker,geofence,${GEOSERVER_EXTENSION_PROFILES} -DskipTests

docker-build-geowebcache: docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests -pl geowebcache-webapp

docker-build-proxy: build-deps docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl security-proxy

docker-build-console: build-deps docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl console

docker-build-analytics: build-deps docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl analytics

docker-build-mapfishapp: build-deps docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl mapfishapp

docker-build-datafeeder: build-deps
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl datafeeder

docker-build-georchestra: build-deps docker-pull-jetty docker-build-database docker-build-ldap docker-build-geoserver docker-build-geowebcache docker-build-gn
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl extractorapp,security-proxy,mapfishapp,header,console,analytics,atlas,datafeeder

docker-build: docker-build-gn docker-build-geoserver docker-build-georchestra


# WAR related targets

war-build-geoserver: build-deps
	cd geoserver; \
	LANG=C mvn clean install -DskipTests -Dfmt.skip=true -P${GEOSERVER_EXTENSION_PROFILES}; \
	cd ..; \
	mvn clean install -pl geoserver/webapp -P${GEOSERVER_EXTENSION_PROFILES}

war-build-geoserver-geofence: build-deps
	cd geoserver; \
	LANG=C mvn clean install -DskipTests -Dfmt.skip=true -P${GEOSERVER_EXTENSION_PROFILES},geofence; \
	cd ..; \
	mvn clean install -pl geoserver/webapp -P${GEOSERVER_EXTENSION_PROFILES}

war-build-geowebcache: build-deps
	mvn clean install -pl geowebcache-webapp -DskipTests -Dfmt.skip=true

war-build-gn: build-deps
	mvn clean install -pl testcontainers,ldap-account-management,security-proxy-spring-integration -DskipTests
	mvn clean install -f geonetwork/pom.xml -DskipTests

war-build-georchestra: war-build-gn war-build-geoserver
	mvn -Dmaven.test.skip=true -DskipTests clean install


# DEB related targets

deb-build-geoserver: war-build-geoserver
	cd geoserver; \
	mvn clean package deb:package -pl webapp -PdebianPackage,${GEOSERVER_EXTENSION_PROFILES} ${DEPLOY_OPTS}

deb-build-geoserver-geofence: war-build-geoserver-geofence
	cd geoserver; \
	mvn clean package deb:package -pl webapp -PdebianPackage,geofence,${GEOSERVER_EXTENSION_PROFILES} ${DEPLOY_OPTS}

deb-build-geowebcache: war-build-geowebcache
	mvn package deb:package -pl geowebcache-webapp -PdebianPackage -DskipTests -Dfmt.skip=true ${DEPLOY_OPTS}

deb-build-georchestra: war-build-georchestra build-deps deb-build-geoserver deb-build-geowebcache
	mvn package deb:package -pl atlas,datafeeder,datafeeder-ui,security-proxy,header,mapfishapp,extractorapp,analytics,console,geonetwork/web -PdebianPackage -DskipTests ${DEPLOY_OPTS}

# Base geOrchestra common modules
build-deps:
	mvn -Dmaven.test.failure.ignore clean install --non-recursive
	mvn clean install -pl commons,ogc-server-statistics -Dmaven.javadoc.failOnError=false

# all
all: war-build-georchestra deb-build-georchestra docker-build
