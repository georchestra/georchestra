# Quick reference

-    **Maintained by**:  
      [georchestra.org](https://www.georchestra.org/)

-    **Where to get help**:  
     the [geOrchestra Github repo](https://github.com/georchestra/georchestra), [IRC chat](https://matrix.to/#/#georchestra:osgeo.org), Stack Overflow

# Featured tags

- `latest`, `25.0.x`, `24.0.x`,

# Quick reference

-	**Where to file issues**:  
     [https://github.com/georchestra/georchestra/issues](https://github.com/georchestra/georchestra/issues)

-	**Supported architectures**:   
     [`amd64`](https://hub.docker.com/r/amd64/docker/)

-	**Source of this description**:  
     [docs repo's `analytics/` directory](https://github.com/georchestra/georchestra/blob/master/analytics/DOCKER_HUB.md)

# What is `georchestra/analytics`

**Analytics** is a module for geOrchestra which offers
- services which are used by the console
- a GUI which displays **monthly** and **global** statistics on platform usage, through OGC web services monitoring.

It relies on the [ogc-server-statistics](ogc-server-statistics/README.md) module (which is embedded into the security-proxy) to collect figures in a database:
- service type, layer name, request type (getmap/getfeature/getcapabilities/...), hits
- username, number of requests
- organisation, number of requests

Each table can be exported to CSV for easy offline use.

# How to use this image

As for every other geOrchestra webapp, its configuration resides in the data directory ([datadir](https://github.com/georchestra/datadir)), typically something like /etc/georchestra, where it expects to find a analytics sub-directory.

It is recommended to use the official docker composition: https://github.com/georchestra/docker.

For this specific component, see the section `analytics` in the [`georchestra/docker/docker-compose.yml`](https://github.com/georchestra/docker/blob/master/docker-compose.yml) file.

## Where is it built

This image is built using maven : `../mvnw package docker:build -Pdocker` in `georchestra` repo `analytics/` folder.

# License

View [license information](https://www.georchestra.org/software.html) for the software contained in this image.

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

[//]: # (Some additional license information which was able to be auto-detected might be found in [the `repo-info` repository's georchestra/ directory]&#40;&#41;.)

As for any docker image, it is the user's responsibility to ensure that usages of this image comply with any relevant licenses for all software contained within.
