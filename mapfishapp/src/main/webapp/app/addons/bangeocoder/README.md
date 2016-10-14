BAN Geocoder ADDON
==================

**** This addon allows user to search adress from BAN service in France. 
Ex : https://adresse.data.gouv.fr/api/

**** This addon use autocompletion service (GET method) with 3 parameters : 
 service URL : http://api-adresse.data.gouv.fr/search/?
 search value : "q=8 bd du port"
 limit of responses (is set to 5 by default) : "limit=5"  
Ex : http://api-adresse.data.gouv.fr/search/?q=8 bd du port&limit=5

**** The service return GeoJSON like that :

    /!\ Default SRS code is EPSG:4326 /!\

{
	"attribution": "BAN",
	"licence": "ODbL 1.0",
	"query": "8 bd du port",
	"type": "FeatureCollection",
	"version": "draft",
	"features": [
		{
			"properties":
			{
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
			"geometry":
			{
				"type": "Point",
				"coordinates": [2.29009, 49.897446]
			},
			"type": "Feature"
		},
		{
			"properties":
			{
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
			"geometry":
			{
				"type": "Point",
				"coordinates": [3.605875, 43.425232]
			},
			"type": "Feature"
		}
	]
}

-------------------
**** config.json file

the addon config should look like this:

[{
        "id": "bangeocoder_0",
        "name": "BANGeocoder",
        "enabled": true,
        "title": {
            "en": "Adress recentering",
            "es": "Adress recentering",
            "fr": "Recherche d'adresse"
        },
        "description": {
            "en": "Adress recentering",
            "es": "Adress recentering",
            "fr": "Recherche d'adresses dans la Base d'Addresse National (France)"
        },
        "options": {
            "limitResponse":5,
            "geocodeServiceUrl":"https://api-adresse.data.gouv.fr/search/?"
        }
}]



**** To change service set "the geocodeServiceUrl". 
    
    /!\/!\/!\ Warning /!\/!\/!\  
    The fields store are compatible only with the response of the default URL. 
    You need to adapt store and _onComboSelect method to get correct responses and correct geometry.
     
**** To get more than 5 responses, set the "limitResponse". If not set, service return 5 results.

-------------------
**** manifest.json file

to set traduction value, modify this file. 

--------------------
authors : Gfi Informatique - @PierreJ & @GaetanB