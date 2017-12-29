# Backgrounds

This addon adds a button to the top toolbar, which allows to add a WMTS background layer to the map, from a configured list.

Only one of the list's layer will be loaded. Selecting another one will replace the current one.

author: @jusabatier

## Setup

In your geOrchestra datadir, edit the `mapfishapp/addons/backgrounds/config.json` file to update the configuration:

```json
[{
    "id": "backgrounds_0",
    "name": "Backgrounds",
    "enabled": true,
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
        "layers_groups": [{
            "title": "OpenStreetMap - GeoBretagne",
            "layers": [{
                    "name": "Google",
                    "url": "https://osm.geobretagne.fr/service/wmts/",
                    "layer": "osm:google"
                },
                {
                    "name": "Bing",
                    "url": "https://osm.geobretagne.fr/service/wmts/",
                    "layer": "osm:bing"
                },
                {
                    "name": "Default",
                    "url": "https://osm.geobretagne.fr/service/wmts/",
                    "layer": "osm:map"
                }
            ]
        }]
    },
    "preloaded": true
}]
```

Note: You can ajust the addon position in the toolbar with the `target` option.
