Magnifier ADDON
===============

This addon allows users to zoom in a specific area of the map, either using the current map layers ("dynamic" mode), or a static layer ("static" mode).
authors: @fvanderbiest, @spelhate


For a static magnifier, you may want to configure your addon with a custom baselayer.
In this case, the addon config should look like this:

    {
        "id": "magnifier_zoom",
        "name": "Magnifier",
        "title": {
            "en": "Aerial imagery magnifier",
            "es": "Lupa ortofoto",
            "fr": "Loupe orthophoto"
        },
        "description": {
            "en": "A tool which allows to zoom in an aerial image on a map portion",
            "es": "Una herramienta que te permite hacer un zoom sobre una parte del mapa ortofoto",
            "fr": "Un outil qui permet de zoomer dans une orthophoto sur une portion de la carte"
        },
        "options": {
            "baseLayerConfig": {
                "wmsurl": "http://myserver.com/gwc/service/wms"
                "layer": "mylayer",
                "format": "image/jpeg",
                "buffer": 8
            }
        }
    }
    

For a dynamic magnifier (which means that the magnifier will zoom into the current visible layers), the addon config should rather be:

    {
        "id": "magnifier_zoom",
        "name": "Magnifier",
        "title": {
            "en": "Aerial imagery magnifier",
            "es": "Lupa ortofoto",
            "fr": "Loupe orthophoto"
        },
        "description": {
            "en": "A tool which allows to zoom in an aerial image on a map portion",
            "es": "Una herramienta que te permite hacer un zoom sobre una parte del mapa ortofoto",
            "fr": "Un outil qui permet de zoomer dans une orthophoto sur une portion de la carte"
        },
        "options": {
            "mode": "dynamic",
            "delta": 1,
            "baseLayerConfig": {
                "buffer": 8
            }
        }
    }


Default options for this addon are specified in the manifest.json file:

    "default_options": {
        "mode": "static",
        "baseLayerConfig": {
            "wmsurl": "http://tile.geobretagne.fr/gwc02/service/wms",
            "layer": "satellite",
            "format": "image/jpeg",
            "buffer": 8
        }
    }

This means that the magnifier tool will display the layer "satellite" from the http://tile.geobretagne.fr/gwc02/service/wms WMS server.


Note: the original OpenLayers magnifier control comes from https://github.com/fredj/openlayers-magnifier
