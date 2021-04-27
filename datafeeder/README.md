# Data Feeder

"Datafeeder" is geOrchestra's backend RESTful service to upload file based datasets and publish them to GeoServer and GeoNetwork in one shot.

The separate front-end UI service provides the wizard-like user interface to interact with this backend.

## Building

Build the project:

To compile and run the unit and integration tests:

```bash
georchestra$ mvn clean install -f datafeeder/
```

Or

```bash
georchestra$ cd datafeeder
datafeeder$ mvn clean install
```

To build the datafeeder application and its docker image:

```bash
georchestra$ make docker-build-datafeeder
```

### Skipping tests

Use the following maven properties to skip tests and/or integration tests:

* `-DskipTests` skips both unit and integration tests
* `-DskipITs` skips only integration tests and also avoid launching the docker composition (see section below)
* `-D-Ddocker-compose.skip=true` avoids running the docker composition (see section below)

### Integration testing: docker compose

For integration testing, some external services are required. For instance:
- A geOrchestra GeoServer instance
- A geOrchestra GeoNetwork instance
- A PostgreSQL database with PostGIS extension, for which we're using geOrchestra's `database` docker image

There is a docker composition with just the required extenal services in the `docker-compose-it.yml` file.

A normal build with no extra aguments (e.g. `mvn verify`) will take care of running the docker composition before the integration tests are run, and shut it down afterwards. This is performed by the `com.dkanejs.maven.plugins:docker-compose-maven-plugin`, launching the composition at maven's `pre-integration-test` phase, and shutting it down during `post-integration-test`.

Since this process may take a while, during development it is desirable to have the composition already running through several runs of the integration tests suite. To do so, launch the composition manually with

```bash
$ docker-compose -f docker-compose-it.yml up -d
```

With that in place, run the tests as many times as needed from the IDE or the console by enabling the `docker-compose.skip` flag:

```bash
$ mvn verify -Ddocker-compose.skip=true
```

so that the `docker-compose-maven-plugin` does not run.

The integration tests ough to be written in a way that support multiple runs without re-initializing the external services state (for example, randomizing database schema names when going to create a schema and such).

### Build the docker image:

**database**: while running from the `datafeeder/integration` branch, and until the work is merged to master, you'll need to build the `database` docker image in addition to datafeeder's, for the postgres `datafeeder` schema to be initialized, and prune the database volume.

```bash
georchestra$ make docker-build-database
```

To build *datafeeder*'s docker image:

```bash
georchestra$ make docker-build-datafeeder
georchestra$ docker images|grep datafeeder
georchestra/datafeeder         20.2-SNAPSHOT       a2ca96143b9f        12 seconds ago      376MB
```

## Running

At this point, the service can run as a geOrchestra dockerized service, as part of its docker composition, or standalone for development purposes.

With the service's REST API being defined as an [Open API 3](api.yaml)  specification, a [swagger-ui](https://swagger.io/tools/swagger-ui/)  user interface is provided when browsing to `/import` (e.g. http://localhost:8080/import/ when running standalone, https://georchestra.mydomain.org/import when running within the docker composition - must log in first - )

### Georchestra environment

#### Checkout the datafeeder branch in docker:

Running as a geOrchestra service requires a clone of geOrchestra's docker repository `git@github.com:georchestra/docker.git`.

At this time, being not part of the official distribution, you need to switch to the `datafeeder` branch, which in turn will set the git `config/` submodule to the appropriate datafeeder branch, so that the `config/datafeeder/datafeder.properties` file exists.

```bash
git clone git@github.com:georchestra/docker.git
git checkout datafeeder
git submodule update --init
```

#### Run geOrchestra docker composition

Run geOrchestra with datafeeder integrated:

```
docker compose up -d
```

*geOrchestra*'s "security proxy" API Gateway service has been configured to redirect all calls to `/datafeeder/**` to this service.

