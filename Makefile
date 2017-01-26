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

docker-build-gn3: docker-pull-jetty-jre8
	cd geonetwork; \
	../mvn -DskipTests clean install; \
	cd web; \
	../../mvn -P docker -DskipTests package docker:build

docker-build-geoserver: docker-pull-jetty-jre7
	cd geoserver/geoserver-submodule/src; \
	rm -rf ../data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C ../../../mvn clean install -DskipTests; \
	cd ../../webapp; \
	../../mvn clean install docker:build -Pdocker -DskipTests

docker-build-geoserver-geofence: docker-pull-jetty-jre7
	cd geoserver/geoserver-submodule/src; \
	rm -fr ../data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C ../../../mvn clean install -Pgeofence-server -DskipTests; \
	cd ../../webapp; \
	../../mvn clean install docker:build -Pdocker,geofence -DskipTests

docker-build-georchestra: docker-pull-jetty-jre8 docker-build-database docker-build-ldap docker-build-geoserver docker-build-gn3
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

docker-build: docker-build-dev docker-build-gn3 docker-build-geoserver docker-build-georchestra

all: docker-build
