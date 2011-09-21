.. _sld_cookbook_points:

Points
======

Si les points paraîssent être le type de forme le plus simple, ne comprenant qu'une positino et pas d'autres dimensions, il existe de nombreuses façons différentes de symboliser un point en SLD.

.. warning:: Pour rester concis, les exemples de code présentés sur cette page ne sont **pas le code SLD complet** car ils ommettent les informations SLD de début et de fin.  Utilisez les liens pour télécharger les SLD complet de chaque exemple.

.. _sld_cookbook_points_attributes:

Exemple de couche ponctuelle
----------------------------

La :download:`couche ponctuelle <artifacts/sld_cookbook_point.zip>` utilisée pour les exemples ci-dessous contient nom et population des villes principales d'un pays fictif. Pour mémoire, la table d'attributs des points de cette couche est présentée ci-dessous.

.. list-table::
   :widths: 30 40 30

   * - **fid** (Feature ID)
     - **name** (City name)
     - **pop** (Population)
   * - point.1
     - Borfin
     - 157860
   * - point.2
     - Supox City
     - 578231
   * - point.3
     - Ruckis
     - 98159
   * - point.4
     - Thisland
     - 34879
   * - point.5
     - Synopolis
     - 24567
   * - point.6
     - San Glissando
     - 76024
   * - point.7
     - Detrania
     - 205609

:download:`Téléchargez le shapefile des points <artifacts/sld_cookbook_point.zip>`

.. _sld_cookbook_points_simplepoint:

Point simple
------------

Cet exemple symbolise les points sous la forme de cercles rouges de diamètre 6 pixels.

.. figure:: images/point_simplepoint.png
   :align: center

   *Simple point*
   
Code
~~~~

:download:`Voir et télécharger le SLD "Simple point" complet <artifacts/point_simplepoint.sld>`

.. code-block:: xml 
   :linenos: 

      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#FF0000</CssParameter>
                </Fill>
              </Mark>
              <Size>6</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>



Details
~~~~~~~

