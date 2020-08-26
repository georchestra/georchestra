Nominatim ADDON
===============

author: @jusabatier

The addon config should look like this:
```
{
    "id": "osmsearchplaces_0",
    "name": "OSMSearchPlaces",
    "title": {
        "en": "Search places over OSM",
        "es": "Buscar lugares a través OSM",
        "fr": "Recherche de lieux sur OSM"
    },
    "description": {
        "en": "Search places over OSM",
        "es": "Buscar lugares a través OSM",
        "fr": "Recherche de lieux sur OSM"
    },
    "options": {
        "overpassURL": "http://overpass-api.de/api/interpreter",
        "nominatimURL": "http://nominatim.openstreetmap.org/search",
        "target": "tbar_11",
        "boundingbox": {
            "minlat": "44.874466",
            "minlon": "3.6239646",
            "maxlat": "45.140811",
            "maxlon": "4.0492900"
        },
        "limit": 50,
        "cutFrom": ", Haute-Loire"
    },
    "preloaded": true
}
```

Options
========

Mandatory options:
 * **overpassURL** - points to the overpass service to use
 * **nominatimURL** - points to the nominatim service to use
 * **boundingbox** - bounding box where restrict the search
 * **limit** - maximum number of results
 * **cutFrom** - Some text to find in the location name displayed from where to cute (the cutFrom text is also cutted)
