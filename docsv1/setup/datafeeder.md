# Datafeeder

## Abstract

geOrchestra `v22.0` comes with a new web application called `datafeeder`, which aims
to facilitate the integration of datasets into the infrastructure, via a web wizard:

* the dataset is sent to the backend
* the user types in some infos
* the dataset is loaded into a postGIS database
* a layer is created into a geoserver workspace
* a Metadata is created into geonetwork

During the process, emails are sent to the user, depending on the status of the import.

For now, the service only supports ZIPped shapefiles, but could allow other formats to be
uploaded in the future.

## Webapps involved

Technically, the web service is split into 2 web applications:

* a backend, commonly available under `/datafeeder/`
* a web user interface, commonly available under `/import/`

The second one is actually triggering calls to the first one, both being behind the
security-proxy.

* The code for the backend is located into the [official geOrchestra repository](https://github.com/georchestra/georchestra/tree/master/datafeeder).
* The web ui code has its own [separated repository](https://github.com/georchestra/geonetwork-ui/tree/georchestra-datafeeder).

A [maven module](https://github.com/georchestra/georchestra/tree/master/datafeeder-ui) has been created
into the main geOrchestra repository, so that we can generate a webapp out of the UI source code.

## Artifacts

Depending on the deployment approach being used, the artifacts for the web user interface do not have the same nature:

* the docker image is a simple nginx one serving static assets of the UI
* the webapp generated from the `datafeeder-ui` maven module is basically
  a spring based web application which mimics what the previous nginx configuration does.

## Dependencies

The datafeeder does not need specific components to run, apart from the ones geOrchestra already relies on:

* a postGreSQL/PostGIS database
* GeoNetwork
* GeoServer

## Configuration

The configuration can be found into the [geOrchestra datadir](https://github.com/georchestra/datadir/tree/master/datafeeder), in the dedicated directory.

The datafeeder will also require a correctly configured GEMET thesaurus inside GeoNetwork. Make sure that the thesaurus URL set in the
[configuration](https://github.com/georchestra/datadir/blob/master/datafeeder/frontend-config.json#L26) can be queried.

## Database

The datafeeder will make use of 2 database connections:

* the first one is meant to keep track of the imported datasets via the user interactions ; see [here](https://github.com/georchestra/datadir/blob/master/datafeeder/datafeeder.properties#L1-L18) for the relevant configuration. By default this targets the `datafeeder` schema from the `georchestra` database.
* the second one is meant to store the geographical data being imported ; relevant config [here](https://github.com/georchestra/datadir/blob/master/datafeeder/datafeeder.properties#L75-L99). Any PostGIS-enabled database is suitable, though we advise to use a fresh one, rather than a shared database (due to potential collision of schemas' names). 
