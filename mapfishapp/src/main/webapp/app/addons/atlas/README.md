# Atlas addon 

Generates PDF maps based on vector features. Each pages represents 
one feature in its mapping context.


Authors: @jdenisgiguere, @fvanderbiest

Compatibility :  geOrchestra >= 16.12

# Setup

## Client-side

Example addon config:

```js
{
    "id": "atlas_0",
    "name": "Atlas",
    "enabled": true,
    "options": {
        "resultPanelAction": true,
        "layerTreeAction": true,
        "maxFeatures": 100,
        "defaultPointScale": 17061.8366707982724577
    },
    "title": {
        "fr": "Atlas",
        "en": "Atlas",
        "es": "atlas",
        "de": "Atlas"
    },
    "description": {
        "fr": "Génération d'un atlas constitué de plusieurs pages PDF",
        "en": "Create an altas with several PDF pages",
        "es": "Generar atlas consta de varias páginas PDF",
        "de": "Einen Atlas generieren"
    }
}
```

Note that several configuration options are available, eg:
```js
    "options": {
        "iconCls": "atlas-icon",
        "target": "tbar_4",
        "resultPanelAction": true,
        "layerTreeAction": true,
        "maxFeatures": 100,
        "atlasServerUrl": "/atlas/print",
        "buffer": 0.1,
        "defaultPointScale": 17061.8366707982724577,
        "wmsc2wms": {
            "https://test.sdi.fr/geowebcache/service/wms": "https://test.sdi.fr/geoserver/wms",
            "https://www.sdi.fr/geowebcache/service/wms": "https://www.sdi.fr/geoserver/wms"
        }
    },
```

Options:
 * **iconCls** to override the default icon - defaults to "atlas-icon"
 * **target** to have the addon button somewhere in the UI - defaults to undefined
 * **resultPanelAction** to have an Atlas menu in the resultsPanel, to allow selection of features to print - defaults to true
 * **layerTreeAction** to have an Atlas menu in the layer action menu - defaults to true
 * **maxFeatures** to limit the number of atlas pages / features - defaults to undefined
 * **atlasServerUrl** to set the server side component endpoint - defaults to "/atlas/print"
 * **buffer** to set the spatial buffer to apply to feature in page - defaults to 0.1
 * **defaultPointScale** to set the default point print scale - defaults to 17061.8366707982724577
 * **wmsc2wms** is a hash storing matching WMS-C / WMS servers - defaults to empty hash
        
        
## Server-side

Please refer to [atlas/README.md](/atlas/README.md) for server-side configuration instructions.
