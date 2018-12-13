# "Go to" addon

This addon allows users to recenter their map on a location, given its coordinates in various SRSes.

auteurs: [@fvanderbiest](https://github.com/fvanderbiest/)

Compatibility: geOrchestra >= 15.12

Example configuration:
```js
[{
    "id": "goto_0",
    "name": "Goto",
    "enabled": true,
    "title": {
        "en": "Go to coordinates...",
        "es": "Ir a coordenadas ...",
        "fr": "Aller aux coordonnées ...",
        "de": "Zu Koordinaten gehen"
    },
    "description": {
        "en": "Go to a location given its coordinates",
        "es": "Ir a un lugar dado sus coordenadas",
        "fr": "Aller à un point de coordonnées données",
        "de": "Gehen Sie zu einem Ort, der seine Koordinaten gibt"
    },
    "options": {
        "projections": [{
            "srs": "EPSG:4326",
            "name": "WGS84"
        }, {
            "srs": "EPSG:2154",
            "name": "Lambert 93"
        }]
    }
}]
```

If the `zoomLevel` option is set, the map is also zoomed to the givn zoom level.
By default, `zoomLevel` is not set, which means the map gets paned only.