Open [https://georchestra-127-0-1-1.traefik.me/](https://georchestra-127-0-1-1.traefik.me/) in your browser.

Log in with these credentials:

* `testuser` / `testuser`
* `testadmin` / `testadmin`

Once logged in, *datafeeder*'s OpenAPI test UI is available at [https://georchestra-127-0-1-1.traefik.me/datafeeder](https://georchestra-127-0-1-1.traefik.me/datafeeder)


### Standalone

Run from within the `datafeeder` root folder with:

```bash
docker-compose -f docker-compose-it.yml up -d
mvn spring-boot:run -Dspring-boot.run.profiles=georchestra,it
```
or create an equivalent run configuration in your IDE with `org.georchestra.datafeeder.app.DataFeederApplication` as the application's main class.

Then `datafeeder` should start and run at `http://localhost:8080/datafeeder/`

## Manual testing

The `/import/upload` endpoint receives a number of files, identifies which ones are geospatial datasets, starts up an asynchronous analysis process, and returns the initial job state where to get the job identifier as a UUID.

For example, given a shapefile:

```bash
wget https://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/ne_10m_admin_0_countries.zip
unzip ne_10m_admin_0_countries.zip
ls *shp
ne_10m_admin_0_countries.shp
```

Launch the upload and analysis process with:

```bash
curl -H 'sec-org: test org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_USER;ROLE_ADMINISTRATOR' -H 'sec-proxy: true' \
 'http://localhost:8080/import/upload' \
 -F filename=@ne_10m_admin_0_countries.shp \
 -F filename=@ne_10m_admin_0_countries.dbf \
 -F filename=@ne_10m_admin_0_countries.shx \
 -F filename=@ne_10m_admin_0_countries.prj
 
{"jobId":"37bb3abe-69ce-40cc-be44-24a05aace203","progress":0.0,"status":"PENDING","error":null,"datasets":[]}
```

Then poll the job status with the returned `jobId`:

```bash
curl -H 'sec-username: testadmin' -H 'sec-roles: ROLE_USER;ROLE_ADMINISTRATOR' -H 'sec-proxy: true' \
 'http://localhost:8080/import/upload/37bb3abe-69ce-40cc-be44-24a05aace203'

{
   "datasets" : [
      {
         "encoding" : "ISO-8859-1",
         "error" : null,
         "featureCount" : 255,
         "name" : "ne_10m_admin_0_countries",
         "nativeBounds" : {
            "crs" : {
               "srs" : "EPSG:4326",
               "wkt" : "GEOGCS[\"GCS_WGS_1984\", DATUM[\"D_WGS_1984\", SPHEROID[\"WGS_1984\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\", 0.017453292519943295], AXIS[\"Longitude\", EAST], AXIS[\"Latitude\", NORTH]]"
            },
            "maxx" : 180,
            "maxy" : 83.6341006530001,
            "minx" : -180,
            "miny" : -89.9999999999999
         },
        "sampleGeometryWKT" : "MULTIPOLYGON (((117.70360790395524 4.163414542001791, .....",
        "sampleProperties" : [
            {
               "name" : "NAME_CIAWF",
               "type" : "String",
               "value" : "Indonesia"
            },
            {
               "name" : "ADM0_DIF",
               "type" : "Integer",
               "value" : "0"
            },
            {
               "name" : "NAME_JA",
               "type" : "String",
               "value" : "Ã£Â<82>Â¤Ã£Â<83>Â³Ã£Â<83>Â<89>Ã£Â<83>Â<8d>Ã£Â<82>Â·Ã£Â<82>Â¢"
            },...
      }
   ]
 }
```

> Note the default shapefile "encoding", `ISO-8859-1`, is not appropriate for the values (the sample property NAME_JA is messed up)

In the future we could also include the (non standard) code-page shapefile sidecar file (in this case it would be `ne_10m_admin_0_countries.cpg`) for automatic recognison of a non-default character encoding for the `.dbf` file; and/or change the dataset's encoding through the API. The `.cpg` file contains `UTF-8`, with which `NAME_JA` should return `{"name": "NAME_JA", "value": "インドネシア"}` instead.

# Developer's corner

## Email notifications

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```
