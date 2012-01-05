.. _geometry_transformations:

Transformations de géométries en SLD
====================================

En SLD 1.0, chaque symboliseur contient un élément `<Geometry>` qui permet à l'utilisateur de spécifier quelle géométrie employer pour le rendu. Dans la plupart des cas ce n'est pas utilisé, mais cela peut devenir utile lorsqu'un objet comprend plusieurs géométries.

SLD 1.0 exige que le contenu de `<Geometry>` soit une propriété `<ogc:PropertyName>`, GeoServer assouplit cette contrainte et permet d'utiliser à la place une expression générique `sld:expression`. Les expressions usuelles ne savent pas opérer sur les géométries, mais GeoServer fournit un lot de fonctions filtres qui peuvent effectivement manipuler les géométries en les transformant en quelque chose de différent: nous appelons cela *transformations de géométries* en SLD.

Une liste complète des transformations disponibles est disponible dans :ref:`filter_function_reference`.

Les transformations sont plutôt souples, leur principale limitation est qu'elles interviennet dans le système de référence et les unitées propres à la géométrie, avant toute reprojection et rééchantillonnage pour affichage.

Voyons quelques exemples.

Extraction des sommets
----------------------

Voici un exemple qui permet d'extraire tous les sommets d'une géométrie pour les rendre visibles sur une carte, en utilisant la fonction `vertices`:

.. code-block:: xml 
   :linenos: 

      <PointSymbolizer>
        <Geometry>
          <ogc:Function name="vertices">
             <ogc:PropertyName>the_geom</ogc:PropertyName>
          </ogc:Function>
        </Geometry>
        <Graphic>
          <Mark>
            <WellKnownName>square</WellKnownName>
            <Fill>
              <CssParameter name="fill">#FF0000</CssParameter>
            </Fill>
          </Mark>
          <Size>6</Size>
        </Graphic>
     </PointSymbolizer>

:download:`Consultez et téléchargez le SLD complet "Sommets" <artifacts/vertices.sld>`

Appliqué à la couche exemple `tasmania_roads`, ceci aboutit à:

.. figure:: images/vertices.png
   :align: center
   
   *Extraire et afficher les sommets extraits d'une géométrie*
   
   
Points début et fin
-------------------

Les fonctions `startPoint` et `endPoint` peuvent être utilisées pour extraire les points début et fin d'une ligne. 

.. code-block:: xml
   :linenos:
     
   <PointSymbolizer>
     <Geometry>
       <ogc:Function name="startPoint">
         <ogc:PropertyName>the_geom</ogc:PropertyName>
       </ogc:Function>
     </Geometry>
     <Graphic>
       <Mark>
         <WellKnownName>square</WellKnownName>
         <Stroke>
           <CssParameter name="stroke">0x00FF00</CssParameter>
           <CssParameter name="stroke-width">1.5</CssParameter>
         </Stroke>
       </Mark>
       <Size>8</Size>
     </Graphic>
    </PointSymbolizer>
    <PointSymbolizer>
      <Geometry>
        <ogc:Function name="endPoint">
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </ogc:Function>
      </Geometry>
      <Graphic>
        <Mark>
          <WellKnownName>circle</WellKnownName>
          <Fill>
             <CssParameter name="fill">0xFF0000</CssParameter>
          </Fill>
        </Mark>
        <Size>4</Size>
      </Graphic>
    </PointSymbolizer>

:download:`Consultez et téléchargez le SLD complet "Début-Fin" <artifacts/startend.sld>`

Appliqué à la couche exemple `tasmania_roads`, ceci aboutit à:

.. figure:: images/startend.png
   :align: center
   
   *Extraire les points début et fin d'une ligne*


Ombre portée
------------

La fonction `offset` peut être utilisée pour créer un effet d'ombre portée derrière les polygones. Notez la valeur bizarre du décalage, choisie ainsi car les données utilisées dans l'exemple sont exprimées en coordonnées géographiques.

.. code-block:: xml 
   :linenos: 
   
     <PolygonSymbolizer>
       <Geometry>
          <ogc:Function name="offset">
             <ogc:PropertyName>the_geom</ogc:PropertyName>
             <ogc:Literal>0.00004</ogc:Literal>
             <ogc:Literal>-0.00004</ogc:Literal>
          </ogc:Function>
       </Geometry>
       <Fill>
         <CssParameter name="fill">#555555</CssParameter>
       </Fill>
     </PolygonSymbolizer>

:download:`Consultez et téléchargez le SLD complet "Ombre" <artifacts/shadow.sld>`

Appliqué à la couche exemple `tasmania_roads`, ceci aboutit à:

.. figure:: images/shadow.png
   :align: center
   
   *Ombre portée*

Autres possibilités
-------------------

L'assortiment de fonctions de transformations dans GeoServer contient aussi un lot de transformations relatives ou constructives comme buffer, intersection, différence et ainsi de suite. Toutefois, ces fonctions sont relativement exigeantes en terme de consommation CPU, il est donc conseillé de les utiliser avec précaution et de ne les activer qu'à de hauts niveaux de zoom.

Les buffer peuvent souvent être approchés en choisissant des traits très larges et des jointures de lignes ronds, sans nécessiter des transformations géométriques.

Ajouter de nouvelles transformations
------------------------------------

Les fonctions filtre sont pluggable, ce qui signifie qu'il est possible d'en construire de nouvelles en Java et de déposer le fichier .jar résultat dans GeoServer en tant que plugin. A l'heure actuelle aucun guide n'est disponible, mais vous pouvez jeter un oeil dans le module principal GeoTools pour trouver des exemples.