.. _`georchestra.documentation.fiche identité du projet`:

======================================
Fiche d'identité du projet geOrchestra
======================================

.. contents:: Sommaire
  :local:

Portail public (CMS)
====================

Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------


Date de première version
--------------------------------------------------


Nombre de versions parues
--------------------------------------------------


Nombre d'utilisateurs / installations
--------------------------------------------------


Quel pilotage des évolutions ?
--------------------------------------------------


Quel financement des évolutions ?
--------------------------------------------------


Le CMS comprend-il une gestion de groupes de travail ?
--------------------------------------------------


Quelle est la liaison avec le catalogue de métadonnées ? (affichage des dernières MD créées, des MD les plus consultées…)
--------------------------------------------------


Y a-t-il localisation cartographique des contenus du CMS ?
--------------------------------------------------



Administration du portail (CMS)
====================

Existe-til un module de gestion  des conventions d'échange ?
--------------------------------------------------


Quelle est la granularité de gestion des droits ? (par contenus, par fonctionnalité, sur les fichiers...)
--------------------------------------------------


Comment est organisée la documentation (arborescence de fichiers ? Bases de données documentaire ?) et quelle liaison avec les éléments du CMS ?
--------------------------------------------------


Comment se fait la restitution des statistiques de consultation ?
--------------------------------------------------



outils de visualisation cartographique : frontOffice
====================
Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------


Date de première version
--------------------------------------------------


Nombre de versions parues
--------------------------------------------------


Nombre d'utilisateurs / installations
--------------------------------------------------


Quel pilotage des évolutions ?
--------------------------------------------------


Quel financement des évolutions ?
--------------------------------------------------


Quelle est la liaison avec les métadonnées du catalogue pour chaque couche ? (par la légende ? Possibilité de rechercher directement les MD dans l'interface ?)
--------------------------------------------------


Quels services web OGC peuvent être consultés dans l'interface ? Posent-ils des problèmes de performances ?
--------------------------------------------------


Est-il possible de sauvegarder et exporter des dessins  ?
--------------------------------------------------


Existe-til un outil de production collaborative de données ?
--------------------------------------------------


Existe-t-il un module de téléversement de données ?  Comment se fait le stockage (fichiers plats, injection en base) ? Qui a accès à ces données ?
--------------------------------------------------




outils de visualisation cartographique : backOffice
====================

Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------


Est-il possible de créer ou d'activer des webservices (flux interopérables, WMS ou WFS) depuis des contextes (créés par l'admin ou par des utilisateurs) ? [contexte cartographique au sens OGC]
--------------------------------------------------


Est-il possible de décliner l'application cartographiques pour créer des visualiseurs thématiques ? 
--------------------------------------------------


Comment se fait le paramétrage de nouveaux formulaires de requête et leur implémentation ? Est-elle facile ? Peut-on gérer les droits sur ces formulaires ? (par exeple, créer des masques de recherche métier liés à une couche particulière de la base)
--------------------------------------------------


L'application produit-elle du geoRSS ?
--------------------------------------------------




demandes d'extraction : FrontOffice	
====================
Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------


Date de première version
--------------------------------------------------


Nombre de versions parues
--------------------------------------------------


Nombre d'utilisateurs / installations
--------------------------------------------------


Quel pilotage des évolutions ?
--------------------------------------------------


Quel financement des évolutions ?
--------------------------------------------------


Comment sont gérées les demandes d'extraction : par donnée, en une seule fois, par gestion d'un panier ?
--------------------------------------------------


L'outil d'extraction est-il accessible rapidement depuis tous les modules (catalogue, visualiseur, CMS)
--------------------------------------------------


Les demandes d'extraction se font par périmètre dessiné ou par sélection de communes ? Quel est le découpage des données téléchargées ? (rectangle d'emprise, découpage par le polygone de demande?)
--------------------------------------------------


Y a--t-il gestion d'un mailing lors de demandes d'extraction ? À quel moment et pour quels destinataires ? Y -t-il calcul d'une estimation de temps avant la mise à disponibilité des fichiers ?
--------------------------------------------------


