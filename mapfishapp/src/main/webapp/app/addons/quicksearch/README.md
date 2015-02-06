QuickSearch ADDON
=================

This addon allows users to query layer objects across different services using "typeahead" search.
author: @fvanderbiest

Compatibility: geOrchestra >= 14.12

Example addon config:

```js
    {
        "id": "quicksearch_0",   // unique & stable string identifier for this addon instance
        "name": "Quicksearch",
        "options": {
            "field": "label",    // [mandatory] field common to all layers for query and display
            "tip": {             // [mandatory] tooltip displayed on field hover
                "fr": "<b>Recherche rapide d'une référence</b><br>Tapez \"d numero_de_dossier\" pour obtenir l'affichage du dossier",
                "en": "...",
                "es": "...",
                "de": "..."
            },
            "searches": [{       // [mandatory] "searches" lists the patterns and related layers to query
                "pattern": "d ([0-9]+)", // example: "d 145896" for document n° 145896
                "service": "https://opendata.agglo-lepuyenvelay.fr/geoserver/wfs",
                "featureNS": "http://opendata.agglo-lepuyenvelay.fr/geoserver/cadastre",
                "featureType": "dossier"
            }, {
                "pattern": "([0-9]+ .+)", // example: "45 rue Droite"
                "service": "https://opendata.agglo-lepuyenvelay.fr/geoserver/wfs",
                "featureNS": "http://opendata.agglo-lepuyenvelay.fr/geoserver/geoloc",
                "featureType": "geo_adresse"
            },{
                // search with no pattern means that it catches all requests which do not match any of the above patterns
                "service": "https://opendata.agglo-lepuyenvelay.fr/geoserver/wfs",
                "featureNS": "http://opendata.agglo-lepuyenvelay.fr/geoserver/geoloc",
                "featureType": "geo_voie"
            }],
            "target": "tbar_11", // [optional] field is placed at eleventh position in top toolbar
            "minChars": 4,       // [optional] minimum number of characters to type before request is fired
            "maxResults": 10,    // [optional] maximum number of query results
            "graphicStyle": {    // [optional] graphic style for the geographical features
                "graphicName": "cross",
                "pointRadius": 16,
                "strokeColor": "fuchsia",
                "strokeWidth": 2,
                "fillOpacity": 0
            }
        },
        "title": {
            "en": "Quick Search",
            "fr": "Recherche rapide",
            "es": "Quick Search",
            "de": "Quick Search"
        },
        "description": {
            "en": "Quick search your favorite objects",
            "fr": "Quick search your favorite objects",
            "es": "Quick search your favorite objects",
            "de": "Quick search your favorite objects"
        }
    }
```

The only constraint is that all layers must share the same query field name.