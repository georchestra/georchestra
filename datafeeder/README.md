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

There is a docker composition with just the required extenal services in the `docker-compose.yml` file.

A normal build with no extra aguments (e.g. `mvn verify`) will take care of running the docker composition before the integration tests are run, and shut it down afterwards. This is performed by the `com.dkanejs.maven.plugins:docker-compose-maven-plugin`, launching the composition at maven's `pre-integration-test` phase, and shutting it down during `post-integration-test`.

Since this process may take a while, during development it is desirable to have the composition already running through several runs of the integration tests suite. To do so, launch the composition manually with

```bash
$ docker-compose -f docker-compose.yml up -d
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
docker-compose -f docker-compose.yml up -d
mvn spring-boot:run -Dspring-boot.run.profiles=georchestra,it
```
or create an equivalent run configuration in your IDE with `org.georchestra.datafeeder.app.DataFeederApplication` as the application's main class.

Then `datafeeder` should start and run at `http://localhost:8080/datafeeder/`

## Manual testing

The following is a simple step by step guide to do manual testing with `curl`.

For a more complete API description, consult the [api.yaml](api.yaml) OpenAPI 3 definition.

You can also access the Swagger UI by browsing to [http://localhost:8080/datafeeder](http://localhost:8080/datafeeder).

### Upload dataset

The `/datafeeder/upload` endpoint receives a number of files, identifies which ones are geospatial datasets, starts up an asynchronous analysis process, and returns the initial job state where to get the job identifier as a UUID.

For example, given a shapefile:

```bash
wget https://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/ne_10m_admin_0_countries.zip
unzip ne_10m_admin_0_countries.zip
ls *shp
ne_10m_admin_0_countries.shp
```

Launch the upload and analysis process with:

```bash
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR' \
 -X POST \
 http://localhost:8080/datafeeder/upload \
 -F filename=@ne_10m_admin_0_countries.shp \
 -F filename=@ne_10m_admin_0_countries.dbf \
 -F filename=@ne_10m_admin_0_countries.shx \
 -F filename=@ne_10m_admin_0_countries.prj
 
{
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0"
    }
  },
  "jobId" : "d1a62676-0de3-4b9d-a0f2-9a691f197cf0",
  "progress" : 0.0,
  "status" : "PENDING",
  "datasets" : [ ]
}
```

#### Poll updload status

Then poll the job status with the returned `jobId`:

```bash
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR' \\
 http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0
{
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0"
    }
  },
  "jobId" : "d1a62676-0de3-4b9d-a0f2-9a691f197cf0",
  "progress" : 1.0,
  "status" : "DONE",
  "datasets" : [ {
    "name" : "ne_10m_admin_0_countries",
    "status" : "DONE",
    "featureCount" : 258,
    "nativeBounds" : {
      "crs" : {
        "srs" : "EPSG:4326",
        "wkt" : "GEOGCS[\"GCS_WGS_1984\", DATUM[\"D_WGS_1984\", SPHEROID[\"WGS_1984\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\", 0.017453292519943295], AXIS[\"Longitude\", EAST], AXIS[\"Latitude\", NORTH]]"
      },
      "minx" : -179.99999999999991,
      "maxx" : 180.0,
      "miny" : -89.99999999999994,
      "maxy" : 83.63410065300008
    },
    "encoding" : "ISO-8859-1"
  } ]
}
```

#### Query dataset bounds

```
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR'  http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/ne_10m_admin_0_countries/bounds
{
  "crs" : {
    "srs" : "EPSG:4326",
    "wkt" : "GEOGCS[\"GCS_WGS_1984\", DATUM[\"D_WGS_1984\", SPHEROID[\"WGS_1984\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\", 0.017453292519943295], AXIS[\"Longitude\", EAST], AXIS[\"Latitude\", NORTH]]"
  },
  "minx" : -179.99999999999991,
  "maxx" : 180.0,
  "miny" : -89.99999999999994,
  "maxy" : 83.63410065300008
```

#### Query sample features

Use a `GET` request on `/datafeeder/updload/{id}/{typeName}/sampleFeature` to get a sample GeoJSON feature from an uploaded dataset:

```
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR'  \
 http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/ne_10m_admin_0_countries/sampleFeature
{
    "type": "Feature",
    "crs": {
        "type": "name",
        "properties": {"name": "EPSG:4326"}
    },
    "bbox": [95.01270592500003,-10.922621351999908,140.97762699400005,5.910101630000042],
    "geometry": {
        "type": "MultiPolygon",
        "coordinates": [
            [[[117.7036,4.1634], ...,[127.1304,4.7744]]]
        ]
    },
    "properties": {
        "featurecla": "Admin-0 country",
        "scalerank": 0,
        "LABELRANK": 2,
        "NAME_AR": "Ø¥Ù\u0086Ø¯Ù\u0088Ù\u0086Ù\u008AØ³Ù\u008AØ§",
        "NAME_BN": "à¦\u0087à¦¨à§\u008Dà¦¦à§\u008Bà¦¨à§\u0087à¦¶à¦¿à¦¯à¦¼à¦¾",
        "NAME_DE": "Indonesien",
        "NAME_EN": "Indonesia",
        "NAME_ES": "Indonesia",
        "NAME_FA": "Ø§Ù\u0086Ø¯Ù\u0088Ù\u0086Ø²Û\u008C",
        "NAME_FR": "IndonÃ©sie",
        "NAME_EL": "Î\u0099Î½Î´Î¿Î½Î·Ï\u0083Î¯Î±",
        "NAME_HE": "×\u0090×\u0099× ×\u0093×\u0095× ×\u0096×\u0099×\u0094",
        "NAME_HI": "à¤\u0087à¤\u0082à¤¡à¥\u008Bà¤¨à¥\u0087à¤¶à¤¿à¤¯à¤¾",
        "NAME_HU": "IndonÃ©zia",
        "NAME_ID": "Indonesia",
        "NAME_IT": "Indonesia",
        "NAME_JA": "ã\u0082¤ã\u0083³ã\u0083\u0089ã\u0083\u008Dã\u0082·ã\u0082¢",
        "NAME_KO": "ì\u009D¸ë\u008F\u0084ë\u0084¤ì\u008B\u009Cì\u0095\u0084",
        "NAME_NL": "IndonesiÃ«",
        "NAME_PL": "Indonezja",
        "NAME_PT": "IndonÃ©sia",
        "NAME_RU": "Ð\u0098Ð½Ð´Ð¾Ð½ÐµÐ·Ð¸Ñ\u008F",
        "NAME_SV": "Indonesien",
        "NAME_TR": "Endonezya",
        "NAME_UK": "Ð\u0086Ð½Ð´Ð¾Ð½ÐµÐ·Ñ\u0096Ñ\u008F",
        "NAME_UR": "Ø§Ù\u0086Ú\u0088Ù\u0088Ù\u0086Û\u008CØ´Û\u008CØ§",
        "NAME_VI": "Indonesia",
        "NAME_ZH": "å\u008D°åº¦å°¼è¥¿äº\u009A",
        "NAME_ZHT": "å\u008D°åº¦å°¼è¥¿äº\u009E"
    },
    "id": "ne_10m_admin_0_countries.1"
}
```

Note the default shapefile "encoding", **`ISO-8859-1`**, is not appropriate for the values (the sample property NAME_JA is messed up).
The above query can receive an `encoding` query parameter to indicate how to interpret the shapefile's alphanumeric data:

```
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR'  \
 http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/ne_10m_admin_0_countries/sampleFeature?encoding=UTF-8
{
    "type": "Feature",
    "crs": {
        "type": "name",
        "properties": {"name": "EPSG:4326"}
    },
    "bbox": [95.01270592500003,-10.922621351999908,140.97762699400005,5.910101630000042],
    "geometry": {
        "type": "MultiPolygon",
        "coordinates": [
            [[[117.7036,4.1634], ...,[127.1304,4.7744]]]
        ]
    },
    "properties": {
        "featurecla": "Admin-0 country",
        "scalerank": 0,
        "LABELRANK": 2,
      "NAME_AR" : "إندونيسيا",
      "NAME_BN" : "ইন্দোনেশিয়া",
      "NAME_CIAWF" : "Indonesia",
      "NAME_DE" : "Indonesien",
      "NAME_EL" : "Ινδονησία",
      "NAME_EN" : "Indonesia",
      "NAME_ES" : "Indonesia",
      "NAME_FA" : "اندونزی",
      "NAME_FR" : "Indonésie",
      "NAME_HE" : "אינדונזיה",
      "NAME_HI" : "इंडोनेशिया",
      "NAME_HU" : "Indonézia",
      "NAME_ID" : "Indonesia",
      "NAME_IT" : "Indonesia",
      "NAME_JA" : "インドネシア",
      "NAME_KO" : "인도네시아",
      "NAME_LEN" : 9,
      "NAME_LONG" : "Indonesia",
      "NAME_NL" : "Indonesië",
      "NAME_PL" : "Indonezja",
      "NAME_PT" : "Indonésia",
      "NAME_RU" : "Индонезия",
      "NAME_SORT" : "Indonesia",
      "NAME_SV" : "Indonesien",
      "NAME_TR" : "Endonezya",
      "NAME_UK" : "Індонезія",
      "NAME_UR" : "انڈونیشیا",
      "NAME_VI" : "Indonesia",
      "NAME_ZH" : "印度尼西亚",
      "NAME_ZHT" : "印度尼西亞"
    },
    "id": "ne_10m_admin_0_countries.1"
}
```

### Publish uploaded dataset

Launching the dataset publishing process and querying/polling its status is performed through
the `/datafeeder/update/{jobId}/publish` endpoint.

#### Query/poll publishing process status

At any point you can query the publishing status with a `GET` request. For example:

```
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR' \
 http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/publish

{
   "_links" : {
      "self" : {
         "href" : "http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/publish"
      }
   },
   "datasets" : [
      {
         "nativeName" : "ne_10m_admin_0_countries",
         "progress" : 0,
         "progressStep" : "SKIPPED",
         "publish" : false,
         "status" : "PENDING"
      }
   ],
   "jobId" : "d1a62676-0de3-4b9d-a0f2-9a691f197cf0",
   "progress" : 0,
   "status" : "PENDING"
}
```

After upload, the publish status will be `PENDING`, as in the sample response above.

#### Launch the publishing process

To launch the publishing process, you'll need to `POST` to that same URL with a JSON request
body that matches the `DatasetPublishRequest` defined in the [api.yaml](api.yaml) OpenAPI 3 spec, for example:

```
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR' \
 -X POST -H "Content-Type: application/json" \
 http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/publish \
 -d '{
  "datasets": [
    {    
      "encoding": "UTF-8",    
      "metadata": {    
        "title": "Include dataset title for the metadata record",    
        "abstract": "Include some dataset description to be used on the metadata record",    
        "creationDate": "2022-10-14",    
        "creationProcessDescription": "",    
        "scale": 25000,    
        "tags": [    
          "tag1", "tag2"    
        ]    
      },    
      "nativeName": "ne_10m_admin_0_countries",    
      "publishedName": "ne_10m_admin_0_countries",    
      "srs": "EPSG:4326",    
      "srs_reproject": false    
    }    
  ]    
}'

```

Where the only mandatory fields are:

* `datasets[].nativeName`
* `datasets[].metadata.title`
* `datasets[].medatada.abstract`

Once the publishing process is launched, poll its status. If the process hasn't completed, it'll return a `"status": "RUNNING"` (or `"SCHEDULED"`).

```
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR' \
 http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/publish

{
   "_links" : {
      "self" : {
         "href" : "http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/publish"
      }
   },
   "datasets" : [
      {
         "nativeName" : "ne_10m_admin_0_countries",
         "progress" : 0,
         "progressStep" : "SCHEDULED",
         "publish" : true,
         "publishedName" : "ne_10m_admin_0_countries",
         "status" : "PENDING",
         "title" : "Include dataset title for the metadata record"
      }
   ],
   "jobId" : "d1a62676-0de3-4b9d-a0f2-9a691f197cf0",
   "progress" : 0,
   "status" : "RUNNING"
}
```

Once the process finishes, the complete state will be returned:

```
curl -H 'sec-proxy: true' -H 'sec-org: test' -H 'sec-orgname: Test Org' -H 'sec-username: testadmin' -H 'sec-roles: ROLE_ADMINISTRATOR'  http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/publish
{
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/datafeeder/upload/d1a62676-0de3-4b9d-a0f2-9a691f197cf0/publish"
    }
  },
  "jobId" : "d1a62676-0de3-4b9d-a0f2-9a691f197cf0",
  "progress" : 1.0,
  "status" : "DONE",
  "datasets" : [ {
    "_links" : {
      "service" : [ {
        "href" : "https://georchestra.mydomain.org/geoserver/test/wms?",
        "title" : "Web Map Service entry point where the layer is published",
        "name" : "WMS"
      }, {
        "href" : "https://georchestra.mydomain.org/geoserver/test/wfs?",
        "title" : "Web Feature Service entry point where the layer is published",
        "name" : "WFS"
      } ],
      "preview" : {
        "href" : "https://georchestra.mydomain.org/geoserver/test/wms/reflect?LAYERS=ne_10m_admin_0_countries&width=800&format=application/openlayers",
        "title" : "OpenLayers preview page for the layer published in GeoServer",
        "type" : "application/openlayers",
        "name" : "openlayers"
      },
      "describedBy" : [ {
        "href" : "https://georchestra.mydomain.org/geonetwork/srv/api/0.1/records/ab798d02-684f-4874-a2d8-8be14bfbb718/formatters/xml",
        "title" : "Metadata record XML representation",
        "type" : "application/xml",
        "name" : "metadata"
      }, {
        "href" : "https://georchestra.mydomain.org/geonetwork/srv/eng/catalog.search#/metadata/ab798d02-684f-4874-a2d8-8be14bfbb718",
        "title" : "Metadata record web page",
        "type" : "text/html",
        "name" : "metadata"
      } ]
    },
    "nativeName" : "ne_10m_admin_0_countries",
    "publishedWorkspace" : "test",
    "publishedName" : "ne_10m_admin_0_countries",
    "metadataRecordId" : "ab798d02-684f-4874-a2d8-8be14bfbb718",
    "title" : "Include dataset title for the metadata record",
    "status" : "DONE",
    "publish" : true,
    "progress" : 1.0,
    "progressStep" : "COMPLETED"
  } ]
}
```


# Developer's corner

## Email notifications

Spring mail is used to send notifications when jobs start, finish, or fail; with the following dependency:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

The geOrchestra datadir's `datafeeder/datafeeder.properties` contains the SMTP configuration properties, like:

```
spring.mail.host=${smtpHost}
spring.mail.port=${smtpPort}
spring.mail.username: 
spring.mail.password: 
spring.mail.protocol: smtp
spring.mail.test-connection: true
spring.mail.properties.mail.smtp.auth: false
spring.mail.properties.mail.smtp.starttls.enable: false
```

If theese configuration properties are not provided, the application simply won't send emails (see 
`DataFeederNotificationsAutoConfiguration` and `GeorchestraNotificationsAutoConfiguration`).

