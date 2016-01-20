# Docker version of georchestra

## Build 

### With maven

Following modules have a maven configuration for building docker images :
 
 * CAS
 * DownloadForm
 * ExtractorApp
 * GeoWebCache
 * Header
 * Ldapadmin
 * MapfishApp
 * Security Proxy

In order to (re)build docker image :

```bash
cd <module>
../mvn -P docker clean package docker:build

```

This will build a 'georchestra/<module>' image

(Replace <module> by module name)


### Not dockerized
Following modules are not already dockerized :

 * Analytics
 * Atlas
 * Catalog App
 * GeoFence
 
### Other custom modules
 
#### Geonetwork 

```bash
cd geonetwork 
../mvn -DskipTests clean install 
cd web
../../mvn -P docker -DskipTests package docker:build
```

#### Geoserver

```bash
cd geoserver/webapp/
../../mvn -P docker clean package docker:build
```

This will create a georchestra/geoserver docker image.

```bash
cd ../../docker/geoserver/shapefile_repo
docker build -t georchestra/shapefile_repo .
../../mvn -P docker clean package docker:build
```

This will create a `georchestra/shapefile_repo` docker image. This image will be used to transfer and store files on 
geoserver. With composition (docker-compose), those files will be available on geoserver instance in 
`/var/local/shapefile`. Files can be modify with SSH protocol on second image in `/home/geoserver/data` folder.