Il y a un ``<Rule>`` dans un ``<FeatureTypeStyle>`` pour ce SLD, ce qui est la situation la plus simple possible.  (Les exemples suivants contiendront un ``<Rule>`` et un ``<FeatureTypeStyle>`` sauf précision.)  La symbolisation des points est effectuée par le ``<PointSymbolizer>`` (**lines 3-13**).  La **ligne 6** dit que la forme du symbole doit être un cercle, avec la **ligne 8** fixant la couleur de remplissage à rouge (``#FF0000``).  La **ligne 11** fixe la taille (diamètre) du graphisme à 6 pixels.


.. _sld_cookbook_points_simplepointwithstroke:

Exemple simple avec bord
------------------------

Cet exemple ajoute un trait (ou bord) autour du :`sld_cookbook_points_simplepoint`, avec le trait coloré en noir et muni d'une épaisseur de 2 pixels.

.. figure:: images/point_simplepointwithstroke.png
   :align: center

   *Point simple avec bord*

Code
~~~~

:download:`Consultez et téléchargez le SLD complet "Point simple avec bord" <artifacts/point_simplepointwithstroke.sld>`

.. code-block:: xml 
   :linenos: 

      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#FF0000</CssParameter>
                </Fill>
                <Stroke>
                  <CssParameter name="stroke">#000000</CssParameter>
                  <CssParameter name="stroke-width">2</CssParameter>
                </Stroke>
              </Mark>
              <Size>6</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>

Détails
~~~~~~~

Cet exemple est similaire à l'exemple :ref:`sld_cookbook_points_simplepoint`.  Les **lines 10-13** spécifient le trait, avec la **ligne 11** réglant couleur à noir (``#000000``) et la **ligne 12** réglant l'épaisseur à 2 pixels.


Carré avec rotation
-------------------

Cet exemple crée un carré au lieu d'un cercle, le colore en vert, le dimensionne à 12 pixels et le fait tourner de 45 degrés.

.. figure:: images/point_rotatedsquare.png
   :align: center

   *Carré avec rotation*

Code
~~~~

:download:`Consultez et téléchargez le SLD complet "Carré avec rotation" <artifacts/point_rotatedsquare.sld>`

.. code-block:: xml 
   :linenos: 

      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>square</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#009900</CssParameter>
                </Fill>
              </Mark>
              <Size>12</Size>
              <Rotation>45</Rotation>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>



Détails
~~~~~~~

Dans cet exemple, la **ligne 6** demande pour forme un carré, avec la  **ligne 8** réglant la couleur à vert foncé (``#009900``).  La **ligne 11** règle la taille à  12 pixels et la **ligne 12** règle la rotation à 45 degrés.


Triangle transparent
--------------------

Cet exemple dessine un triangle, crée un trait noir identique à l'exemple :ref:`sld_cookbook_points_simplepointwithstroke` , et règle le remplissage du triangle à 20% d'opacité (presque transparent).

.. figure:: images/point_transparenttriangle.png
   :align: center

   *Triangle transparent*

Code
~~~~   

:download:`Consultez et téléchargez le SLD complet "Triangle transparent" SLD <artifacts/point_transparenttriangle.sld>`

.. code-block:: xml 
   :linenos:

      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>triangle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#009900</CssParameter>
                  <CssParameter name="fill-opacity">0.2</CssParameter>
                </Fill>
                <Stroke>
                  <CssParameter name="stroke">#000000</CssParameter>
                  <CssParameter name="stroke-width">2</CssParameter>
                </Stroke>
              </Mark>
              <Size>12</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>



Détails
~~~~~~~

Dans cet exemple, la **ligne 6** règle la forme, un triangle dans ce cas. La **ligne 8** règle la couleur de remplissage à vert foncé (``#009900``) et la **ligne 9** règle l'opacité à  0.2 (opaque à 20%).  Une valeur d'opacité de 1 signifie que la forme est dessinée avec une opacité de 100%, alors qu'une valeur d'opacité de 0 signifie que la forme est dessinée avec une opacité de 0%, soit complètement transparente. La valeur de 0.2 (20% d'opacité) signifie que le remplissage des points prend partiellement la couleur et le style de ce qui est dessiné en-dessous. Dans cet exemple, comme le fond est blanc, le vert foncé paraît plus clair. Si les points étaient posés sur fond noir, la couleur résultante serait plus foncée. Les **lignes 12-13** règlent la couleur de trait à noir (``#000000``) et la largeur à 2 pixels. Finalement, la **ligne 16** règle la taille du point à un diamètre de 12 pixels.

Point graphique
---------------

Cet exemple symbolise chaque point avec un graphisme au lieu d'une forme simple.

.. figure:: images/point_pointasgraphic.png
   :align: center

   *Point image*

Code
~~~~

:download:`Consultez et téléchargez le SLD complet "Point image" <artifacts/point_pointasgraphic.sld>`

.. code-block:: xml 
   :linenos:

      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource
                  xlink:type="simple"
                  xlink:href="smileyface.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
              <Size>32</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>
	  


Détails
~~~~~~~

Ce style utilise une image au lieu d'une forme simple pour représenter les points. Dans le SLD, ceci est connu comme un ``<ExternalGraphic>``, pour le distinguer des formes courantes comme carrés et cercles qui sont "internes" au moteur de rendu. Les **lignes 5-10** spécifient les détails de cette image. La **ligne 8** paramètre le chemin et le nom de fichier de l'image, alors que la  **ligne 9** indique le format (type MIME) de l'image (image/png). Dans cet exemple, l'image est contenue dans le même répertoire que le SLD, aucune information de chemin n'est donc nécessaire en **ligne 8**, mais une URL complète pourrait être utilisée si souhaité. La **ligne 11** détermine la taille d'affichage de l'image; ceci peut être réglé indépendamment de la dimension propre de l'image, même si dans ce cas elles sont identiques (32 pixels). Si l'image était rectangulaire, la valeur ``<Size>`` s'appliquerait à la *hauteur* seule de l'image, avec la largeur réévaluée proportionnellement.

.. figure:: images/smileyface.png
   :align: center

   *Graphisme utilisé pour un point*

.. _sld_cookbook_points_pointwithdefaultlabel:

Point avec étiquette par défaut
-------------------------------

Cet exemple présente une étiquette textuelle sur le :ref:`sld_cookbook_points_simplepoint` affichant l'attibut "name" du point. Une étiquette sera représentée de cette façon en l'absence de personnalisation.

.. figure:: images/point_pointwithdefaultlabel.png
   :align: center

   *Point avec étiquette par défaut*

Code
~~~~

:download:`Consultez et téléchargez le SLD complet "Point with default label" <artifacts/point_pointwithdefaultlabel.sld>`

.. code-block:: xml 
   :linenos:

      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#FF0000</CssParameter>
                </Fill>
              </Mark>
              <Size>6</Size>
            </Graphic>
          </PointSymbolizer>
          <TextSymbolizer>
            <Label>
              <ogc:PropertyName>name</ogc:PropertyName>
            </Label>
            <Fill>
              <CssParameter name="fill">#000000</CssParameter>
            </Fill>
          </TextSymbolizer>
        </Rule>
      </FeatureTypeStyle>



Détails
~~~~~~~

**Lines 3-13**, which contain the ``<PointSymbolizer>``, are identical to the :ref:`sld_cookbook_points_simplepoint` example above.  The label is set in the ``<TextSymbolizer>`` on **lines 14-27**.  **Lines 15-17** determine what text to display in the label, which in this case is the value of the "name" attribute.  (Refer to the attribute table in the :ref:`sld_cookbook_points_attributes` section if necessary.)  **Line 19** sets the text color.  All other details about the label are set to the renderer default, which here is Times New Roman font, font color black, and font size of 10 pixels.  The bottom left of the label is aligned with the center of the point.


.. _sld_cookbook_points_pointwithstyledlabel:

Point with styled label
-----------------------

This example improves the label style from the :ref:`sld_cookbook_points_pointwithdefaultlabel` example by centering the label above the point and providing a different font name and size.

.. figure:: images/point_pointwithstyledlabel.png
   :align: center

   *Point with styled label*

Code
~~~~   

:download:`View and download the full "Point with styled label" SLD <artifacts/point_pointwithstyledlabel.sld>`

.. code-block:: xml 
   :linenos:

      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#FF0000</CssParameter>
                </Fill>
              </Mark>
              <Size>6</Size>
            </Graphic>
          </PointSymbolizer>
          <TextSymbolizer>
            <Label>
              <ogc:PropertyName>name</ogc:PropertyName>
            </Label>
            <Font>
              <CssParameter name="font-family">Arial</CssParameter>
              <CssParameter name="font-size">12</CssParameter>
              <CssParameter name="font-style">normal</CssParameter>
              <CssParameter name="font-weight">bold</CssParameter>
            </Font>
            <LabelPlacement>
              <PointPlacement>
                <AnchorPoint>
                  <AnchorPointX>0.5</AnchorPointX>
                  <AnchorPointY>0.0</AnchorPointY>
                </AnchorPoint>
                <Displacement>
                  <DisplacementX>0</DisplacementX>
                  <DisplacementY>5</DisplacementY>
                </Displacement>
              </PointPlacement>
            </LabelPlacement>
            <Fill>
              <CssParameter name="fill">#000000</CssParameter>
            </Fill>
          </TextSymbolizer>
        </Rule>
      </FeatureTypeStyle>


Details
~~~~~~~

In this example, **lines 3-13** are identical to the :ref:`sld_cookbook_points_simplepoint` example above.  The ``<TextSymbolizer>`` on lines 14-39 contains many more details about the label styling than the previous example, :ref:`sld_cookbook_points_pointwithdefaultlabel`.  **Lines 15-17** once again specify the "name" attribute as text to display.  **Lines 18-23** set the font information:  **line 19** sets the font family to be "Arial", **line 20** sets the font size to 12, **line 21** sets the font style to "normal" (as opposed to "italic" or "oblique"), and **line 22** sets the font weight to "bold" (as opposed to "normal").  **Lines 24-35** (``<LabelPlacement>``) determine the placement of the label relative to the point.  The ``<AnchorPoint>`` (**lines 26-29**) sets the point of intersection between the label and point, which here (**line 27-28**) sets the point to be centered (0.5) horizontally axis and bottom aligned (0.0) vertically with the label.  There is also ``<Displacement>`` (**lines 30-33**), which sets the offset of the label relative to the line, which in this case is 0 pixels horizontally (**line 31**) and 5 pixels vertically (**line 32**).  Finally, **line 37** sets the font color of the label to black (``#000000``).

The result is a centered bold label placed slightly above each point.



Point with rotated label
------------------------

This example builds on the previous example, :ref:`sld_cookbook_points_pointwithstyledlabel`, by rotating the label by 45 degrees, positioning the labels farther away from the points, and changing the color of the label to purple.

.. figure:: images/point_pointwithrotatedlabel.png
   :align: center

   *Point with rotated label*

Code
~~~~

:download:`View and download the full "Point with rotated label" SLD <artifacts/point_pointwithrotatedlabel.sld>`

.. code-block:: xml 
   :linenos:

      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#FF0000</CssParameter>
                </Fill>
              </Mark>
              <Size>6</Size>
            </Graphic>
          </PointSymbolizer>
          <TextSymbolizer>
            <Label>
              <ogc:PropertyName>name</ogc:PropertyName>
            </Label>
            <Font>
              <CssParameter name="font-family">Arial</CssParameter>
              <CssParameter name="font-size">12</CssParameter>
              <CssParameter name="font-style">normal</CssParameter>
              <CssParameter name="font-weight">bold</CssParameter>
            </Font>
            <LabelPlacement>
              <PointPlacement>
                <AnchorPoint>
                  <AnchorPointX>0.5</AnchorPointX>
                  <AnchorPointY>0.0</AnchorPointY>
                </AnchorPoint>
                <Displacement>
                  <DisplacementX>0</DisplacementX>
                  <DisplacementY>25</DisplacementY>
                </Displacement>
                <Rotation>-45</Rotation>
              </PointPlacement>
            </LabelPlacement>
            <Fill>
              <CssParameter name="fill">#990099</CssParameter>
            </Fill>
          </TextSymbolizer>
        </Rule>
      </FeatureTypeStyle>



Details
~~~~~~~

This example is similar to the :ref:`sld_cookbook_points_pointwithstyledlabel`, but there are three important differences.  **Line 32** specifies 25 pixels of vertical displacement.  **Line 34** specifies a rotation of "-45" or 45 degrees counter-clockwise.  (Rotation values increase clockwise, which is why the value is negative.)  Finally, **line 38** sets the font color to be a shade of purple (``#99099``).

Note that the displacement takes effect before the rotation during rendering, so in this example, the 25 pixel vertical displacement is itself rotated 45 degrees.


Attribute-based point
---------------------

This example alters the size of the symbol based on the value of the population ("pop") attribute.  

.. figure:: images/point_attributebasedpoint.png
   :align: center

   *Attribute-based point*
   
Code
~~~~

:download:`View and download the full "Attribute-based point" SLD <artifacts/point_attribute.sld>`

.. code-block:: xml 
   :linenos:

      <FeatureTypeStyle>
        <Rule>
          <Name>SmallPop</Name>
          <Title>1 to 50000</Title>
          <ogc:Filter>
            <ogc:PropertyIsLessThan>
              <ogc:PropertyName>pop</ogc:PropertyName>
              <ogc:Literal>50000</ogc:Literal>
            </ogc:PropertyIsLessThan>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#0033CC</CssParameter>
                </Fill>
              </Mark>
              <Size>8</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Name>MediumPop</Name>
          <Title>50000 to 100000</Title>
          <ogc:Filter>
            <ogc:And>
              <ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyName>pop</ogc:PropertyName>
                <ogc:Literal>50000</ogc:Literal>
              </ogc:PropertyIsGreaterThanOrEqualTo>
              <ogc:PropertyIsLessThan>
                <ogc:PropertyName>pop</ogc:PropertyName>
                <ogc:Literal>100000</ogc:Literal>
              </ogc:PropertyIsLessThan>
            </ogc:And>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#0033CC</CssParameter>
                </Fill>
              </Mark>
              <Size>12</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Name>LargePop</Name>
          <Title>Greater than 100000</Title>
          <ogc:Filter>
            <ogc:PropertyIsGreaterThanOrEqualTo>
              <ogc:PropertyName>pop</ogc:PropertyName>
              <ogc:Literal>100000</ogc:Literal>
            </ogc:PropertyIsGreaterThanOrEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#0033CC</CssParameter>
                </Fill>
              </Mark>
              <Size>16</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>



Details
~~~~~~~
   
.. note:: Refer to the :ref:`sld_cookbook_points_attributes` to see the attributes for this data.  This example has eschewed labels in order to simplify the style, but you can refer to the example :ref:`sld_cookbook_points_pointwithstyledlabel` to see which attributes correspond to which points.

This style contains three rules.  Each ``<Rule>`` varies the style based on the value of the population ("pop") attribute for each point, with smaller values yielding a smaller circle, and larger values yielding a larger circle.

The three rules are designed as follows:

.. list-table::
   :widths: 20 30 30 20

   * - **Rule order**
     - **Rule name**
     - **Population** ("pop")
     - **Size**
   * - 1
     - SmallPop
     - Less than 50,000
     - 8
   * - 2
     - MediumPop
     - 50,000 to 100,000
     - 12
   * - 3
     - LargePop
     - Greater than 100,000
     - 16

The order of the rules does not matter in this case, since each shape is only rendered by a single rule.

The first rule, on **lines 2-22**, specifies the styling of those points whose population attribute is less than 50,000.  **Lines 5-10** set this filter, with **lines 6-9** setting the "less than" filter, **line 7** denoting the attribute ("pop"), and **line 8** the value of 50,000.  The symbol is a circle (**line 14**), the color is dark blue (``#0033CC``, on **line 16**), and the size is 8 pixels in diameter (**line 19**).  

The second rule, on **lines 23-49**, specifies a style for points whose population attribute is greater than or equal to 50,000 and less than 100,000.  The population filter is set on **lines 26-37**.  This filter is longer than in the first rule because two criteria need to be specified instead of one: a "greater than or equal to" and a "less than" filter.  Notice the ``And`` on **line 27** and **line 36**.  This mandates that both filters need to be true for the rule to be applicable.  The size of the graphic is set to 12 pixels on **line 46**.  All other styling directives are identical to the first rule.

The third rule, on **lines 50-70**, specifies a style for points whose population attribute is greater than or equal to 100,000.  The population filter is set on **lines 53-58**, and the only other difference is the size of the circle, which in this rule (**line 67**) is 16 pixels.

The result of this style is that cities with larger populations have larger points.


Zoom-based point
----------------

This example alters the style of the points at different zoom levels.

.. figure:: images/point_zoombasedpointlarge.png
   :align: center

   *Zoom-based point: Zoomed in*

.. figure:: images/point_zoombasedpointmedium.png
   :align: center
   
   *Zoom-based point: Partially zoomed*

.. figure:: images/point_zoombasedpointsmall.png
   :align: center
   
   *Zoom-based point: Zoomed out*

   
Code
~~~~

:download:`View and download the full "Zoom-based point" SLD <artifacts/point_zoom.sld>`

.. code-block:: xml 
   :linenos:

      <FeatureTypeStyle>
        <Rule>
          <Name>Large</Name>
          <MaxScaleDenominator>160000000</MaxScaleDenominator>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#CC3300</CssParameter>
                </Fill>
              </Mark>
              <Size>12</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Name>Medium</Name>
          <MinScaleDenominator>160000000</MinScaleDenominator>
          <MaxScaleDenominator>320000000</MaxScaleDenominator>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#CC3300</CssParameter>
                </Fill>
              </Mark>
              <Size>8</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <Name>Small</Name>
          <MinScaleDenominator>320000000</MinScaleDenominator>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>circle</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#CC3300</CssParameter>
                </Fill>
              </Mark>
              <Size>4</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>




Details
~~~~~~~

It is often desirable to make shapes larger at higher zoom levels when creating a natural-looking map.  This example styles the points to vary in size based on the zoom level (or more accurately, scale denominator).  Scale denominators refer to the scale of the map.  A scale denominator of 10,000 means the map has a scale of 1:10,000 in the units of the map projection.

.. note:: Determining the appropriate scale denominators (zoom levels) to use is beyond the scope of this example.

This style contains three rules.  The three rules are designed as follows:

.. list-table::
   :widths: 25 25 25 25 

   * - **Rule order**
     - **Rule name**
     - **Scale denominator**
     - **Point size**
   * - 1
     - Large
     - 1:160,000,000 or less
     - 12
   * - 2
     - Medium
     - 1:160,000,000 to 1:320,000,000
     - 8
   * - 3
     - Small
     - Greater than 1:320,000,000
     - 4

The order of these rules does not matter since the scales denominated in each rule do not overlap.

The first rule (**lines 2-16**) is for the smallest scale denominator, corresponding to when the view is "zoomed in".  The scale rule is set on **line 4**, so that the rule will apply to any map with a scale denominator of 160,000,000 or less.  The rule draws a circle (**line 8**), colored red (``#CC3300`` on **line 10**) with a size of 12 pixels (**line 13**).

The second rule (**lines 17-32**) is the intermediate scale denominator, corresponding to when the view is "partially zoomed".  The scale rules are set on **lines 19-20**, so that the rule will apply to any map with a scale denominator between 160,000,000 and 320,000,000.  (The ``<MinScaleDenominator>`` is inclusive and the ``<MaxScaleDenominator>`` is exclusive, so a zoom level of exactly 320,000,000 would *not* apply here.)  Aside from the scale, the only difference between this rule and the first is the size of the symbol, which is set to 8 pixels on **line 29**.

The third rule (**lines 33-47**) is the largest scale denominator, corresponding to when the map is "zoomed out".  The scale rule is set on **line 35**, so that the rule will apply to any map with a scale denominator of 320,000,000 or more.  Again, the only other difference between this rule and the others is the size of the symbol, which is set to 4 pixels on **line 44**.

The result of this style is that points are drawn larger as one zooms in and smaller as one zooms out.


.. fabrice at phung.fr 2011/09/20 r16266