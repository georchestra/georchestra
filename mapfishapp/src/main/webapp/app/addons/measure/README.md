# Measure

Simple mapfishapp addon to perform distance and area measurements.
... was originally part of mapfishapp codebase, ported to an addon for the 15.12 release.

Author: @fvanderbiest

## Setup

Edit the `GEOR_custom.js` file to add the required configuration for this addon:

```js
ADDONS: [...,
{
    "id": "measure_0",
    "name": "Measure",
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