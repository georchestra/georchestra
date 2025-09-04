# Docker related targets
BTAG=latest

docker-build-ldap:
	cd ldap; \
	docker build -t georchestra/ldap:${BTAG} .

docker-build-ldap-withrotation:
	cd ldap; \
	docker build -t georchestra/ldap:${BTAG} --build-arg PM_POLICY=rotation .

docker-build-database:
	cd postgresql; \
	docker build -t georchestra/database:${BTAG} .

docker-build-console: build-deps
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl console

docker-build-georchestra: build-deps docker-build-database docker-build-ldap
	mvn clean package docker:build -DdockerImageTags=${BTAG} -Pdocker -DskipTests --pl console

docker-build: docker-build-georchestra

# WAR related targets
war-build-georchestra:
	mvn -Dmaven.test.skip=true -DskipTests clean install

# DEB related targets
deb-build-georchestra: war-build-georchestra build-deps
	mvn package deb:package -pl console -PdebianPackage -DskipTests ${DEPLOY_OPTS}

# Base geOrchestra common modules
build-deps:
	mvn -Dmaven.test.failure.ignore clean install --non-recursive
	mvn clean install -pl commons -Dmaven.javadoc.failOnError=false

# all
all: war-build-georchestra deb-build-georchestra docker-build
