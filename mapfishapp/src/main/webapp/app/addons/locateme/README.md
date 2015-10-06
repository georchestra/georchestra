# LocateMe

This addon, when enabled and active, allows users to track their location on the map.
author: @fvanderbiest

## Setup

Edit the `GEOR_custom.js` file to add the required configuration:

```js
ADDONS: [...,
{
    "id": "locateme_0",
    "name": "LocateMe",
    "options": {
        "target": "bbar_7"
    },
    "title": {
        "fr": "Localisez moi",
        "en": "Locate me",
        "es": "localizarme",
        "de": "Wo bin ich ?"
    },
    "description": {
        "fr": "Cet addon permet d'afficher votre position sur la carte",
        "en": "This addon allows you to display your position on the map",
        "es": "Esta herramienta le permite mostrar su posición en el mapa",
        "de": "Dieses Addon ermöglicht Ihnen, Ihre Position auf der Karte angezeigt werden"
    }
}], ...
```

Note that several configuration options are available, eg:
```js
    "options": {
        "target": "bbar_7",
        "watchPosition": {
            "maximumAge": 2000,
            "timeout": 30000,
            "enableHighAccuracy": true
        },
        "graphicStyle": {
            "graphicName": "cross",
            "pointRadius": 16,
            "strokeColor": "fuchsia",
            "strokeWidth": 2,
            "fillOpacity": 0
        }
    },
```
The ```watchPosition``` options are documented on this page: [https://developer.mozilla.org/en-US/docs/Web/API/PositionOptions](https://developer.mozilla.org/en-US/docs/Web/API/PositionOptions)