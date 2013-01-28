ADDON originally created by Sebastien Pelhate.

The OpenLayers magnifier control comes from https://github.com/fredj/openlayers-magnifier


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

For a static magnifier, you may want to configure your addon with a custom baselayer.
In this case, be sure to include such options in your ADDON config object:

    "options": {
        "baseLayerConfig": {
            "wmsurl": "http://myserver.com/gwc/service/wms"
            "layer": "mylayer",
            "format": "image/jpeg",
            "buffer": 8
        }
    }


For a dynamic magnifier (which means that the magnifier will zoom into the current visible layers), be sure to include such options in your ADDON config object:

    "options": {
        "mode": "dynamic",
        "delta": 1,
        "baseLayerConfig": {
            "buffer": 8
        }
    }


