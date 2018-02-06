# Docker related targets

docker-pull-jetty:
	docker pull jetty:9-jre8

docker-build-ldap:
	docker pull dinkel/openldap
	docker-compose build ldap

docker-build-database:
	docker pull postgres:10
	docker-compose build database

docker-build-gn3: docker-pull-jetty
	cd geonetwork; \
	../mvn -DskipTests clean install; \
	cd web; \
	../../mvn -P docker -DskipTests package docker:build

docker-build-geoserver: docker-pull-jetty
	cd geoserver/; \
	rm -rf geoserver-submodule/data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C ../mvn clean install -DskipTests; \
	cd webapp; \
	../../mvn clean install docker:build -Pdocker,colormap,mbtiles,wps-download,app-schema,control-flow,csw,feature-pregeneralized,gdal,importer,inspire,libjpeg-turbo,monitor,pyramid,wps -DskipTests

docker-build-geoserver-geofence: docker-pull-jetty
	cd geoserver; \
	rm -fr geoserver-submodule/data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C ../mvn clean install -Pgeofence -DskipTests; \
	cd webapp; \
	../../mvn clean install docker:build -Pdocker,colormap,mbtiles,wps-download,app-schema,control-flow,csw,feature-pregeneralized,gdal,importer,inspire,libjpeg-turbo,monitor,pyramid,wps,geofence -DskipTests

docker-build-ldapadmin: docker-pull-jetty
	./mvn clean package docker:build -Pdocker -DskipTests --pl ldapadmin

docker-build-georchestra: docker-pull-jetty docker-build-database docker-build-ldap docker-build-geoserver docker-build-gn3
	./mvn clean package docker:build -Pdocker -DskipTests --pl extractorapp,cas-server-webapp,security-proxy,mapfishapp,header,ldapadmin,analytics,geowebcache-webapp,atlas

docker-build-dev:
	docker pull debian:stretch
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
	../../../mvn clean install -Pcolormap,mbtiles,wps-download,app-schema,control-flow,csw,feature-pregeneralized,gdal,importer,inspire,libjpeg-turbo,monitor,pyramid,wps,css -DskipTests; \
	cd ../../..; \
	./mvn clean install -pl geoserver/webapp

war-build-geoserver-geofence: build-deps
	cd geoserver/geoserver-submodule/src/; \
	../../../mvn clean install -Pcolormap,mbtiles,wps-download,app-schema,control-flow,csw,feature-pregeneralized,gdal,importer,inspire,libjpeg-turbo,monitor,pyramid,wps,css,geofence-server -DskipTests; \
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
	./mvn package deb:package -pl atlas,cas-server-webapp,security-proxy,header,mapfishapp,extractorapp,analytics,geoserver/webapp,ldapadmin,geonetwork/web,geowebcache-webapp -PdebianPackage -DskipTests

# Base geOrchestra config and common modules
build-deps:
	./mvn -Dmaven.test.failure.ignore clean install --non-recursive
	./mvn clean install -pl config -Dmaven.javadoc.failOnError=false
	./mvn clean install -pl commons,epsg-extension,ogc-server-statistics -Dmaven.javadoc.failOnError=false
	cd config/; \
	../mvn -Dserver=template install

# all
all: war-build-georchestra deb-build-georchestra docker-build
