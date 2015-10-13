# Fullscreen

When this addon is loaded and activated, the map is shown fullscreen. 
If the fullscreen API is not supported by the browser, side panels are collapsed.

Note: requestFullscreen is deprecated on insecure origins, and support will be removed in the future. You should consider switching your SDI to a secure origin, such as HTTPS. See https://goo.gl/rStTGz for more details.

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
    "options": {},
    "description": {
        "en": "Make the map go fullscreen",
        "es": "Mostrar el mapa en pantalla completa",
        "fr": "Voir la carte en plein écran",
        "de": "Siehe Vollbildkarte"
    }
}], ...
```

Note that this addon supports toolbar integration, with eg:
```
    "options": {
        "target": "tbar_11"
    },
```
