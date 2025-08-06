# Docker related targets
BTAG=latest

docker-pull-jetty:
	docker pull jetty:9-jre11

docker-build-ldap:
	docker pull debian:bookworm
	cd ldap; \
	docker build -t georchestra/ldap:${BTAG} .

docker-build-ldap-withrotation:
	docker pull debian:bookworm
	cd ldap; \
	docker build -t georchestra/ldap:${BTAG} --build-arg PM_POLICY=rotation .

docker-build-database:
	docker pull postgres:15
	cd postgresql; \
	docker build -t georchestra/database:${BTAG} .

docker-build-geowebcache: docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests -pl geowebcache-webapp

docker-build-proxy: build-deps docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl security-proxy

docker-build-console: build-deps docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl console

docker-build-analytics: build-deps docker-pull-jetty
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl analytics

docker-build-georchestra: build-deps docker-pull-jetty docker-build-database docker-build-ldap docker-build-geowebcache
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl security-proxy,console,analytics

docker-build: docker-build-georchestra


# WAR related targets
war-build-geowebcache: build-deps
	mvn clean install -pl geowebcache-webapp -DskipTests -Dfmt.skip=true

war-build-georchestra:
	mvn -Dmaven.test.skip=true -DskipTests clean install


# DEB related targets
deb-build-geowebcache: war-build-geowebcache
	mvn package deb:package -pl geowebcache-webapp -PdebianPackage -DskipTests -Dfmt.skip=true ${DEPLOY_OPTS}

deb-build-georchestra: war-build-georchestra build-deps deb-build-geowebcache
	mvn package deb:package -pl security-proxy,analytics,console -PdebianPackage -DskipTests ${DEPLOY_OPTS}

# Base geOrchestra common modules
build-deps:
	mvn -Dmaven.test.failure.ignore clean install --non-recursive
	mvn clean install -pl commons -Dmaven.javadoc.failOnError=false

# all
all: war-build-georchestra deb-build-georchestra docker-build
