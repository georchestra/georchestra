# Docker related targets

docker-pull-jetty-jre7:
	docker pull jetty:9.2-jre7

docker-pull-jetty-jre8:
	docker pull jetty:9.3-jre8

docker-build-ldap:
	docker pull dinkel/openldap
	docker-compose build ldap

docker-build-database:
	docker pull postgres:9.4
	docker-compose build database

docker-build-gn3: build-deps docker-pull-jetty-jre8
	cd geonetwork; \
	../mvn -DskipTests clean install; \
	cd web; \
	../../mvn -P docker -DskipTests package docker:build

docker-build-geoserver: build-deps docker-pull-jetty-jre7
	cd geoserver/geoserver-submodule/src; \
	rm -rf ../data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C ../../../mvn clean install -DskipTests; \
	cd ../../webapp; \
	../../mvn clean install docker:build -Pdocker -DskipTests

docker-build-geoserver-geofence: build-deps docker-pull-jetty-jre7
	cd geoserver/geoserver-submodule/src; \
	rm -fr ../data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C ../../../mvn clean install -Pgeofence-server -DskipTests; \
	cd ../../webapp; \
	../../mvn clean install docker:build -Pdocker,geofence -DskipTests

docker-build-proxy: build-deps docker-pull-jetty-jre8
	./mvn clean package docker:build -Pdocker -DskipTests --pl security-proxy

docker-build-georchestra: build-deps docker-pull-jetty-jre8 docker-build-database docker-build-ldap docker-build-geoserver docker-build-gn3
	./mvn clean package docker:build -Pdocker -DskipTests --pl extractorapp,cas-server-webapp,security-proxy,mapfishapp,header,ldapadmin,analytics,catalogapp,downloadform,geowebcache-webapp,atlas

docker-build-dev:
	docker pull debian:jessie
	docker pull tianon/apache2
	docker-compose build smtp courier-imap webmail geodata

docker-stop-rm:
	docker-compose stop
	docker-compose rm -f

docker-clean-volumes:
	docker-compose down --volumes --remove-orphans

docker-clean-images:
	docker-compose down --rmi 'all' --remove-orphans

docker-clean-all:
	docker-compose down --volumes --rmi 'all' --remove-orphans

docker-build: build-deps docker-build-dev docker-build-gn3 docker-build-geoserver docker-build-georchestra


# WAR related targets

war-build-geoserver: build-deps
	cd geoserver/geoserver-submodule/src/; \
	../../../mvn clean install -Pcontrol-flow,css,csw,gdal,inspire,pyramid,wps -DskipTests; \
	cd ../../..; \
	./mvn clean install -pl geoserver/webapp

war-build-geoserver-geofence: build-deps
	cd geoserver/geoserver-submodule/src/; \
	../../../mvn clean install -Pcontrol-flow,css,csw,gdal,inspire,pyramid,wps,geofence-server -DskipTests; \
	cd ../../..; \
	./mvn clean install -pl geoserver/webapp

war-build-gn3:
	./mvn clean install -f geonetwork/pom.xml -DskipTests

war-build-georchestra: war-build-gn3 war-build-geoserver
	./mvn -Dmaven.test.skip=true clean install


# DEB related targets

deb-build-geoserver: war-build-geoserver
	cd geoserver; \
	../mvn clean package deb:package -PdebianPackage --pl webapp

deb-build-geoserver-geofence: war-build-geoserver-geofence
	cd geoserver; \
	../mvn clean package deb:package -PdebianPackage --pl webapp

deb-build-georchestra: war-build-georchestra build-deps deb-build-geoserver
	./mvn package deb:package -pl atlas,catalogapp,cas-server-webapp,downloadform,security-proxy,header,mapfishapp,extractorapp,analytics,geoserver/webapp,ldapadmin,geonetwork/web,geowebcache-webapp -PdebianPackage -DskipTests

# Base geOrchestra config and common modules
build-deps:
	./mvn -Dmaven.test.failure.ignore clean install --non-recursive
	./mvn clean install -pl config -Dmaven.javadoc.failOnError=false
	./mvn clean install -pl commons,epsg-extension,ogc-server-statistics -Dmaven.javadoc.failOnError=false
	cd config/; \
	../mvn -Dserver=template install

# all
all: war-build-georchestra deb-build-georchestra docker-build
