==================================
Gestion des droits et sécurité
==================================

Introduction
=============

La mise en place de droit d'accès est fondamentale au sein d'une Infrastructure 
de Données Spatiales. Cette page a pour objectif d'expliquer son fonctionnement 
au sein de geOrchestra.

La première partie explique où se trouve la gestion des accès en fonction de 
l'outil utilisé. La section suivante illustre le concept de groupe et de région 
au sein de geOrchestra.

Gestion
=========

Catalogue
----------

La gestion des droits se fait dans l'interface dédié au catalogue. Soit en 
recherchant une fiche de métadonnées (bouton *Autres actions*), soit après 
avoir sauver la fiche de métadonnées, le bouton *Autres actions* est disponible 
en haut de la page.

Serveur cartographique
-----------------------

 * via gssec pour GeoServer 1 :
 
   * **avantage :** 
   
    * permet de structurer les ACL en "groupes" pour ensuite en déléguer la gestion. 
    * Ne nécessite pas un compte administrateur geoserver. 
    * Requiert un accès système pour gérer les délégations.

   * **inconvénient :** 
   
    * ergonomie non adaptée à un grand nombre de couches ou de groupes. 
    * Stabilité à éprouver. 
    * Pas de nettoyage automatique pour les groupes/couches supprimés.

 * GeoServer 2 et supérieur :
   
   * **avantage :** 
   
     * intégré, stable, 
     * fonction de recherche adaptée à un grand nombre de couches, 
     * pas d'accès système nécessaire.

   * **inconvénient :** 
   
     * ne connaît pas la liste des groupes LDAP qui doivent être entrés comme 
       des mots clefs, pas de délégation possible. 
     * Pas de nettoyage automatique pour les groupes/couches supprimés.

Notion de groupe
=================

Il faut comprendre ces groupes comme des "tags" que l'on applique à un utilisateur.

 * EL_* : groupe structurel (accès en fonction de la structure)
 * SV_* : groupe fonctionnel (accès en fonction des fonctionnalités, si j'ose dire)

.. note::
    Par défaut, toutes les couches peuvent être accéder en lecture, aucune ne 
    l'est en écriture.

Exemple de groupe structurel :

 * EL_C2C
 * EL_adherent
 * EL_nonadherent
 * EL_OSM
 * EL_BRETAGNE
 * EL_BREST

*user1* appartient à EL_C2C et EL_adherent. Par contre *user2* appartiendra à 
EL_C2C, EL_adherent et EL_OSM.

Exemple de groupe fonctionnel :

 * SV_ADMIN : big boss
 * SV_USER : peut s'authentifier
 * SV_EDITOR : édition du catalogue
 * SV_REVIEWOR : si l'organisme a des sous, des utilisateurs qui peuvent relire 
   les fiches de catalogues

.. Notez que SV_EDITOR donne des droits dans mapfishapp/editor mais cela n'est 
   pas cohérent car l'utilisateur a un accès en écriture en fonction de son 
   groupe structurel + des droits côté GS (soit via gssec, soit directement par GS2)

Notion de région
==================

Il s'agit plutôt de groupe d'ACL plutôt que de "région" au sens localisation. 
L'intérêt est de pouvoir permettre la  délégation de la gestion des ACL des 
couches à un utilisateur.

**Exemple :** 

L'utilisateur *user2* appartient au groupe EL_ADMIN_OSM, on créé un fichier de configuration
OSM.properties qui fait le lien entre le nom du groupe d'ACL et le groupe 
LDAP (EL_ADMIN_OSM par exemple) et cet utilisateur, après qu'on lui 
ait donné un droit sur une couche, pourra gérer les accès pour ses collègues. 
Il ne verra et pourra modifier que les acl au sein de ce groupe/région.
