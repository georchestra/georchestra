# geOrchestra on Docker

## Build 

### With maven

The following modules have a maven configuration to build docker images:
 
 * CAS
 * DownloadForm
 * ExtractorApp
 * GeoWebCache
 * Header
 * Ldapadmin
 * MapfishApp
 * Security Proxy

In order to (re)build a docker image:

```bash
cd <module>
../mvn -P docker clean package docker:build

```

This will build a 'georchestra/<module>' image

(Replace <module> by module name)


### Not dockerized
Following modules are not already dockerized:

 * Analytics
 * Atlas
 * Catalog App
 * GeoFence
 
### Other custom modules
 
#### GeoNetwork 

```bash
cd geonetwork 
../mvn -DskipTests clean install 
cd web
../../mvn -P docker -DskipTests package docker:build
```

#### GeoServer

This creates a ```georchestra/geoserver``` docker image:

```bash
cd geoserver/webapp/
../../mvn -P docker clean package docker:build
```

This creates a ```georchestra/ssh_data``` docker image:

```bash
cd ../../docker/ssh_data
docker build -t georchestra/ssh_data .
```
This image will be used to transfer and store geodata files for geoserver. 
Through composition (docker-compose), those files will be available to all geoserver instances in `/var/local/shapefile`. 
Files can be managed via SSH on second image in `/home/geoserver/data` folder.
