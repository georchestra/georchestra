OSM Editors Addon
=================

This addon make easy to start an OpenStreetMap edit session in one 
of several editors based on the current geographic extent.

This feature

Authors: @fvanderbiest, @jdenisgiguere, @severo 

Compatibility :  geOrchestra >= 15.12

Example addon config:

```js
[
    {
        "id": "osmeditors_0",
        "enabled": true,
        "name": "OsmEditors",
        "options": {
            "target": "tbar_11",
            "editors": {
                "iD": true,
                "potlach": true,
                "JOSM": true,
                "WalkingPapers": true
            }
        },
        "title": {
            "en": "Edit in OSM",
            "fr": "Édition dans OSM",
            "es": "edición en OSM",
            "de": "Bearbeiten in OSM"
        },
        "description": {
            "en": "Lauch an edit session of the collaborative map OpenStreetMap for current geographic extent",
            "fr": "Lancer l'édition de la carte collaborative OpenStreetMap pour la zone présentée dans le visualisateur",
            "es": "Comenzar a editar el mapa de OpenStreetMap de colaboración para el área que se muestra en el visor",
            "de": "Starten Sie die OpenStreetMap kollaborative Karte für die im Viewer angezeigt Bereich Bearbeitung"
        }
    }
]

```

### Using JOSM

In order to use, JOSM, remote control must be actived.
See http://josm.openstreetmap.de/wiki/Help/Preferences/RemoteControl for
details.


### Setup

No spectific setup is required.