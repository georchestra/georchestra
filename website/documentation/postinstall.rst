.. _`georchestra.documentation.postinstall`:

=============================
Procédure post-installation
=============================

Configuration des référentiels
===============================

mapfishapp et extractorapp propose chacun uneliste déroulante de couches 
référentes. Pour que ces couches apparaissent il faut configurer correctement 
GeoServer.

Tout d'abord il faut générer une couche aevc une seule colonne attributaire, 
généralement le nom de l'entité. Par exemple pour une couche *commune*, il faut 
un champ attributaire *nom_commune*. Le nom du champ importe peu.

Ensuite il faut importer cette couche dans GeoServer dans un namespace spécifique 
appelé *geob_loc* et l'activer dans le service WFS.

La couche devrait apparaitre dans la liste déroulante.

Mapfishapp
===========

Personnalisation de la liste des WMS
--------------------------------------

La liste des services WMS proposés dans la fenêtre de chargement des couches peut 
être personnalisé à l'aide du fichier de configuration 
*src/main/webapp/app/js/GEOB_wmslist.js*

Personnalisation des couches chargées au démarrage
---------------------------------------------------

Cela s'effectue dans le fichier à la racine de la mapfishapp : *src/main/webapp/dev-default.wmc*