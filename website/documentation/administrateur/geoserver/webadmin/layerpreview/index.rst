.. _layerpreview:

Prévisualisation de couches
===========================

Cette page propose des vues de couches dans différents formats de sortie. Notez 
qu'une couche doit être activée pour être prévisualisée.

.. figure:: ../images/preview_list.png
   :align: center
   
   *Page de prévisualisation de couches*

Chaque ligne de couche affiche un type, un nom un titre et les formats disponibles 
pour visualisation.

.. list-table::
   :widths: 5 95 

   * - **Champ**
     - **Description**

   * - .. image:: ../images/data_layers_type1.png
     - Couche raster (grille)
   * - .. image:: ../images/data_layers_type2.png
     - Couche vecteur (entité)  
   * - .. image:: ../images/preview_layergroup.png
     - Groupe de couche 

Le nom fait référence à l'espace de travail et au nom de la couche, alors que le 
titre fait référence à la courte description définie dans la partie 
:ref:`edit_layer_data`. Dans l'exemple suivant, nurc fait référence à l'espace de 
travail, Arc_Sample au nom de la couche et "A sample ArcGrid field" a été définie 
dans la partie édition des données de la couche.


.. figure:: ../images/preview_row.png
   :align: center
   
   *Une ligne de prévisualisation de couche unique*

Formats de sortie
-------------------
La page de prévisualisation des couches gère différents formats de sortie pour 
des utilisations diverses ou du partage de données. Vous pouvez prévisualiser 
toutes les types de couches dans les formats OpenLayers et KML. Parallèlement 
en utilisant la liste déroulante "Tous les formats" vous pouvez voir tous les 
types de couches dans une demi douzaine de formats supplémentaires -- AtomPub, 
GIF, GeoRss, JPEG, KML (compressé), PDF, PNG, SVG, et TIFF. Seules les couches 
vecteurs propose des prévisualisation de sortie WFS, dont le format GML et CSV, 
GML3, GeoJSON et Shapefile. Le tableau ci-dessous propose une brève description 
de tous les formats gérés, organisé en type de sortie : image, text ou données.  

Sortie image
`````````````

Toutes les sorties d'images peuvent être réalisées à partir d'une requête GetMap 
WMS sur une données raster, vecteur ou couverture. Les services WMS sont des 
méthodes qui permettent l'affichage visuel de données spatiales sans nécessairement 
fournir un accès aux entités qui composent les données.

.. list-table::
   :widths: 10 90 

   * - **Format**
     - **Description**
     
   * - KML
     - KML (Keyhole Markup Language) est un schéma basé sur le langage XML pour 
       modéliser des données géographiques afin de naviguer sur la terre comme 
       Google Earth ou Google Maps. Le format KML utilise une structure basée sur 
       des balises et des attributs imbriqués. Pour GeoServer, les fichiers KML 
       sont distribués sous la forme de KMZ, qui sont des fichiers KML compressé.
   * - JPEG
     - Sortie WMS en format raster. Le format JPEG est un format de fichier 
       compressé avec perte de qualité dû à la compression. Il est conseillé pour 
       les photos et pas pour la reproduction de données exactes.
   * - GIF
     - Sortie WMS en format raster. Le format  GIF (Graphics Interchange Format) 
       est un format image bitmap utile pour les images en lignes de bordures 
       nettes avec un nombre limité de couleur. Cela permet d'optimiser la 
       compression sans perte du format qui favorise lezs zones étendues de même 
       couleur avec des limites nettes (contrairement au format JPEG, qui favorise 
       les images avec des gradients de couleurs). Le format GIF est limité à une 
       palette de 8 bit ou 256 couleurs.
   * - SVG
     - Sortie WMS de format vecteur. SVG (Scalable Vector Graphics) est un langage 
       pour modéliser les graphiques en deux dimensions en XML. Il diffère des 
       formats GIG et JPEG dans la mesure où il utilise des objets graphiques 
       plutôt que des points individuels.   
   * - TIFF
     - Sortie WMS en format raster. Le format TIFF (Tagged Image File Format) est 
       un format flexible, adaptable pour prendre en charge des données multiples 
       dans un seul fichier. GeoTIFF contient des données géographiques incluses 
       sous forme de balises dans le fichier TIFF.
   * - PNG
     - Sortie WMS en format raster. Le format de fichier  PNG (Portable Network Graphics) a 
       été créé comme le successeur libre et open source au GIF. Le format de 
       fichier PNG gère le truecolor (16 million de couleurs) tandis que le GIF 
       gère seulement 256 couleurs. Le fichier PNG est performant lorsque l'image 
       a des zones larges et de couleurs uniformes.
   * - OpenLayers
     - Sortie de la requête GetMap du WMS comme fenêtre de prévisualisation 
       OpenLayers. `OpenLayers <http://openlayers.org/>`_ est une bibliothèque 
       JavaScript open source pour afficher des cartes dans des navigateurs web. 
       La sortie OpenLayers possède des filtres avancés qui ne sont pas disponibles 
       lors de l'utilisation seule d'OpenLayers. De plus, la prévisualisation 
       générée contient un en-tête qui facilite les options de configuration pour 
       l'affichage.
   * - PDF
     - Un fichier PDF (Portable Document Format) encapsule une description 
       complète d'une mise en page définie d'un document 2D, dont du texte, des 
       polices des images raster et des graphiques vecteurs 2D.
 