Les données peuvent être récupérées FTP, sur une URL provisoire ou bien le logiciel gère l'envoi sur support externe ?
--------------------------------------------------


	
demandes d'extraction : BackOffice	
====================
Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------


Quelle est la gestion des files d'attentes (validation / invalidation de demandes / changement des empilements) ?
--------------------------------------------------


Est-il possible d'accéder aux statistiques de charge serveur pour anticiper des blocages ?
--------------------------------------------------


Des packages déjà extraits sont-ils proposés de manière préférentielle en fonction des demandes ?
--------------------------------------------------


Quel est le format de stockage des projections ? Y a-t-il implémentation des bibliothèques IGNF ? Est-il possible d'ajouter aisément des projections/formats ?
--------------------------------------------------


	
catalogue	
====================
Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------


Date de première version
--------------------------------------------------


Nombre de versions parues
--------------------------------------------------


Nombre d'utilisateurs / installations
--------------------------------------------------


Quel pilotage des évolutions ?
--------------------------------------------------


Quel financement des évolutions ?
--------------------------------------------------


Quelle est la liaison avec le portail ?
--------------------------------------------------


Quelle est la liaison entre une fiche de métadonnées et l'affichage de la donnée elle-même (dans le visualiseur) ?
--------------------------------------------------


Quelle est la liaison entre une fiche de métadonnées et la documentation présente dans le CMS ? (carte, document ressource...)
--------------------------------------------------


Peut-on visualiser les statistiques de consultation et quelle est la liaison avec les statistiques globales du site ou du portail ?
--------------------------------------------------


Quelle est la liaison avec les demandes de téléchargement (ou le panier) ?
--------------------------------------------------


	
serveur interopérable	
====================
Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------


Date de première version
--------------------------------------------------


Nombre de versions parues
--------------------------------------------------


Nombre d'utilisateurs / installations
--------------------------------------------------


Quel pilotage des évolutions ?
--------------------------------------------------


Quel financement des évolutions ?
--------------------------------------------------


L'application produit-elle du WMS (quelles versions) ? 
--------------------------------------------------


L'application produit-elle du WFS (quelles versions) ? 
--------------------------------------------------


A la création d'un flux, comment se fait la création de la fiche de métadonnée associée ?
--------------------------------------------------


L'application produit-elle du WMS-C ?
--------------------------------------------------


L'application produit-elle du WMTS ?
--------------------------------------------------


L'application produit-elle du WCS ?
--------------------------------------------------


L'application produit-elle du WPS ?
--------------------------------------------------


L'application produit-elle du WFS-T ?
--------------------------------------------------


	
Fonctions transverses	
====================
Comment estimer la robustesse des composants ?
--------------------------------------------------


Quelle est l'homogénéité des environnements de développement / framework / langages ?
--------------------------------------------------


Comment estimez-vous, globalement, la facilité d'administration ?
--------------------------------------------------


Comment estimez-vous la force de la communauté (réactivité en cas de demandes liées à des blocages, nombre de contributeurs, hétérogénéité des contributeurs) ?
--------------------------------------------------


SGBD : quelle puissance en cas de requêtes complexes ?
--------------------------------------------------


SGBD : quelle est l'intégration des fonctions géographiques de la plate-forme ? (les fonctions géographiques du SGBD sont-elles implémentées nativement ou bien le moteur cartographique utilisé déploie-t-il ses propres fonctions ?)
--------------------------------------------------


Quel est le niveau d'intégration des différents modules dans le portail, surtout en administration ?
--------------------------------------------------


Prise en main / courbe d'apprentissage
--------------------------------------------------


Sécurité : quelle authentification des utilisateurs ? (CAS, LDAP...)
--------------------------------------------------


Sécurité : quelle sécurisation des échanges avec l'internaute sur les données sensibles ?
--------------------------------------------------


Sécurité  : quelle réactivité de l'éditeur en cas de failles de sécurité et mises à jour ?
--------------------------------------------------


Quelle fréquence de mise à jour, quelle facilité de déploiement ?
--------------------------------------------------




