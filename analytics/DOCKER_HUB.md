# Quick reference

-    **Maintained by**:  
      [Georchestra.org](https://www.georchestra.org/)

-    **Where to get help**:  
      the Georchestra Github repo, Matrix chat, Stack Overflow

https://matrix.to/#/#georchestra:libera.chat

# Featured tags

- `latest`, `23.0.x`, `23.0.5`

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

Add it in your docker compose file, specifying [datadir](https://github.com/georchestra/datadir) location:
```yaml
  analytics:
    image: georchestra/analytics:latest
    healthcheck:
      test: ["CMD-SHELL", "curl -s -f http://localhost:8080/analytics/ >/dev/null || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 10
    depends_on:
      database:
        condition: service_healthy
    volumes:
      - georchestra_datadir:/etc/georchestra
```

A full configuration example is available in [georchestra/docker](https://github.com/georchestra/docker) repo.

# License

View [license information](https://www.georchestra.org/software.html) for the software contained in this image.

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

[//]: # (Some additional license information which was able to be auto-detected might be found in [the `repo-info` repository's georchestra/ directory]&#40;&#41;.)

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.