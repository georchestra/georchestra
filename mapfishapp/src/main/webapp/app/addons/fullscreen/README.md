# Fullscreen

When this addon is loaded and activated, the map is shown fullscreen. 
If the fullscreen API is not supported by the browser, or if the ```toolbars``` options is set to ```true```, side panels are collapsed.

This addon also adds a new behavior to mapfishapp: when loaded, it detects if the ```fullscreen``` GET parameter is set to ```true``` and updates the viewer layout accordingly.

author: @fvanderbiest

Credits to Google for the icons - https://github.com/google/material-design-icons (CC BY 4.0)

## Setup

Edit the `GEOR_custom.js` file to add the required configuration:

```js
ADDONS: [...,
{
    "id": "fullscreen_0", // unique & stable string identifier for this addon instance
    "name": "Fullscreen",
    "title": {
        "en": "Fullscreen",
        "es": "Pantalla Completa",
        "fr": "Plein écran",
        "de": "Vollbild"
    },
    "options": {
        "toolbars": true // remove this for a true fullscreen map
    },
    "description": {
        "en": "Make the map go fullscreen",
        "es": "Mostrar el mapa en pantalla completa",
        "fr": "Voir la carte en plein écran",
        "de": "Siehe Vollbildkarte"
    }
}], ...
```

This addon supports [toolbar integration](../README.md#addon-placement), with eg:
```
    "options": {
        "target": "tbar_11"
    },
```

Note that you can leave the choice between true fullscreen or fullscreen with toolbars to your users by declaring two addon instances, eg:
```js
ADDONS: [...,
{
    "id": "fullscreen_0", // unique & stable string identifier for this addon instance
    "name": "Fullscreen",
    "title": {
        "en": "Fullscreen with toolbars",
        "es": "Pantalla Completa con barras de herramientas",
        "fr": "Plein écran avec barres d'outils",
        "de": "Vollbild mit Symbolleisten"
    },
    "options": {
        "toolbars": true // remove this for a true fullscreen map
    },
    "description": {
        "en": "Make the map go fullscreen, keeping the toolbars",
        "es": "Mostrar el mapa en pantalla completa con barras de herramientas",
        "fr": "Voir la carte avec ses barres d'outils en plein écran",
        "de": "Siehe Vollbildkarte mit Symbolleisten"
    }
}, {
    "id": "fullscreen_1", // unique & stable string identifier for this addon instance
    "name": "Fullscreen",
    "title": {
        "en": "Fullscreen",
        "es": "Pantalla Completa",
        "fr": "Plein écran",
        "de": "Vollbild"
    },
    "description": {
        "en": "Make the map go fullscreen",
        "es": "Mostrar el mapa en pantalla completa",
        "fr": "Voir la carte en plein écran",
        "de": "Siehe Vollbildkarte"
    }
}], ...
```
