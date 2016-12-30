# Building your own docker images for geOrchestra

Usually, you don't have to do this, since it is already taken care of by our CI, which pushes the latest images to docker hub.

## PostGreSQL

```bash
cd postgresql
docker build -t georchestra/database .
```

## LDAP 

```bash
cd ldap
docker build -t georchestra/ldap .
```

## Webapps

### GeoNetwork 3

```bash
cd geonetwork 
../mvn -DskipTests clean install 
cd web
../../mvn -P docker -DskipTests package docker:build
```

### GeoServer without geofence

This creates a ```georchestra/geoserver``` docker image:

```bash
cd geoserver/geoserver-submodule/src
rm -fr ../data/citewfs-1.1/workspaces/sf/sf/E*
LANG=C ../../../mvn clean install -DskipTests
cd ../../webapp
../../mvn clean install docker:build -Pdocker -DskipTests
```

### GeoServer with geofence

This creates a ```georchestra/geoserver:geofence``` docker image:

```bash
cd geoserver/geoserver-submodule/src
rm -fr ../data/citewfs-1.1/workspaces/sf/sf/E*
LANG=C ../../../mvn clean install -Pgeofence-server -DskipTests
cd ../../webapp
../../mvn clean install docker:build -Pdocker,geofence -DskipTests
```


### Other webapps

From the project root:
```bash
./mvn clean package docker:build -Pdocker -DskipTests --pl extractorapp,cas-server-webapp,security-proxy,mapfishapp,header,ldapadmin,analytics,catalogapp,downloadform,geowebcache-webapp
```


## Other complementary modules

### Geodata upload

This creates a ```georchestra/ssh_data``` docker image, which will be useful to transfer geodata into the SDI:

```bash
cd ../../docker/ssh_data
docker build -t georchestra/ssh_data .
```

These files can be managed through SSH / rsync, eg with:
```bash
ssh -p 2222 geoserver@localhost 
```
The default password is `geoserver`.

File should be transfered to the `/mnt/geoserver_geodata/` folder:
```bash
geoserver@20d925d9072b:~$ ls -al /mnt/geoserver_geodata/
total 8
drwxr-xr-x 2 geoserver geoserver 4096 Jan 22 13:10 .
drwxr-xr-x 4 geoserver geoserver 4096 Jan 22 13:10 ..
```

They will be made available to all geoserver instances in `/mnt/geoserver_geodata`. 