## Email message templates

A message template consists of a number of one-line header parts, followed by the full message body.

Variables on a message template are specified using `${variable-name}` notation.
These variable names can be any of the ones defined below, or an application context property. In any case, the property
must exist or the application will fail to start up.

Note the contents of the message templates can be modified while the application is running, and the changes will be picked
up the next time an email is to be sent. However, be careful of using valid property names, validation
of such properties occurs only during application start-up.

Here's a small template example:

```
to: ${user.email}
cc: ${administratorEmail}
bcc:
sender: ${administratorEmail}
from: Georchestra Importer Application
subject:
body:

Dear ${user.name}, 
....
```

The following variables are resolved against the job's user, dataset, or publishing attributes:

* `${user.name}`:
* `${user.lastName}`:
* `${user.email}`:
* `${job.id}`:
* `${job.createdAt}`:
* `${job.error}`:
* `${job.analizeStatus}`:
* `${job.publishStatus}`:
* `${dataset.name}`:
* `${dataset.featureCount}`:
* `${dataset.encoding}`:
* `${dataset.nativeBounds}`:
* `${publish.tableName}`:
* `${publish.layerName}`:
* `${publish.workspace}`:
* `${publish.srs}`:
* `${publish.encoding}`:
* `${metadata.id}`:
* `${metadata.title}`:
* `${metadata.abstract}`:
* `${metadata.creationDate}`:
* `${metadata.lineage}`:
* `${metadata.latLonBoundingBox}`:
* `${metadata.keywords}`:
* `${metadata.scale}`:

