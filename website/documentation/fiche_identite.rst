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

Liferay, mais il n'y a pas de connexion forte entre le CMS et l'infrastructure de données spatiale.
geOrchestra peut s'intégrer facilement dans tout site web.


Date de première version
--------------------------------------------------
2000

Nombre de versions parues
--------------------------------------------------
6

Nombre d'utilisateurs / installations
--------------------------------------------------
Plusieurs millions

Quel pilotage des évolutions ?
--------------------------------------------------

Classique : plusieurs sociétés contribuent à ce projet OpenSource sur la base de demandes client.

Quel financement des évolutions ?
--------------------------------------------------

Via société éditrice et partenaires.

Le CMS comprend-il une gestion de groupes de travail ?
--------------------------------------------------

?


Quelle est la liaison avec le catalogue de métadonnées ? (affichage des dernières MD créées, des MD les plus consultées…)
--------------------------------------------------

Via agrégateur de flux RSS : affichage des dernières MD créées & via recherche personnalisée

Y a-t-il localisation cartographique des contenus du CMS ?
--------------------------------------------------

?


Administration du portail (CMS)
====================

Existe-til un module de gestion des conventions d'échange ?
--------------------------------------------------
?

Quelle est la granularité de gestion des droits ? (par contenus, par fonctionnalité, sur les fichiers...)
--------------------------------------------------
To Be Done

Comment est organisée la documentation (arborescence de fichiers ? Bases de données documentaire ?) et quelle liaison avec les éléments du CMS ?
--------------------------------------------------

La documentation du projet geOrchestra est disponible :
 - dans une webapp "doc" déployée avec l'IDS pour ce qui est de la documentation utilisateur
 - sur le site web georchestra.org pour ce qui est de la documentation administrateur

Comment se fait la restitution des statistiques de consultation ?
--------------------------------------------------
?


Outil de visualisation cartographique
====================

Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------
Un développement spécifique a été réalisé car aucun visualiseur existant ne remplissait les fonctions demandées.
Le visualiseur a été développé sur la base des technologies clientes OpenLayers, GeoExt, ExtJS et Spring, MapFish print pour la partie serveur.

Il s'interface (au sens: "communique") avec le serveur cartographique via les normes OGC WMS, WFS, SLD, FE.

Date de première version
--------------------------------------------------
2010

Nombre de versions parues
--------------------------------------------------
24 au 19 septembre 2011, cf http://csm-bretagne.fr/hudson/job/georchestra/

Nombre d'utilisateurs / installations
--------------------------------------------------
De l'ordre de la dizaine.

Quel pilotage des évolutions ?
--------------------------------------------------
Via le PSC, cf `http://www.georchestra.org/documentation/psc.html <http://www.georchestra.org/documentation/psc.html>`_

Quel financement des évolutions ?
--------------------------------------------------
Dans l'exprit le plus traditionnel du développement logiciel libre, c'est-à-dire, par le biais de projets financés mais également contributions externes bénévoles.

