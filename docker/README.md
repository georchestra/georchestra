# Docker

Are you looking for a way to run geOrchestra on Docker ?
Please read the dedicated [how-to](https://github.com/georchestra/docker).

## Building your own docker images for geOrchestra

To build all geOrchestra docker images, including complementary services (ssh, smtp, webmail), clone this repo, and, from the repository root, just type the following:
```
make docker-build
```

To see which images where built: `docker images | grep georchestra`

Other useful makefile targets:
 * `docker-build-georchestra`: builds core geOrchestra images, including database & ldap
 * `docker-build-geoserver-geofence`: builds `georchestra/geoserver:geofence`
 * `docker-build-dev`: builds non-core geOrchestra images (ssh, smtp, webmail)
 * `docker-build-database`: builds `georchestra/database`
 * `docker-build-ldap`: builds `georchestra/ldap`
 * `docker-build-gn3`: builds `georchestra/geonetwork`
 * `docker-build-geoserver`: builds `georchestra/geoserver`
 * `docker-clean-volumes`: stops services and erases volumes (use with caution !)
 * `docker-clean-images`: stops services and removes images 
 * `docker-clean-all`: removes images and volumes (use with caution !)


Note: usually, you don't have to build these images by yourself, since it is already taken care of by our CI, which pushes the latest images to [docker hub](https://hub.docker.com/u/georchestra/).
