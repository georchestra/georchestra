docker-build: pull-docker-deps dev-docker-build gn3-docker-build geoserver-docker-build georchestra-docker-build

pull-docker-deps:
	docker pull debian:jessie
	docker pull dinkel/openldap 
	docker pull postgres:9.4
	docker pull tianon/apache2

dev-docker-build:
	docker-compose build

gn3-docker-build:
	cd geonetwork; \
	../mvn -DskipTests clean install; \
	cd web; \
	../../mvn -P docker -DskipTests package docker:build

geoserver-docker-build:
	cd geoserver/geoserver-submodule/src; \
	rm -rf ../data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C ../../../mvn clean install -DskipTests; \
	cd ../../webapp; \
	../../mvn clean install docker:build -Pdocker -DskipTests

geoserver-geofence-docker-build:
	cd geoserver/geoserver-submodule/src; \
	rm -fr ../data/citewfs-1.1/workspaces/sf/sf/E*; \
	LANG=C ../../../mvn clean install -Pgeofence-server -DskipTests; \
	cd ../../webapp; \
	../../mvn clean install docker:build -Pdocker,geofence -DskipTests

georchestra-docker-build:
	./mvn clean package docker:build -Pdocker -DskipTests --pl extractorapp,cas-server-webapp,security-proxy,mapfishapp,header,ldapadmin,analytics,catalogapp,downloadform,geowebcache-webapp,atlas