Quelle est la liaison avec les métadonnées du catalogue pour chaque couche ? (par la légende ? Possibilité de rechercher directement les MD dans l'interface ?)
--------------------------------------------------
Au niveau de la légende de chaque couche, le visualiseur affiche un lien vers la fiche de métadonnées correspondante. Celle-ci s'ouvre dans une nouvelle fenêtre.
L'utilisateur peut également rechercher dans le catalogue en utilisant les mots clés des métadonnées, tout en restant dans l'interface du visualiseur.

Quels services web OGC peuvent être consultés dans l'interface ? Posent-ils des problèmes de performances ?
--------------------------------------------------
Les services WMS, WMS-C & WFS peuvent être nativement consultés et requêtés dans le visualiseur.

Il y a cependant des limitations :
 - les WMS-C sont consultables uniquement via chargement de fichiers de contexte (OGC WMC)
 - les WFS sont soumis aux limites intrinsèques des navigateurs en terme de nombre d'objets et de vertex simultanément affichables (et cette limite varie fortement selon le naviateur choisi)
 
Pour passer outre ces limitations, il y a la possibilité de "proxifier" les couches WMS-C et WFS via le moteur cartographique de la plateforme (GeoServer), qui permettra de les afficher sous la forme de couches WMS, tout en conservant une très bonne ergonomie de consultation (notamment grâce au mode tuilé).


Est-il possible de sauvegarder et exporter des dessins ?
--------------------------------------------------

Non


Existe-til un outil de production collaborative de données ?
--------------------------------------------------

Oui, il s'agit d'un éditeur collaboratif basé sur le protocole OGC WFS-T.
Cf http://www.georchestra.org/documentation/utilisateur/edition.html pour une présentation plus complète.


Existe-t-il un module de téléversement de données ?  Comment se fait le stockage (fichiers plats, injection en base) ? Qui a accès à ces données ?
--------------------------------------------------

Non, pas dans le visualiseur.
Oui, au niveau du catalogue : tout type de données (PDF, CSV, JPG, PNG ...) peut être "attaché" à une fiche de métadonnées. Le stockage est alors effectué dans un espace dédié sur le disque. L'accès à ces données peut être limité pour chaque fiche de métadonnées, sur la base du système de gestion des droits (organismes/rôles).


Possibilité de sauvegarder/charger un contexte (créés par l'admin ou par des utilisateurs) ? [contexte cartographique au sens OGC]
--------------------------------------------------

Dans le visualiseur, toute composition cartographique est OGC WMC :
 - l'administrateur choisit le contexte par défaut du visualiseur sous la forme d'un fichier WMC,
 - l'administrateur peut créer un certain nombre de contextes et proposer autant de liens vers des visualiseurs avec des thèmes distincts,
 - tout utilisateur peut sauvegarder (sur son poste) sa composition cartographique au format WMC,
 - tout utilisateur peut restaurer un contexte cartographique qu'on lui aura communiqué au format WMC,
 - tout utilisateur peut créer un permalien vers sa composition cartographique en appelant le visualiseur avec le paramètre ?wmc= suivi d'une URL vers le fichier WMC.
 

Outil de visualisation cartographique : backOffice
====================

Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------

Nous utilisons le moteur cartographique GeoServer 2, dont l'interface d'administration présente une très bonne ergonomie.
Seuls les utilisateurs munis du rôle "ADMINISTRATOR" ont accès à cette interface.
L'interface est intégrée à la suite d'applications via un bandeau reprenant les fonctionnalités de l'IDS (catalogue/visualiseur/extracteur/administration)


Est-il possible de créer ou d'activer des webservices (flux interopérables, WMS ou WFS) 
--------------------------------------------------

Tout à fait, et sans avoir besoin de taper une seule ligne de commande : en ligne depuis l'interface de GeoServer.
Chaque couche est activable/désactivable à volonté.


Est-il possible de décliner l'application cartographiques pour créer des visualiseurs thématiques ? 
--------------------------------------------------

Oui, cf ci-dessus (contextes OGC WMC)


Comment se fait le paramétrage de nouveaux formulaires de requête et leur implémentation ? Est-elle facile ? Peut-on gérer les droits sur ces formulaires ? (par exemple, créer des masques de recherche métier liés à une couche particulière de la base)
--------------------------------------------------

Il n'y a pas de notion de formulaire de requête pré-existant : tout utilisateur est en mesure de créer sa propre requête métier sur toute couche WMS/WFS en utilisant des critères attributaires et géométriques.


L'application produit-elle du geoRSS ?
--------------------------------------------------

Oui, en utilisant la syntaxe suivante pour une couche nommée ns:test : 
http://ids.monserveur.org/geoserver/wms/reflect?layers=ns:test&format=rss


Demandes d'extraction : FrontOffice	
====================

Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------
Un développement spécifique a été réalisé car aucun système d'extraction existant ne remplissait les fonctions demandées.
L'extracteur a été développé sur la base des technologies clientes OpenLayers, GeoExt, ExtJS et Spring pour la partie serveur.

Il reçoit ses demandes de traitement par le contenu JSON qui lui est directement POSTé.
Il est en mesure de traiter les couches WMS/WFS servies par le moteur cartographique local ou par des serveurs OGC distants.

Date de première version
--------------------------------------------------
2010, idem module visualiseur.

Nombre de versions parues
--------------------------------------------------
Idem module visualiseur.

Nombre d'utilisateurs / installations
--------------------------------------------------
Probablement une petite dizaine.

Quel pilotage des évolutions ?
--------------------------------------------------
Idem module visualiseur.

Quel financement des évolutions ?
--------------------------------------------------
Idem module visualiseur.


Comment sont gérées les demandes d'extraction : par donnée, en une seule fois, par gestion d'un panier ?
--------------------------------------------------
Gestion d'un panier, traité comme un lot unique de plusieurs couches de données.


L'outil d'extraction est-il accessible rapidement depuis tous les modules (catalogue, visualiseur, CMS)
--------------------------------------------------
Il est accessible rapidement depuis le catalogue (lien via le menu "actions sur la sélection") et depuis le CMS (intégration directe).
Le lien depuis le visualiseur sera implémenté au cours de l'année 2012 (cf ticket http://csm-bretagne.fr/redmine/issues/2049)


Les demandes d'extraction se font par périmètre dessiné ou par sélection de communes ? 
--------------------------------------------------
Les deux modalités sont possibles.
Noter que, pour la sélection par communes, tout autre type d'objet polygonal est acceptable (ex: emprise de forêts), du moment que celui-ci est servi en WFS par GeoServer dans l'espace de nommage configuré.
Noter enfin que le périmètre d'extraction peut être agrémenté d'un buffer configurable.


Quel est le découpage des données téléchargées ? (rectangle d'emprise, découpage par le polygone de demande?)
--------------------------------------------------
Le découpage est effectué selon un rectangle qui peut être soit l'emprise native de la couche, soit une emprise définie manuellement, soit une emprise correpondant à un objet de la base de données, éventuellement augmenté d'un buffer.


Y a-t-il gestion d'un mailing lors de demandes d'extraction ? À quel moment et pour quels destinataires ? Y a-t-il calcul d'une estimation de temps avant la mise à disponibilité des fichiers ?
--------------------------------------------------

Oui, un email est envoyé lors de la prise en compte de la demande d'extraction et lors de la fin du traitement.
Le destinataire est le demandeur, et éventuellement un administrateur de données.
Il n'y a pas d'estimation de temps de traitement.

Les données peuvent être récupérées FTP, sur une URL provisoire ou bien le logiciel gère l'envoi sur support externe ?
--------------------------------------------------

Les données sont récupérées via une URL à usage limité dans le temps.

	
Demandes d'extraction : BackOffice	
====================

Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------
Développement spécifique.

Quelle est la gestion des files d'attentes (validation / invalidation de demandes / changement des empilements) ?
--------------------------------------------------

En cours de développement (sept 2011) : interface de gestion de la file d'attente, changement de priorité d'un job, arrêt d'un job.
Les jobs sont auto-modérés par l'application, sur la base de critères (essentiellement de volumétrie) fixés à l'avance par l'administrateur de l'IDS.


Est-il possible d'accéder aux statistiques de charge serveur pour anticiper des blocages ?
--------------------------------------------------
Il n'y a pas de blocage possible : les demandes trop volumineuses sont refusées et l'utilisateur en est averti par courriel.

Des packages déjà extraits sont-ils proposés de manière préférentielle en fonction des demandes ?
--------------------------------------------------
Pas au niveau de l'extracteur, mais cela est possible via le module catalogue.

Quel est le format de stockage des projections ? Y a-t-il implémentation des bibliothèques IGNF ? Est-il possible d'ajouter aisément des projections/formats ?
--------------------------------------------------
Les projections supportées sont gérées au niveau du code de l'application.

	
Catalogue	
====================
Quel outil a été retenu ? Comment s'interface-t-il avec les autres éléments de l'architecture ?
--------------------------------------------------
GeoNetwork OpenSource. Interfaçage via flux OGC WMS/WFS/WCS & CSW.

Date de première version
--------------------------------------------------
2000

Nombre de versions parues
--------------------------------------------------
> 14 (cf http://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/)

Nombre d'utilisateurs / installations
--------------------------------------------------
Plusieurs milliers.

Quel pilotage des évolutions ?
--------------------------------------------------
PSC, projet OSGeo, cf http://trac.osgeo.org/geonetwork/wiki/PSC

Quel financement des évolutions ?
--------------------------------------------------
Nations Unies et financeurs privés.

Quelle est la liaison avec le portail ?
--------------------------------------------------
Flux RSS pour une intégration fine & intégration via iframe pour consultation complète.

Quelle est la liaison entre une fiche de métadonnées et l'affichage de la donnée elle-même (dans le visualiseur) ?
--------------------------------------------------
Via une liste déroulante, il est possible d'envoyer une sélection de fiches de métadonnées vers le visualiseur, afin de visualiser complètement les couches OGC référencées dans ces fiches de métadonnées.

Quelle est la liaison entre une fiche de métadonnées et la documentation présente dans le CMS ? (carte, document ressource...)
--------------------------------------------------
Une fiche de métadonnées peut lier vers toute page du CMS.

Peut-on visualiser les statistiques de consultation et quelle est la liaison avec les statistiques globales du site ou du portail ?
--------------------------------------------------
?

Quelle est la liaison avec les demandes de téléchargement (ou le panier) ?
--------------------------------------------------
Il est possible d'envoyer simultanément une ou plusieurs fiches de métadonnées vers l'extracteur, afin de télécharger les données référencées dans ces fiches (via services WMS et/ou WFS)

	
Serveur interopérable	
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




