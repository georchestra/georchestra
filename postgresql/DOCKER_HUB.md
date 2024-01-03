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
     [docs repo's `postgresql/` directory](https://github.com/georchestra/georchestra/blob/master/postgresql/DOCKER_HUB.md)

# What is `georchestra/database`

**Database** is the database for geOrchestra which offers a custom postgresql image with all schema and tables prepared for geOrchestra SDI.

# How to use this image

You can run the image using :
```shell
docker run georchestra/database:latest
```

Or with `docker compose`:
```yaml
  database:
    image: georchestra/database:latest
    depends_on:
      envsubst:
        condition: service_completed_successfully
    volumes:
      - postgresql_data:/var/lib/postgresql/data
    restart: always
```

A full configuration example is available in [georchestra/docker](https://github.com/georchestra/docker) repo.

## Where is it built

This image is build using Dockerfile in `georchestra` repo `postgresql/` folder.

# License

View [license information](https://www.georchestra.org/software.html) for the software contained in this image.

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

[//]: # (Some additional license information which was able to be auto-detected might be found in [the `repo-info` repository's georchestra/ directory]&#40;&#41;.)

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.