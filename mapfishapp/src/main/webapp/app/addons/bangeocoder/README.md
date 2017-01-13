# BAN Geocoder ADDON

This addon allows users to search for an address from the French "BAN" service ("BAN" stands for "Base Adresse Nationale"). 
Ex : https://adresse.data.gouv.fr/api/

Authors: @PierreJ & @GaetanB

The addon config should look like this:

```json
[{
    "id": "bangeocoder_0",
    "name": "BANGeocoder",
    "enabled": true,
    "title": {
        "en": "Recherche d'adresse",
        "es": "Recherche d'adresse",
        "fr": "Recherche d'adresse"
    },
    "description": {
        "en": "Recherche d'adresses dans la Base d'Addresse National (France)",
        "es": "Recherche d'adresses dans la Base d'Addresse National (France)",
        "fr": "Recherche d'adresses dans la Base d'Addresse National (France)"
    },
    "options": {
        "limitResponse": 5
    }
}]
```

This addon uses the "BAN" autocompletion service (service URL: http://api-adresse.data.gouv.fr/search/) with 2 parameters: 
 * search value: "q=8 bd du port"
 * limit the number of responses (is set to 5 by default) : "limit=5"

Eg: http://api-adresse.data.gouv.fr/search/?q=8%20bd%20du%20port&limit=5

The service returns a GeoJSON string like:
```json
{
    "attribution": "BAN",
    "licence": "ODbL 1.0",
    "query": "8 bd du port",
    "type": "FeatureCollection",
    "version": "draft",
    "features": [{
        "properties": {
            "context": "80, Somme, Picardie",
            "housenumber": "8",
            "label": "8 Boulevard du Port 80000 Amiens",
            "postcode": "80000",
            "citycode": "80021",
            "id": "ADRNIVX_0000000260875032",
            "score": 0.3351181818181818,
            "name": "8 Boulevard du Port",
            "city": "Amiens",
            "type": "housenumber"
        },
        "geometry": {
            "type": "Point",
            "coordinates": [2.29009, 49.897446]
        },
        "type": "Feature"
    }, {
        "properties": {
            "context": "34, H\u00e9rault, Languedoc-Roussillon",
            "housenumber": "8",
            "label": "8 Boulevard du Port 34140 M\u00e8ze",
            "postcode": "34140",
            "citycode": "34157",
            "id": "ADRNIVX_0000000284423783",
            "score": 0.3287575757575757,
            "name": "8 Boulevard du Port",
            "city": "M\u00e8ze",
            "type": "housenumber"
        },
        "geometry": {
            "type": "Point",
            "coordinates": [3.605875, 43.425232]
        },
        "type": "Feature"
    }]
}
```
