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
    "options": {},
    "title": {
        "fr": "OSM vers geOrchestra",
        "en": "OSM to geOrchestra",
        "es": "OSM a geOrchestra",
        "de": "OSM zu geOrchestra"
    },
    "description": {
        "fr": "Cet addon permet de charger une nouvelle couche de données en provenance de la base OpenStreetMap",
        "en": "This addon allows you to load OSM data as a new layer in your map",
        "es": "Esta herramienta le permite mostrar datos OSM en su mapa",
        "de": "Dieses Add-on ermöglicht es die OSM-Daten in Ihrer Karte anzuzeigen"
    }
}], ...
```

With the ```options``` property, you can customize a bit more the way this addon works, eg:
```js
    "options": {
        "API_URL": "..."
        "formatOptions": {
            "checkTags": true,
            ...
        },
        "defaultStyle": "{\"strokeColor\": \"#ffff00\", \"fillColor\": \"#ffff00\"}",
        "defaultQuery": "node[\"amenity\"]{{BBOX}};way[\"amenity\"]{{BBOX}};",
    },
```

 * ```API_URL``` defaults to "http://overpass-api.de/api/interpreter"
 * ```formatOptions``` relates to the [OpenLayers OSM format](https://github.com/openlayers/openlayers/blob/master/lib/OpenLayers/Format/OSM.js), see it's [source](https://github.com/openlayers/openlayers/blob/master/lib/OpenLayers/Format/OSM.js) for detailed options.
 * ```defaultStyle``` is to customize the default style applied to features. Please refer to [OpenLayers/Feature/Vector.js](https://github.com/openlayers/openlayers/blob/release-2.13/lib/OpenLayers/Feature/Vector.js#L436-L458) for a list of all possible properties. 
 * ```defaultQuery``` is the default OSM OverPass API query. See the [Overpass API language guide](http://wiki.openstreetmap.org/wiki/Overpass_API/Language_Guide) for more information.