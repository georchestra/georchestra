# How to run geOrchestra on Docker

## 3 easy steps

**First step**

Download the `docker-compose.yml` file:
```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/docker-compose.yml
```
This file describes:
 * which images / webapps will run,
 * how they are linked together,
 * where the configuration and data volumes are

Feel free to comment out the apps you do not need.


**Second step**

Create a configuration directory on your host machine:
```
sudo mkdir /etc/georchestra
sudo chown unprivileged_user /etc/georchestra
git clone -b docker-master https://github.com/georchestra/datadir.git /etc/georchestra
```

Optional, but recommended: as `unprivileged_user`, adjust the configuration in `/etc/georchestra` according to your needs.


**Third step**

Run geOrchestra with
```
docker-compose up
```

Open [http://localhost:8080/header/](http://localhost:8080/header/) in your browser.  

To login, use these credentials:
 * `testuser` / `testuser`
 * `testadmin` / `testadmin`

To upload data into the GeoServer data volume (`geoserver_geodata`), use rsync:
```
rsync -arv -e `ssh -p 2222` /path/to/geodata/ geoserver@localhost:/mnt/geoserver_geodata/
```
(password is: `geoserver`)

Files uploaded into this volume will also be available to the geoserver instance in `/mnt/geoserver_geodata/`.


## Notes

In the above guide, images are pulled from docker hub, which means they've been compiled by our CI. 
In case you need to build these images by yourself, please refer to the [docker images build instructions](../docker/README.md).
