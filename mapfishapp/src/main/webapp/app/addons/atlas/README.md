Atlas addon 
===========

Generates PDF maps based on vector features. Each pages represents 
one feature in its mapping context.



Authors: @jdenisgiguere, @fvanderbiest

Compatibility :  geOrchestra >= 16.06

Example addon config:

```js
    {
        "id": "atlas_0",
        "name": "Atlas",
        "enabled": true,
        "options": {
            "iconCls": "atlas-icon",
            "target": "tbar_4",
            "resultPanelAction": true,
            "layerTreeAction": true,
            "maxFeatures": 100,
            "printServerUrl": "http://localhost:8181/print/atlas/",
            "bboxBuffer": 0.1
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
            "de": "Einen Atlas generieren besteht aus mehreren Seiten"
        }
    }
```

### Setup

No special setup is required by this addon.
Please refer to georchestra/atlas/README.md to server-side configuration instructions.