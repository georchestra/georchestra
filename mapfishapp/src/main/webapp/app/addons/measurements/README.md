# Measurements

Mapfishapp addon to perform advanced distance and area measurements.

Contrary to the measure addon, this one allows users to:
 * have N measurements at the same time on their map
 * measure each segment
 * print the measurements

Author: @fvanderbiest & @jdenisgiguere, with kudos to @jorix for the original [OL-DynamicMeasure](https://github.com/jorix/OL-DynamicMeasure) control.

## Setup

Edit the `GEOR_custom.js` file to add the required configuration for this addon:

```js
ADDONS: [...,
{
    "id": "measurements_0",
    "name": "Measurements",
    "options": {},
    "title": {
        "en": "Advanced measurement tools",
        "es": "Herramientas de medición avanzada",
        "fr": "Outils de mesure avancés",
        "de": "Erweiterte Messwerkzeuge"
    },
    "description": {
        "en": "Distance and area measuring tools. Measures are cumulative, they can also be printed & exported.",
        "es": "Herramientas de medición de superficies y distancias. Las medidas son acumulativos, que también se pueden imprimir y exportación.",
        "fr": "Outils de mesure de distances et de surfaces. Les mesures sont persistantes, et peuvent être imprimées & exportées.",
        "de": "Werkzeuge zum Messen von Entfernungen und Flächen. Die Messungen können auch gedruckt und exportiert werden."
    }
}], ...
```

Options allow an administrator to alter the default:
 * drawing style
 * accuracy
 * KML style

eg:
```js
    "options": {
        "accuracy": 3,
        "KMLStyle": "<Style id='measureFeatureStyle'><LineStyle><width>2</width><color>ff6666636</color></LineStyle><PolyStyle><fill>0</fill></PolyStyle><LabelStyle><color>ff170580</color></LabelStyle><IconStyle><color>00ffffff</color><Icon><href>http:/maps.google.com/mapfiles/kml/shapes/placemark_circle.png</href></Icon></IconStyle></Style>",
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
                "strokeWidth": 2,
                "strokeOpacity": 1,
                "strokeColor": "#666666",
                "strokeDashstyle": "dash"
            },
            "Polygon": {
                "strokeWidth": 2,
                "strokeOpacity": 1,
                "strokeColor": "#666666",
                "strokeDashstyle": "solid",
                "fillColor": "white",
                "fillOpacity": 0.3
            },
            "labelSegments": {
                "label": "${measure} ${units}",
                "fontSize": "11px",
                "fontColor": "#800517",
                "fontFamily": "Verdana",
                "labelOutlineColor": "#dddddd",
                "labelAlign": "cm",
                "labelOutlineWidth": 2,
                "pointRadius": 1,
                "fillColor": "#ffffff",
                "fillOpacity": 0,
                "strokeWidth": 1,
                "strokeOpacity": 0,
                "strokeColor": "#ffffff"
            },
            "labelLength": {
                "label": "${measure} ${units}\n",
                "fontSize": "11px",
                "fontWeight": "bold",
                "fontColor": "#800517",
                "fontFamily": "Verdana",
                "labelOutlineColor": "#dddddd",
                "labelAlign": "lb",
                "labelOutlineWidth": 3,
                "pointRadius": 1,
                "fillColor": "#ffffff",
                "fillOpacity": 0,
                "strokeWidth": 1,
                "strokeOpacity": 0,
                "strokeColor": "#ffffff"
            },
            "labelArea": {
                "label": "${measure}\n${units}²\n",
                "fontSize": "11px",
                "fontWeight": "bold",
                "fontColor": "#800517",
                "fontFamily": "Verdana",
                "labelOutlineColor": "#dddddd",
                "labelAlign": "cm",
                "labelOutlineWidth": 3,
                "pointRadius": 1,
                "fillColor": "#ffffff",
                "fillOpacity": 0,
                "strokeWidth": 1,
                "strokeOpacity": 0,
                "strokeColor": "#ffffff"
            }
        }
    },
```

Icons comes from [icons8.com](http://megaicons.net/iconspack-178/5730/)
License: Linkware (Backlink required) 
