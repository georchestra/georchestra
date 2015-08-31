StreetView ADDON
================

This addon allows your users to get a picture taken from a given place, using the StreetView API.
Please refer to https://developers.google.com/maps/documentation/streetview/ for the API documentation and Terms of Use.

author: @fvanderbiest


Typical configuration to include in your GEOR_custom.js file:

    {
        "id": "streetview_0", // unique & stable string identifier for this addon instance
        "name": "Streetview",
        "title": {
            "en": "StreetView",
            "fr": "StreetView",
            "es": "StreetView",
            "de": "StreetView"
        },
        "description": {
            "en": "Get a detailed view of a place you choose on the map",
            "es": "Obtener una vista detallada de un lugar que usted elija en el mapa",
            "fr": "Obtenez une vue détaillée d'un endroit que vous choisissez sur la carte",
            "de": "Sehen Sie eine Detailansicht eines Ortes, den Sie auf der Karte auswählen"
        },
        options: {
            "api_key": "AIzaSyDSPmOyzuhJA_tM5_RF0tLLIuQFV6sBJ1U",
            "initial_window_size": 640
        }
    }


The default options for this addon are specified in the manifest.json file:

    "default_options": {
        "api_key": "AIzaSyDSPmOyzuhJA_tM5_RF0tLLIuQFV6sBJ1U",
        "initial_window_size": 400,
        "initial_fov": 90,
        "initial_pitch": 0
    }

The above StreetView API key is for dev purposes only (works on localhost and sdi.georchestra.org). 
Put your own instead. This is a required option.

You may also grow the ```initial_window_size``` option up to 640 pixels.

The field of view (```initial_fov```) may range from 10 to 120 degrees. Smaller numbers means "more zoomed in".

The pitch ranges from -90 to 90, and 0 often means "flat horizontal" (but not always).