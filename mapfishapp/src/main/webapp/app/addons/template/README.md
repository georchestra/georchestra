Addon title
===========

Description of the addon in few sentences.

Authors: @gitusername, @anothergitusername

Compatibility :  geOrchestra >= 16.06

Example addon config:

```js
    {
        "id": "template_0",   // unique & stable string identifier for this addon instance
        "name": "Addon Name",
        "options": {
            "target": "tbar_11", // [optional] field is placed at eleventh position in top toolbar
            "graphicStyle": {    // [optional] graphic style for the geographical features
                "graphicName": "cross",
                "pointRadius": 16,
                "strokeColor": "fuchsia",
                "strokeWidth": 2,
                "fillOpacity": 0
            }
        },
        "title": {
            "en": "Addon title in English",
            "fr": "Titre de l'extension en français",
            "es": "Titular en español",
            "de": "Titel"
        },
        "description": {
            "en": "Description in English",
            "fr": "Description en Français",
            "de": "Bezeichnung",
            "es": "Descripción en español"
        }
    }
```

### Setup

Description of setup steps goes here.