Traveler ADDON
================

This addon allows users to calcul routes and isochrone through IGN web services.
This addon includes : 
	- BAN geocoder to localize adress
	- Referential elements to find utilities or layer's entity from GeoServer
	- Draw control to freehanding point
	- Elements to params isochrones request
	- Elements to params route request
	- Function to save isochrones geometry to local storage
	- Function to use points geometry from local storage to create many isochrones 

This addon need : 
	- IGN key to use route services
	- IGN key to use isochrone service (often the same key as route service)
	- GeoServer workspace that contains referentials layer
	
authors: @Getanbru

The addon original config should look like this:

	{
	        "id": "traveler_0",
	        "name": "Traveler",
	        "enabled": true,
	        "preloaded":true,
	        "title": {
	            "en": "Road measuring",
	            "es": "Camino de medición",
	            "fr": "Mesures routières"
	        },
	        "description": {
	            "en": "Travel Tools",
	            "es": "herramientas de viaje",
	            "fr": "Outils de déplacement"
	        },
	        "options": {}
	}
