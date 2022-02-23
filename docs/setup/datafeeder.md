# Datafeeder

## Abstract

geOrchestra `v22.0` comes with a new web application called `datafeeder`, which aims
to facilitate the integration of some datasets into the infrastructure, via
a web wizard:

#. The dataset is sent to the backend
#. The user types in some infos
#. The dataset is loaded into a postGIS database
#. A layer is created into a geoserver workspace
#. A Metadata is created into geonetwork

During the process, some mails are sent to the user, depending on the status of the import.

For now, the service only supports ZIPped shapefiles, but could allow other formats to be
uploaded in the future.

## Webapps involved

Technically, the web service is split into 2 web applications:

#. The backend, commonly available under `/datafeeder/`
#. A web user interface, commonly available under `/import/`

The second one is actually triggering calls to the first one, both being behind the
security-proxy.

* The code for the backend is located into the [official geOrchestra repository](https://github.com/georchestra/georchestra/tree/master/datafeeder).
* The web ui code has its own [separated repository](https://github.com/georchestra/geonetwork-ui/tree/georchestra-datafeeder).

A [maven module](https://github.com/georchestra/georchestra/tree/master/datafeeder-ui) has been created
into the main geOrchestra repository, so that we can generate a webapp out of the UI source code.

## Artifacts

Depending on the deployment approach being used, the artifacts for the web user interface do not have the same nature:

* The docker image is a simple nginx one serving static assets of the UI
* The webapp generated from the `datafeeder-ui` maven module is basically
  a spring based web application which mimic what the previous nginx configuration does.

## Dependencies

The datafeeder does not need specific components to run, apart from the ones geOrchestra already relies on:

* A postGreSQL/PostGIS database
* GeoNetwork
* GeoServer

## Configuration

The configuration can be found into the [geOrchestra datadir](https://github.com/georchestra/datadir/tree/master/datafeeder), in the dedicated directory.

In order to function, the datafeeder will require a correctly configured GEMET thesaurus inside GeoNetwork. Make sure that the thesaurus URL set in the
[configuration](https://github.com/georchestra/datadir/blob/master/datafeeder/frontend-config.json#L26) can be queried.

## Database

Datafeeder will make use of 2 different database connections:

* The first one is meant to keep track of the imported datasets via the user interactions ; see [here](https://github.com/georchestra/datadir/blob/master/datafeeder/datafeeder.properties#L1-L18) for the relevant configuration
* The second one is meant to store the geographical datas being imported ; relevant config [here](https://github.com/georchestra/datadir/blob/master/datafeeder/datafeeder.properties#L75-L99).

To enforce separation of concerns, it is advised to separate the PostGIS database receiving the uploaded datasets from the regular `geOrchestra` database.