Additionally, any other <code>${property}</code> will be resolved against the application context
(for example, any property specified in `default.properties` or `datafeeder.properties`).


## Payload

To publish a (CSV) job:

```
curl 'https://georchestra-127-0-1-1.traefik.me/datafeeder/upload/cbc00cdd-5e5d-4c40-aa76-ea44998a66ec/publish' \
  -H 'authority: georchestra-127-0-1-1.traefik.me' \
  -H 'accept: application/json' \
  -H 'accept-language: en' \
  -H 'content-type: application/json' \
  -H 'cookie: JSESSIONID=node01ukm3k9zb1uz5e9emt72uz5y60.node0' \
  -H 'dnt: 1' \
  -H 'origin: https://georchestra-127-0-1-1.traefik.me' \
  -H 'referer: https://georchestra-127-0-1-1.traefik.me/import/cbc00cdd-5e5d-4c40-aa76-ea44998a66ec/confirm' \
  -H 'sec-ch-ua: "Not_A Brand";v="8", "Chromium";v="120"' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'sec-ch-ua-platform: "Linux"' \
  -H 'sec-fetch-dest: empty' \
  -H 'sec-fetch-mode: cors' \
  -H 'sec-fetch-site: same-origin' \
  -H 'user-agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36' \
  --data-raw '{"datasets":[{"nativeName":"covoit-mel","srs":"EPSG:4326","metadata":{"title":"aaa","abstract":"aaa","tags":["Soil"],"creationDate":"2024-01-16T13:07:37.094Z","scale":10000,"creationProcessDescription":"aaa"}}]}' \
  --compressed
```
