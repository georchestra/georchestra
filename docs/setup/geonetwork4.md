# Geonetwork4

GeoNetwork in version 4 does not manage the metadata indexes internally anymore.
Since the index management has been externalized to an Elasticsearch server, it is
necessary to set it up.

## Elasticsearch

The first thing _GeoNetwork4_ will require is an Elasticsearch server to store
the metadata index.

The advised version to use is the `7.14.0` one, you can refer to the
[official documentation](https://www.elastic.co/guide/en/elasticsearch/reference/7.14/deb.html)
to set ip up.

## Kibana

As not mandatory per se, Kibana can also be set up, in order to provide a dashboard
accessible directly from the GeoNetwork admin user interface.

You can follow the previous documentation from Elastic to install kibana.

## geOrchestra Datadir

Compared to the former version, the geOrchestra datadir in regards to GeoNetwork
also faced some modifications:

* It is not possible anymore to load spring beans from the datadir, as a result
  the XMLs files from the `config` subdirectory have been removed
* only the `geonetwork.properties` and the `log4j` configuration remain

you can find
[the advised configuration from the geOrchestra datadir here](https://github.com/georchestra/datadir/tree/master/geonetwork/).
