# LocateMe

This addon allows users to display their position on the map.
author: @fvanderbiest

## Setup

Edit the `GEOR_custom.js` file to add the required configuration:

```js
ADDONS: [...,
{
    "id": "locateme_0",
    "name": "LocateMe",
    "options": {
        "target": "bbar_7"
    },
    "title": {
        "fr": "Localisez moi",
        "en": "Locate me",
        "es": "localizarme",
        "de": "Wo bin ich ?"
    },
    "description": {
        "fr": "Cet addon permet d'afficher votre position sur la carte",
        "en": "This addon allows you to display your position on the map",
        "es": "Esta herramienta le permite mostrar su posición en el mapa",
        "de": "Dieses Addon ermöglicht Ihnen, Ihre Position auf der Karte angezeigt werden"
    }
}], ...
```
