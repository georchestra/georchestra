# osm2geor

Mapfishapp addon made to display vector data from OSM (got from the Overpass API) into a vector layer.

Original work from @pmauduit (https://github.com/pmauduit/osm2geor). 
Minor changes from @fvanderbiest to make it work with geOrchestra 14.12 and later.

## Setup

Edit the `GEOR_custom.js` file to add the required configuration for this addon:

```js
ADDONS: [...,
{
    "id": "osm2geor_0",
    "name": "Osm2Geor",
    "enabled": true,
    "title": {
        "fr": "osm2geor",
        "en": "osm2geor",
        "es": "osm2geor"
    },
    "description": {
        "fr": "osm2geOr est un greffon permettant de requêter l'overpass-API OpenStreetMap et de charger une nouvelle couche",
        "en": "osm2geOr is an addon which allows querying the OSM overpass-API and to load the result as a new layer",
        "es": "osm2geOr es un plugin para interrogar el OSM overpass-API y cargar el resultado como un nuevo layer"
    }
}], ...
```