.. figure:: ../images/preview_openlayers.png
   :align: left
   
   *Exemple de sortie image - une prévisualisation OpenLayers de la couche nurc:Pk50095*

Sortie texte
````````````

.. list-table::
   :widths: 10 90 

   * - **Format**
     - **Description**

   * - AtomPub
     - Sortie WMS de données spatiales au format XML. Le format AtomPub (Atom 
       Publishing Protocol) est un protocole d'application pour publier et éditer 
       des ressources web en utilisant le protocol HTTP et le langage XML. 
       Développé pour remplacer la famille des standarads RSS pour la syndication 
       de contenu, Atom permet la subscription de données géographiques.
   * - GeoRss
     - Sortie de la requête GetMap du WMS de données vecteurs au format XML. Le 
       format RSS (Rich Site Summary) est un format XML pour délivrer régulièrement 
       du contenu web modifiée. `GeoRss <http://www.georss.org>`_ est un standard 
       pour encoder la localisation sous forme de flux RSS. La prévisualisation 
       de couches produit des documents RSS 2.0 avec des géométries simples 
       GeoRSS en utilisant Atom.
   * - GeoJSON
     - `JavaScript Object Notation <http://json.org/>`_ (JSON) est un format légé 
       d'échange de données basé sur le langage de programmation JavaScript. Cela 
       en fait un format d'échange idéal pour les applications basées sur les 
       navigateurs puisqu'il peut être lu directement et facilement en JavaScript. 
       GeoJSON est un format de sortie en texte qui ajoute des types géographiques 
       au JSON.
   * - CSV
     - Sortie du GetFeature du WFS en format textuel séparé par des virgules. Les 
       fichiers CSV (Comma Separated Values) sont des fichiers textes contenat 
       des lignes de données. Les valeurs de données dans chaque ligne sont 
       séparées par des virgules. Les fichiers CSV contiennent aussi un en-tête 
       séparé par des virgules dénommant chaque valeurs des lignes. Les CSV de 
       GeoServer sont entièrement en streaming, avec aucune limitation de la 
       quantité de données qui peuvent être renvoyées.

Un échantillon d'un GeoRSS simple pour la couche nurc:Pk50095 en utilisant Atom :
::

   <?xml version="1.0" encoding="UTF-8"?>
	<rss xmlns:atom="http://www.w3.org/2005/Atom"
	     xmlns:georss="http://www.georss.org/georss" version="2.0">
	   <channel>
		 <title>Pk50095</title>
		 <description>Feed auto-generated by GeoServer</description>
		 <link>></link>		
		 <item>
		   <title>fid--f04ca6b_1226f8d829e_-7ff4</title>
		   <georss:polygon>46.722110379286 13.00635746384126 
			46.72697223230676 13.308182612644663 46.91359611878293
			13.302316867622581 46.90870264238999 12.999446822650462 
			46.722110379286 13.00635746384126
		   </georss:polygon>
		   </item>
	   </channel>
   </rss>

Sortie de données
``````````````````

Toutes les sorties de données sont réalisées par une requête GetFeature du WFS sur 
des données vecteurs.

.. list-table::
   :widths: 10 90 

   * - **Format**
     - **Description**

   * - GML2/3
     - GML (Geography Markup Language) is the XML grammar defined by the `Open Geospatial Consortium <http://en.wikipedia.org/wiki/Open_Geospatial_Consortium>`_ (OGC) to express geographical features. GML serves as a modeling language for geographic systems as well as an open interchange format for geographic data sharing.  GML2 is the default (Common) output format, while GML3 is available from the "All Formats" drop down menu.
   * - Shapefile
     - The ESRI Shapefile or simply a shapefile is the most commonly used format for exchanging GIS data.  GeoServer outputs shapefiles in zip format, with a directory of .cst, .dbf, .prg, .shp, and .shx files. 
     

     
     



.. yjacolin at free.fr 2011/11/18 r13133
