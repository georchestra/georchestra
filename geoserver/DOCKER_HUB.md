# Quick reference

-    **Maintained by**:  
      [georchestra.org](https://www.georchestra.org/)

-    **Where to get help**:  
     the [Georchestra Github repo](https://github.com/georchestra/georchestra), [Matrix chat](https://matrix.to/#/#georchestra:libera.chat), Stack Overflow

# Featured tags

- `latest`, `23.0.x`
- `geogfence`, `23.0.x-geofence`

# Quick reference

-	**Where to file issues**:  
     [https://github.com/georchestra/georchestra/issues](https://github.com/georchestra/georchestra/issues)

-	**Supported architectures**:   
     [`amd64`](https://hub.docker.com/r/amd64/docker/)

-	**Source of this description**:  
     [docs repo's directory](https://github.com/georchestra/geoserver/blob/master/DOCKER_HUB.md)

# What is `georchestra/geoserver`

**Geoserver** is a module for geOrchestra which offers
- a Map and Feature server compliant with OGC standards (WMS, WFS, WCS, WPS, CSW, ...)

geOrchestra's Geoserver is a fork of [GeoServer](https://github.com/geoserver/geoserver).


# How to use this image

As for every other geOrchestra webapp, its configuration resides in the data directory ([datadir](https://github.com/georchestra/datadir)), typically something like /etc/georchestra, where it expects to find a geoserver sub-directory.

It is recommended to use the official docker composition: https://github.com/georchestra/docker.

For this specific component, see the section `geoserver` in the [`georchestra/docker/docker-compose.yml`](https://github.com/georchestra/docker/blob/master/docker-compose.yml) file.

## Where is it built

This image is build using maven : `./mvnw -Pdocker package docker:build` in [`georchestra`](https://github.com/georchestra/georchestra) repo.

# License

View [license information](https://www.georchestra.org/software.html) for the software contained in this image.

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

[//]: # (Some additional license information which was able to be auto-detected might be found in [the `repo-info` repository's georchestra/ directory]&#40;&#41;.)

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.
