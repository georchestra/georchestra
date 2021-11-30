# Georchestra application gateway service

## Features

- [ ] OpenID Connect authentication
- [ ] CAS authentication
- [ ] HTTP Proxy
- [ ] HTTP/2
- [ ] Websockets

## Configuration

## Data directory property sources

Routes and other relevant configuration properties are loaded from geOrchestra "data directory"'s
`default.properties` and `gateway/gateway.yaml`.

The location of the data directory is picked up from the `georchestra.datadir` environment property,
and the additional property sources by means of spring-boot's 
`spring.config.import` environment property, like in:
`spring.config.import: ${georchestra.datadir}/default.properties,${georchestra.datadir}/gateway/gateway.yaml`.


## Docker image build

TL;DR:

Any of the following commands will build the image:

- `mvn [-DimageTag=<tag>] spring-boot:build-image` (e.g. `mvn -DimageTag=latest spring-boot:build-image`)
- `mvn [-DimageTag=<tag>] install` will also build it.
- `[BTAG=<tag>] make docker-build-gateway` at the georchestra project root directory (e.g. `BTAG=test make docker-build-gateway`)

> Not explicitly specifying the image tag produces `georchestra/gateway:latest` in the case of
using the `Makefile`, and `georchestra/gateway:${project.version}` with maven.

The docker image is called `georchestra/gateway:<tag>`, by means of the
`spring-boot.build-image.imageName` maven property.

The tag is resolved through the `imageTag` maven property, and defaults to 
the pom's `${project.version}` value.

The docker image is created by the `spring-boot-maven-plugin` under the 
`docker` maven profile, which is active by default.

`spring-boot-maven-plugin` builds an OCI compliant image based on Packeto buildpacks.


### Migrating from security-proxy

Security proxy feature set upgrade matrix

| security-proxy | Gateway | Notes |
| --- | --- | --- |
| Per service URI simple routing  | <ul><li>[x] defined in `gateway.yml`</li></ul> | as traditionally defined in `targets-mapping.properties` |
| Global and per-service `sec-*` headers | <ul><li>[ ] defined in `gateway.yml`</li></ul> | as traditionally defined in `headers-mapping.properties` |
| Filter incoming `sec-*` headers | <ul><li>[ ] custom regex based filter</li></ul> | prevents impersonation from outside world |
| `ogc-server-statistics` integration | <ul><li>[ ] </li></ul> |  |
|  | <ul><li>[ ] </li></ul> |  |
