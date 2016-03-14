# Measurements

Mapfishapp addon to perform advanced distance and area measurements.

Contrary to the measure addon, this one allows users to:
 * have N measurements at the same time on their map
 * measure each segment
 * print the measurements

Author: @fvanderbiest, with kudos to @jorix for the original [OL-DynamicMeasure](https://github.com/jorix/OL-DynamicMeasure) control.

## Setup

Edit the `GEOR_custom.js` file to add the required configuration for this addon:

```js
ADDONS: [...,
{
    "id": "measurements_0",
    "name": "Measurements",
    "options": {},
    "title": {
        "en": "Measurement tools",
        "es": "Herramientas de medición",
        "fr": "Outils de mesure",
        "de": "Messwerkzeuge"
    },
    "description": {
        "en": "Distance and area measuring tools",
        "es": "Herramientas de medición de superficies y distancias",
        "fr": "Outils de mesure de distances et de surfaces",
        "de": "Werkzeuge zum Messen von Entfernungen und Flächen"
    }
}], ...
```

Note that it is possible to alter the default drawing style with the following option:
```js
    "options": {
        "graphicStyle": {
            "Point": {
                "pointRadius": 4,
                "graphicName": "square",
                "fillColor": "white",
                "fillOpacity": 1,
                "strokeWidth": 1,
                "strokeOpacity": 1,
                "strokeColor": "#333333"
            },
            "Line": {
                "strokeWidth": 3,
                "strokeOpacity": 1,
                "strokeColor": "#666666",
                "strokeDashstyle": "dash"
            },
            "Polygon": {
                "strokeWidth": 2,
                "strokeOpacity": 1,
                "strokeColor": "#666666",
                "fillColor": "white",
                "fillOpacity": 0.3
            }
        }
    },
```

Icons comes from [icons8.com](http://megaicons.net/iconspack-178/5730/)
License: Linkware (Backlink required) 
