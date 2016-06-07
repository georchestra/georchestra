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

#### GeoServer without geofence

This creates a ```georchestra/geoserver``` docker image:

```bash
cd geoserver/geoserver-submodule/src
rm -fr ../data/citewfs-1.1/workspaces/sf/sf/E*
LANG=C ../../../mvn clean install -DskipTests
cd ../../webapp
../../mvn clean install docker:build -Pdocker -DskipTests
```

#### GeoServer with geofence

This creates a ```georchestra/geoserver:geofence-15.12``` docker image:

```bash
cd geoserver/geoserver-submodule/src
rm -fr ../data/citewfs-1.1/workspaces/sf/sf/E*
LANG=C ../../../mvn clean install -Pgeofence-server -DskipTests
cd ../../webapp
../../mvn clean install docker:build -Pdocker,geofence -DskipTests
```

#### Geodata container
This creates a ```georchestra/ssh_data``` docker image:

```bash
cd ../../docker/ssh_data
docker build -t georchestra/ssh_data .
```
This image will be used to transfer and store geodata files for geoserver. 
Through composition (docker-compose), those files will be available to all geoserver instances in `/var/local/geodata`. 


These files can also be managed via SSH onto the `georchestra_geodata_1`, eg with:
```
ssh -p 2222 geoserver@localhost 
```
The default password is `geoserver`

```
geoserver@20d925d9072b:~$ ls -al /home/geoserver/data/
total 8
drwxr-xr-x 2 geoserver geoserver 4096 Jan 22 13:10 .
drwxr-xr-x 4 geoserver geoserver 4096 Jan 22 13:10 ..
```
