# Backgrounds

This addon add a button to the toolbar which allow to select a WMTS background layer for the map in a configured list.

Only one of the list's layer will be loaded, select another one will replace the previous one.

author: @jusabatier

## Setup

Edit the `GEOR_custom.js` file to add the required configuration:

```js
{
    "id": "backgrounds_0",
    "name": "Backgrounds",
    "title": {
        "en": "Background layers",
        "es": "Capas de fondo",
        "fr": "Fonds de plan"
    },
    "description": {
        "en": "Select a background layer",
        "es": "Seleccione una capa de fondo",
        "fr": "Sélectionnez une couche de fond"
    },
    "options": {
        "target": "tbar_12",
        "layers_groups": [
            {
                "title": "OpenStreetMap - GéoBretagne",
                "layers": [
                    {
                        "name": "Google",
                        "url": "http://osm.geobretagne.fr/service/wmts/",
                        "layer": "osm:google"
                    },
                    {
                        "name": "Bing",
                        "url": "http://osm.geobretagne.fr/service/wmts/",
                        "layer": "osm:bing"
                    },
                    {
                        "name": "Défaut",
                        "url": "http://osm.geobretagne.fr/service/wmts/",
                        "layer": "osm:map"
                    }
                ]
            },
            {
              "title": "...",
              "layers": []
            }
        ]
    },
    "preloaded": true
}
```

Note : You can ajust the addon position in toolbar with :
```js
"target": "tbar_12"
```
