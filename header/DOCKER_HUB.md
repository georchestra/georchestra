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
     [docs repo's `header/` directory](https://github.com/georchestra/georchestra/blob/master/header/DOCKER_HUB.md)

# What is `georchestra/header`

**Header** is a module for geOrchestra which offers a dynamic header, which is by default incorporated into every geOrchestra webapp.

# How to use this image

As for every other geOrchestra webapp, its configuration resides in the data directory ([datadir](https://github.com/georchestra/datadir)), typically something like /etc/georchestra, where it expects to find a analytics sub-directory.

You can run the image using :
```shell
docker run -v georchestra_datadir:/etc/georchestra georchestra/header:latest
```

Or with `docker compose`:
```yaml
  header:
    image: georchestra/header:latest
    depends_on:
      envsubst:
        condition: service_completed_successfully
    volumes:
      - georchestra_datadir:/etc/georchestra
```

A full configuration example is available in [georchestra/docker](https://github.com/georchestra/docker) repo.

## Where is it built

This image is build using maven : `../mvnw package docker:build -Pdocker` in `georchestra` repo `header/` folder.

# License

View [license information](https://www.georchestra.org/software.html) for the software contained in this image.

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

[//]: # (Some additional license information which was able to be auto-detected might be found in [the `repo-info` repository's georchestra/ directory]&#40;&#41;.)

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.