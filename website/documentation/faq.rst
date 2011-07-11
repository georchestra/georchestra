.. _`georchestra.documentation.faq`:

=======
F.A.Q.
=======

.. contents:: Sommaire
  :local:

Gouvernance
============

À quoi sert le PSC (Project Steering Committee) ?
--------------------------------------------------

Le PSC est le bureau du projet : il décide des grandes orientations du projet, 
permet de résoudre les problèmes (techniques ou non). Il est constitué de 
contributeur majeur de la communauté.

Ses membres sont élus par les commiteurs du projet (voir le document 
:ref:`georchestra.documentation.psc` pour plus d'informations).

Comment contacter le PSC (Project Steering Committee) ?
---------------------------------------------------------

Une adresse permet de le contacter : psc chez georchestra point org.

Comment obtenir un accès à la forge ?
--------------------------------------

Le site de suivi des développements de GeOrchestra est maintenant ouvert à 
toute personne intéressée par l'actualité du projet et sa feuille de route.

Dans la phase actuelle de développement et jusqu'à la livraison de la version 1 
stable, vous pouvez ainsi être informés en temps réel : annonces, soumissions 
de bogues, modifications du code source, recette, ...

Pour accéder à l'ensemble des ressources, trois étapes :

1. rendez-vous sur http://csm-bretagne.fr/redmine/account/register pour 
vous enregistrer sur la plate-forme avec l'identifiant et l'adresse de 
messagerie de votre choix
2. contactez les administrateurs du projet geOrchestra sur info@georchestra.org 
en rappelant votre identifiant, afin qu'il valide votre inscription au projet.
3. une fois la confirmation reçue par messagerie, rendez-vous sur 
http://csm-bretagne.fr/redmine/projects/show/geobretagne2 pour avoir accès à 
l'ensemble des informations

Technique
==========

Quels sont les standards implémentés dans geOrchestra ?
--------------------------------------------------------

geOrchestra utilise les standards proposées par l'OGC, notamment :

* `WMS <http://www.opengeospatial.org/standards/wms>`_ : Web Map Service
* `WMTS <http://www.opengeospatial.org/standards/wmts>`_ : Web Map Tile Service
* `WFS <http://www.opengeospatial.org/standards/wfs>`_ : Web Feature Service
* `WCS <http://www.opengeospatial.org/standards/wcs>`_ : Web Coverage Service
* `WMC <http://www.opengeospatial.org/standards/wmc>`_ : Web Map Context
* `SLD <http://www.opengeospatial.org/standards/sld>`_ : Styled Layer Descriptor
* `FE <http://www.opengeospatial.org/standards/filter>`_ : Filter Encoding
* `CSW <http://www.opengeospatial.org/standards/cat>`_ : Catalog Service

Quelle est cette limite de 114 Ko qui apparait parfois dans mapfishapp ?
-------------------------------------------------------------------------

C'est une limite dans le code javascript de la Mapfishapp, afin d'éviter des 
traitements potentiellement trop lourds coté client. On limite volontairement la 
taille des flux XML acceptables, et cette limite est évolutive selon le 
navigateur. Exemple : IE 6 => faible capacité à parser du XML => limite basse.

Il y a trois raisons pour que cela arrive :

* la liste de tous les codes EPSG est listé dans le GetCapabilities ;
* toutes les couches sont dans le même namespace ou vous n'utilisez pas de 
  namespace dans l'URL des services (http://monserver/geoserver/wms par exemple) ;
* vous avez trop de couches dans le namespace (même raison que le point précédent).

Au final cela entraine une taille trop importante pour le fichier XML du 
GetCapabilities.

Pour corriger ce problème :

* lister les codes EPSG qui doivent être disponible dans GeoServer ;
* placer les couches dans différents namespace ;
* utiliser l'url avec le namespace : http://monserver/geoserver/monNamespace/wms.


La recherche par adresse ne fonctionne pas ?
---------------------------------------------

Il faut installer le module :ref:`georchestra.documentation.search_address`.

Comment brancher la recherche référentiels ?
---------------------------------------------

Voir :ref:`georchestra.documentation.postinstall`.


En mode édition, la page ralenti fortement, j'ai un emssage d'erreur
---------------------------------------------------------------------

Les performances JavaScript sont très dépendantes du navigateur web utilisé et 
IE n'est pas réputé pour avoir d'excellentes performances en la matière. 
Généralement on a ce problème dans l'éditeur lorsque l'on veut éditer une couche 
présentant une quantité élevé de sommet/point.

Il n'y a pas de solution.

Comment étendre les types de format possibles dans l'extracteur ?
------------------------------------------------------------------

Ce n'est pas possible pour le moment. Vous pouvez financer cette évolution.

Impossible d’ajouter une couche raster (ecw, jpg2000, ...)
----------------------------------------------------------

Il faut installer des plugins supplémentaires dans GeoServer. Voir le blog de 
`geomatips <http://geomatips.blogspot.com/2010/02/support-de-lecw-dans-geoserver.html>`_ 
ou la doc de GeoServer.
