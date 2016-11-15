# Coordinates addon

This addon allows users to copy point coordinates as text, using different projections.

Authors: @fvanderbiest, inspired from the original https://github.com/geobretagne/coordinates

Compatibility :  geOrchestra >= 15.12

Example addon config:

```js
[{
    "id": "coordinates_0",
    "name": "Coordinates",
    "enabled": true,
    "title": {
        "en": "Coordinates",
        "es": "Coordenadas",
        "fr": "Coordonnées"
    },
    "description": {
        "en": "Get pixel coordinates",
        "es": "Obtener coordenadas de píxeles",
        "fr": "Obtenir les coordonnées d'un point"
    },
    "options": {
        "target": "bbar_3",
        "projections": [{
            "srs": "EPSG:4326",
            "name": "WGS84",
            "decimals": 6,
            "labels": ["Longitude", "Latitude"]
        }, {
            "srs": "EPSG:2154",
            "name": "Lambert 93",
            "decimals": 1,
            "labels": ["X", "Y"]
        }]
    }
}]
```
