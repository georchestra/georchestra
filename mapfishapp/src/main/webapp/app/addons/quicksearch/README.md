QuickSearch ADDON
=================

This addon allows users to query layer objects using "typeahead" search.
author: @fvanderbiest

Example addon config:

    {
        "id": "quicksearch_0", // unique & stable string identifier for this addon instance
        "name": "Quicksearch",
        "options": {
            "searches": [{
                "pattern": "([0-9]+ .+)",
                "service": "https://opendata.agglo-lepuyenvelay.fr/geoserver/geoloc/wfs",
                "layer": "geoloc:geo_adresse",
                "field": "label",
                "filter": "{pattern}*",
                "template": "<b>{label}</b>"
            }, {
                "pattern": "d ([0-9]+)",
                "service": "https://opendata.agglo-lepuyenvelay.fr/geoserver/wfs",
                "layer": "TODO",
                "field": "TODO",
                "filter": "TODO",
                "template": "TODO"
            }, {
                "service": "https://opendata.agglo-lepuyenvelay.fr/geoserver/geoloc/wfs",
                "layer": "geoloc:geo_voie",
                "field": "label",
                "filter": "{pattern}*",
                "template": "<b>{label}</b>"
            }]
        },
        "title": {
            "en": "Quick Search"
        },
        "description": {
            "en": "Quick search your favorite objects"
        }
    }
