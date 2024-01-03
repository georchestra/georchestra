# Quick reference

-    **Maintained by**:  
      [Georchestra.org](https://www.georchestra.org/)

-    **Where to get help**:  
     the [Georchestra Github repo](https://github.com/georchestra/georchestra), [Matrix chat](https://matrix.to/#/#georchestra:libera.chat), Stack Overflow

# Featured tags

- `latest`, `23.0.x`, `23.0.5`

# Quick reference

-	**Where to file issues**:  
     [https://github.com/georchestra/georchestra/issues](https://github.com/georchestra/georchestra/issues)

-	**Supported architectures**:   
     [`amd64`](https://hub.docker.com/r/amd64/docker/)

-	**Source of this description**:  
     [docs repo's `datafeeder/` directory](https://github.com/georchestra/georchestra/blob/master/datafeeder/DOCKER_HUB.md)

# What is `georchestra/datafeeder`

**Datafeeder** is geOrchestra's backend RESTful service to upload file based datasets and publish them to GeoServer and GeoNetwork in one shot.

The separate front-end UI ([georchestra/datafeeder-frontend](https://hub.docker.com/r/georchestra/datafeeder-frontend)) service provides the wizard-like user interface to interact with this backend.

# How to use this image

As for every other geOrchestra webapp, its configuration resides in the data directory ([datadir](https://github.com/georchestra/datadir)), typically something like /etc/georchestra, where it expects to find a analytics sub-directory.

You can run the image using :
```shell
docker run -v georchestra_datadir:/etc/georchestra georchestra/datafeeder:latest
```

Or with `docker compose`:
```yaml
  datafeeder:
    image: georchestra/datafeeder:latest
    depends_on:
      database:
        condition: service_healthy
      postgis:
        condition: service_healthy
    volumes:
      - georchestra_datadir:/etc/georchestra
      - datafeeder_uploads:/tmp/datafeeder
```

A full configuration example is available in [georchestra/docker](https://github.com/georchestra/docker) repo.

# License

View [license information](https://www.georchestra.org/software.html) for the software contained in this image.

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

[//]: # (Some additional license information which was able to be auto-detected might be found in [the `repo-info` repository's georchestra/ directory]&#40;&#41;.)

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.