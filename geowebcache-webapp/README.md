# GeoWebCache distribution for geOrchestra

![geowebcache](https://github.com/georchestra/georchestra/workflows/geowebcache/badge.svg)

This is a geOrchestra specific re-packaging of GeoWebcache with customized extensions to

- Handle authentication through geOrchestra's authentication HTTP request headers
- Use a custom URL "mangler" for GWC to return URL's based on geOrchestra's public URL

## Build

```
mvn clean install -f geowebcache-webapp
```

### Docker

```
make docker-build-geowebcache
```

## Integration approach

Vanilla GWC `gwc-web` maven published artifact is actually a .war file packaged as .jar,
containing all the web application libraries and resources, which turns it into a useless
dependency as a jar.

In order not to include this jar that adds nothing but doubling the size of the re-packaged
application, `gwc-web` is not referenced directly. Instead, we include the required `gwc-*`
transitive dependencies from `gwc-web` directly in our `pom.xml`.

### Spring configuration

Geowebcache loads its main configuration from `WEB-INF/lib/geowebcache-servlet.xml`, which in turn
imports a bunch of other `geowebcache-*.xml` spring beans configuration files at the same location.

`geowebcache-servlet.xml` is configured in web.xml to be loaded by both `DispatcherServlet`
and `ContextLoaderListener`, which results in double initialization of singleton beans.

For this reason, we instead make `ContextLoaderListener` load from `applicationContext.xml`, which in
turn includes `geowebcache-georchestra.xml`, which allows to add new bean definitions as well as to
override any previously defined bean by name; and let `DispatcherServlet` load from an empty
`geowebcache-servlet.xml`.
