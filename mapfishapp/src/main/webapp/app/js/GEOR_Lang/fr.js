/*
 * Copyright (C) Camptocamp
 *
 * This file is part of geOrchestra
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * French translation file
 */
OpenLayers.Lang.fr = OpenLayers.Util.extend(OpenLayers.Lang.fr, {
    /* General purpose strings */
    "Yes": "Oui",
    "No": "Non",
    "OK": "OK",
    "Cancel": "Annuler",
    "Save": "Sauvegarder",
    "Loading...": "Chargement...",
    "Layer": "Couche",
    "Description": "Description",
    "Error": "Erreur",
    "The provided context is not valid.": "Le contexte fourni n'est pas " +
        "valide.",
    "Server": "Serveur",
    "Close": "Fermer",
    /* GEOR.js strings */
    "Cities": "Localités",
    "Recentering on GeoNames cities": "Recentrage sur localités<br />de la base GeoNames",
    "Referentials": "Référentiels",
    "Recentering on a selection of referential layers": "Recentrage sur une sélection<br />de couches \"référentiels\"",
    "Addresses": "Adresses",
    "Recentering on a given address": "Recentrage sur point adresse",
    "Available layers": "Couches disponibles",
    "Editing": "Edition",
    "resultspanel.emptytext":
        "<p>Sélectionnez l'outil d'interrogation " +
        "ou construisez une requête sur une couche.<br />" +
        "Les attributs des objets s'afficheront dans ce cadre.</p>",
    /* GEOR_ClassificationPanel.js strings */
    "Attribute": "Attribut",
    "Type": "Type",
    "Number of classes": "Nombre de classes",
    "Minimum size": "Taille minimum",
    "Maximum size": "Taille maximum",
    "First color": "Première couleur",
    "Last color": "Dernière couleur",
    "Palette": "Palette",
    "Auto classification": "Classification automatique",
    "Classify": "Classifier",
    "Unique values": "Valeurs uniques",
    "Color range": "Plages de couleurs",
    "Proportional symbols": "Symboles proportionnels",
    /* GEOR_FeatureDataModel.js strings */
    "objects": "objets",
    /* GEOR_address.js strings */
    "Go to: ": "Aller à : ",
    "searching...": "recherche en cours...",
    "adressSearchExemple": "ex: 4, Hugo, Brest",
    /* GEOR_ajaxglobal.js strings strings */
    "Server did not respond.": "Le serveur n'a pas répondu.",
    "Server access denied.": "Le serveur a refusé de répondre.",
    "ajax.badresponse":
        "Le service a répondu, mais le contenu de la " +
        "réponse n'est pas conforme à celle attendue",
    "Server unavailable.": "Le serveur est temporairement indisponible. Veuillez réessayer ultérieurement.",
    "Too much data.": "Données trop volumineuses.",
    "Server exception.": "Le serveur a renvoyé une exception.",
    "ajax.defaultexception":
        "Pour plus d'information, nous vous invitons à " +
        "chercher le code de retour sur <a href=\"http://" +
        "en.wikipedia.org/wiki/List_of_HTTP_status_codes\" target=\"_blank\">" +
        "cette page</a>.",
    "An error occured.<br />": "Une erreur est survenue.<br />",
    "Warning : browser may freeze": "Attention : risque de blocage du navigateur",
    "ajaxglobal.data.too.big": "Les données provenant du serveur sont trop " +
        "volumineuses.<br />Le serveur a envoyé ${SENT}KO " +
        "(la limite est à ${LIMIT}KO)<br />Voulez-vous tout de même continuer ?",
    /* GEOR_config.js strings */
    /* GEOR_cswbrowser.js strings */
    "NAME layer": "Couche ${name}",
    "Metadata without a name": "Métadonnée non nommée",
    "The getDomain CSW query failed": "La requête CSW getDomain a échoué",
    "Error for the thesaurus": "Erreur sur le thésaurus",
    "Missing key to access the thesaurus":
        "Absence de clé pour accéder à ce thésaurus",
    "Keywords query failed": "La requête des mots clés a échoué",
    "Thesaurus:": "Thésaurus :",
    "Thesaurus": "Thésaurus",
    "cswbrowser.default.thesaurus.mismatch":
        "Administrateur : problème de configuration - " +
        "la variable DEFAULT_THESAURUS_KEY ne correspond à aucune" +
        " valeur exportée par GeoNetwork",
    /* GEOR_cswquerier.js strings */
    "Show metadata sheet in a new window":
        "Afficher la fiche de métadonnées dans une nouvelle fenêtre",
    'more': 'plus',
    "Clic to select or deselect the layer":
        "Cliquez pour sélectionner ou désélectionner la couche",
    "Open the URL url in a new window":
        "Ouvrir l'url ${url} dans une nouvelle fenêtre",
    "Unreachable server": "Serveur non disponible",
    "Catalogue": "Catalogue",
    "Find": "Chercher",
    "in": "dans",
    "Not any layer": "Aucune couche",
    "1 layer": "1 couche",
    "NB layers": "${NB} couches",
    " in NB metadata": " dans ${NB} metadonnées",
    " in 1 metadata": " dans 1 metadonnée",
    ": precise your request": " : précisez votre requête",
    "Not any metadata correspond to the words specified":
        "Aucune métadonnée ne correspond aux termes saisis",
    /* GEOR_editing.js strings */
    /* GEOR_geonames.js strings */
    /* GEOR_getfeatureinfo.js strings */
    "<div>Searching...</div>": "<div>Recherche en cours...</div>",
    "<div>Search on objects active for NAME layer. Clic on the map.</div>":
         "<div>Recherche d\'objets activée sur la couche ${name}. " +
         "Cliquez sur la carte.</div>",
    /* GEOR_layerfinder.js strings */
    "Find layers searching in metadata":
        "Trouvez des couches en cherchant dans les métadonnées",
    "Find layers from keywords": "Trouvez des couches par mots clés",
    "Find layers querying WMS servers":
        "Trouvez des couches en interrogeant des serveurs WMS",
    "Find layers querying WFS servers":
        "Trouvez des couches en interrogeant des serveurs WFS",
    "layerfinder.layer.unavailable":
        "La couche ${name} n'a pas été trouvée dans le service WMS.<br/<br/>" +
        "Peut-être n'avez-vous pas le droit d'y accéder " +
        "ou alors cette couche n'est plus disponible",
    "Layer projection is not compatible":
        "La projection de la couche n'est pas compatible.",
    "The NAME layer does not contain a valid geometry column":
        "La couche ${name} ne possède pas de colonne géométrique valide.",
    "Add": "Ajouter",
    "Add layers from a ...": "Ajouter des couches depuis un ...",
    "Malformed URL": "URL non conforme.",
    /* GEOR_managelayers.js strings */
    "Confirm NAME layer deletion ?":
        "Voulez-vous réellement supprimer la couche ${name} ?",
    "1:MAXSCALE to 1:MINSCALE": "1:${maxscale} à 1:${minscale}",
    "Visibility range (indicative):<br />from TEXT":
        "Plage de visibilité (indicative):<br />de ${text}",
    "Information on objects of this layer":
        "Interroger les objets de cette couche",
    "Default style": "Style par défaut",
    "Recenter on the layer": "Recentrer sur la couche",
    "Impossible to get layer extent":
        "Impossible d'obtenir l'étendue de la couche.",
    "Refresh layer": "Recharger la couche",
    "Show metadata": "Afficher les métadonnées",
    "Edit symbology": "Éditer la symbologie",
    "Build a query": "Construire une requête",
    "Failed to get WFS layer address. <br />The query module will be disabled":
        "Impossible d'obtenir l'adresse de la couche WFS." +
        "<br />Le requêteur ne sera pas disponible.",
    "DescribeLayer WMS query failed. <br />The query module will be disabled":
        "La requête WMS DescribeLayer a malheureusement échoué." +
        "<br />Le requêteur ne sera pas disponible.",
    "Download data": "Télécharger les données",
    "Choose a style": "Choisir un style",
    "Modify format": "Modifier le format",
    "Delete this layer": "Supprimer cette couche",
    "Push up this layer": "Monter cette couche",
    "Push down this layer": "descendre cette couche",
    "Add layers": "Ajouter des couches",
    "source: ": "source : ",
    "Actions": "Actions",
    /* GEOR_map.js strings */
    "Location map": "Carte de situation",
    "Warning after loading layer":
        "Avertissement suite au chargement de couche",
    "The <b>NAME</b> layer could not appear for that reason: ":
        "La couche <b>${name}</b> pourrait ne pas apparaître pour " +
        "la raison suivante : ",
    "Min/max visibility scales are invalid":
        "Les échelles min/max de visibilité sont invalides.",
    "Visibility range does not match map scales":
        "La plage de visibilité ne correspond pas aux échelles de la carte.",
    "Geografic extent does not match map extent":
        "L'étendue géographique ne correspond pas à celle de la carte.",
    /* GEOR_mapinit.js strings */
    "Add layers from WMS services":
        "Ajouter des couches depuis des services WMS",
    "NB layers not imported": "${NB} couches non importées",
    "One layer not imported": "Une couche non importée",
    "mapinit.layers.load.error":
        "Les couches nommées ${list} n'ont pas pu être chargées. " +
        "Raisons possibles : droits insuffisants, SRS incompatible ou couche non existante",
    "NB layers imported": "${NB} couches importées",
    "One layer imported": "Une couche importée",
    "Not any layer imported": "Aucune couche importée",
    "The default context is not defined (and it is a BIG problem!)":
        "Le contexte par défaut n'est pas défini " +
        "(et ce n'est pas du tout normal !)",
    /* GEOR_mappanel.js strings */
    "Mouse coordinates in SRS": "Coordonnées du pointeur en ${srs}",
    /* GEOR_ows.js strings */
    "The NAME layer was not found in WMS service.":
        "La couche ${name} n'a pas été trouvée dans le service WMS.",
    /* GEOR_print.js strings */
    "Sources: ": "Sources : ",
    "Source: ": "Source : ",
    "Print error": "Impression impossible",
    "Print server returned an error":
        "Le service d'impression a signalé une erreur.",
    "Contact platform administrator":
        "Contactez l'administrateur de la plateforme.",
    "Layer unavailable for printing": "Couche non disponible pour impression",
    "The NAME layer cannot be printed.":
        "La couche ${name} ne peut pas encore être imprimée.",
    "Unable to print": "Impression non disponible",
    "The print server is currently unreachable":
        "Le service d'impression est actuellement inaccessible.",
    "print.unknown.format":
        "Erreur de configuration: DEFAULT_PRINT_FORMAT " +
        "${format} n'est pas dans la liste des formats d'impression",
    "print.unknown.resolution":
        "Erreur de configuration: DEFAULT_PRINT_RESOLUTION " +
        "${resolution} n'est pas dans la liste des résolutions d'impression",
    "Title": "Titre",
    "Copyright": "Copyright",
    "Minimap": "Mini-carte",
    "North": "Nord",
    "Scale": "Echelle",
    "Date": "Date",
    "Legend": "Légende",
    "Format": "Format",
    "Resolution": "Résolution",
    "Print the map": "Impression de la carte",
    "Print": "Imprimer",
    "Printing...": "Impression en cours...",
    "Print current map": "Imprimer la carte courante",
    /* GEOR_querier.js strings */
    "Fields of filters with a red mark are mandatory": "Vous devez remplir " +
        "les champs des filtres marqués en rouge.",
    "Request on NAME": "Requêteur sur ${name}",
    "Search": "Recherche",
    "querier.layer.no.geom":
        "La couche ne possède pas de colonne géométrique." +
        "<br />Le requêteur géométrique ne sera pas fonctionnel.",
    "querier.layer.error":
        "Impossible d'obtenir les caractéristiques de la couche demandée." +
        "<br />Le requêteur ne sera pas disponible.",
    /* GEOR_referentials.js strings */
    "Referential": "Référentiel",
    "There is no geometry column in the selected referential":
        "Le référentiel sélectionné ne possède pas de colonne géométrique",
    "Choose a referential": "Choisissez un référentiel",
    /* GEOR_resultspanel.js strings */
    "resultspanel.maxfeature.reached":
        " <span ext:qtip=\"Utilisez un navigateur plus performant " +
        "pour augmenter le nombre d'objets affichables\">" +
        "Nombre maximum d'objets atteint (${NB})</span>",
    "NB results": "${NB} résultats",
    "One result": "1 résultat",
    "Not any result": "Aucun résultat",
    "Clean": "Effacer",
    "Clean all results on the map and in the table": "Supprimer les " +
        "résultats affichés sur la carte et dans le tableau",
    "Zoom": "Zoom",
    "Zoom to results extent": "Cadrer l'étendue de la carte sur celle " +
        "des résultats",
    "CSV Export": "Export CSV",
    "Export results as CSV": "Exporter l'ensemble des résultats en CSV",
    "<p>Not any result for that request.</p>": "<p>Aucun objet ne " +
        "correspond à votre requête.</p>",
    /* GEOR_scalecombo.js strings */
    /* GEOR_styler.js strings */
    "Download style": "Télécharger le style",
    "You can download your SLD style at ": "Votre SLD est disponible à " +
        "l\'adresse suivante : ",
    "Thanks!": "Merci !",
    "Saving SLD": "Sauvegarde du SLD",
    "Some classes are invalid, verify that all fields are correct": "Des " +
        "classes ne sont pas valides, vérifier que les champs sont corrects",
    "Get SLD": "Récupération du SLD",
    "Malformed SLD": "Le SLD n'est pas conforme.",
    "circle": "cercle",
    "square": "carré",
    "triangle": "triangle",
    "star": "étoile",
    "cross": "croix",
    "x": "x",
    "customized...": "personnalisé...",
    "Classification ...<br/>(this operation can take some time)":
        "Classification ...<br/>(cette opération peut prendre du temps)",
    "Class": "Classe",
    "Untitled": "Sans titre",
    "styler.guidelines":
        "Utiliser le bouton \"+\" pour créer une classe, et le bouton " +
        "\"Analyse\" pour créer un ensemble de classes définies par une " +
        "analyse thématique.</p>",
    "Analyze": "Analyse",
    "Add a class": "Ajouter une classe",
    "Delete the selected class": "Supprimer la classe sélectionnée",
    "Styler": "Styleur",
    "Apply": "Appliquer",
    "Impossible to complete the operation:": "Opération impossible :",
    "not any WFS service associated to that layer": "aucun service WFS " +
        "associé à cette couche.",
    "not any available attribute": "aucun attribut disponible.",
    /* GEOR_toolbar.js strings */
    "m": "m",
    "hectares": "hectares",
    "zoom to global extent of the map": "zoom sur l'étendue globale de la " +
        "carte",
    "pan": "glisser - déplacer la carte",
    "zoom in": "zoom en avant",
    "distance measure": "mesurer une distance",
    "area measure": "mesurer une surface",
    "back to previous zoom": "revenir à la précédente emprise",
    "go to next zoom": "aller à l'emprise suivante",
    "Login": "Connexion",
    "Logout": "Déconnexion",
    "Help": "Aide",
    "Show help": "Afficher l'aide",
    "Show legend": "Afficher la légende",
    "Leave this page ? You will lose the current cartographic context.":
        "Vous allez quitter cette page et perdre le contexte cartographique " +
        "courant",
    /* GEOR_util.js strings */
    "Characters": "Caractères",
    "Digital": "Numérique",
    "Boolean": "Booléen",
    "Other": "Autre",
    "Confirmation": "Confirmation",
    "Information": "Information",
    /* GEOR_waiter.js strings */
    /* GEOR_wfsbrowser.js strings */
    "Choose a WFS server: ": "Choisissez un serveur WFS : ",
    "... or enter its address: ": "... ou saisissez son adresse : ",
    "Unreachable server or insufficient rights": "Réponse invalide du " +
        "serveur. Raisons possibles : droits insuffisants, " +
        "serveur injoignable, trop de données, etc.",
    "WFS server": "Serveur WFS",
    /* GEOR_wmc.js strings */
    "wmc.bad.srs": "Le fichier .wmc ne " +
        "peut pas être restauré. Son système de référence spatiale est " +
        "différent de celui de la carte en cours.",
    /* GEOR_wmsbrowser.js strings */
    "Queryable": "Interrogeable",
    "Opaque": "Opaque",
    "Choose a WMS server: ": "Choisissez un serveur WMS : ",
    "The server is publishing one layer with an incompatible projection":
        "Le serveur publie une couche dont la projection n'est pas compatible",
    "The server is publishing NB layers with an incompatible projection":
        "Le serveur publie ${NB} couches dont la projection n'est pas " +
        "compatible",
    "WMS server": "Serveur WMS",
    /* GEOR_workspace.js strings */
    "Context saving": "Sauvegarde du contexte",
    "The file is required.": "Un nom de fichier est nécessaire.",
    "Context restoring": "Restauration d'un contexte",
    "<p>Please note that the WMC must be UTF-8 encoded</p>": "<p>Notez que le" +
        " fichier de contexte doit être encodé en UTF-8.</p>",
    "File": "Fichier",
    "Load": "Charger",
    "Workspace": "Espace de travail",
    "Save the map context": "Sauvegarder la carte",
    "Load a map context": "Charger une carte",
    "Edit in OSM": "Editer dans OSM",
    "with JOSM": "avec JOSM",
    "JOSM must be started with the remote control option": "Il vous faut " +
        "auparavant lancer JOSM et activer le contrôle à distance",
    "with Potlatch": "avec Potlatch",
    "Recommended scale is 1:10.000": "Il est recommandé de travailler à des " +
        "échelles proches de 1:10.000",
    "with Potlatch2": "avec Potlatch2",
    "with Walking Papers": "avec Walking Papers",
    /* GEOR_EditingPanel.js */
    "Read-only layer": "Couche non éditable",
    "editingpanel.geom.error": "La géométrie de cette couche est de type ${type}.<br/>" +
        "Seules les géométries de type point, ligne et polygone" +
        " (et multi-*) sont éditables.",
    "choose a layer": "choisissez une couche",
    /* GEOR_LayerEditingPanel.js */
    "Modify object": "Modifier un objet",
    "Cancel all": "Tout annuler",
    "Enter ": "Saisir ",
    "layereditingpanel.cancel.confirm": "Souhaitez-vous vraiment annuler toutes les modifications<br />depuis la dernière synchronisation ?",
    "Synchronize": 'Synchroniser',
    "layereditingpanel.changes.confirm": "Veuillez confirmer ou annuler " +
        "les modifications en cours.",
    "Delete": "Supprimer",
    "Confirm": "Confirmer",
    " (required)": " (requis)",
    "Text too long": "Texte trop long",
    "Text too short": "Texte trop court",
    "Maximum value exceeded": "Valeur maximale dépassée",
    "Minimum value not reached": "Valeur minimale non atteinte",
    "Invalid number": "Nombre non valide",
    "Synchronization successful.": "Synchronisation réussie.",
    "Synchronization failed.": "Erreur lors de la synchronisation.",
    "Recover": "Restaurer",
    "No feature selected !": "Aucun objet sélectionné !",
    "a point": "un point",
    "a line": "une ligne",
    "a polygon": "un polygone",
    "Pending changes": "Modifications en cours",
    "Do you want to confirm changes ?": "Souhaitez-vous confirmer les modifications ?",
    /* SpatialComboBox.js */
    "intersection": "intersection avec",
    "inside": "à l'intérieur de",
    "contains": "contient l'objet",
    /* FilterPanel.js */
    "This field is mandatory": "Ce champ est nécessaire",
    /* SpatialFilterPanel.js */
    "Modify geometry": "Modifier la géométrie",
    "Save this geometry": "Enregistrer cette géométrie",
    "spatialfilterpanel.geometry.saved": "Géométrie enregistrée pour 30 jours sur ce navigateur.",
    /* FilterBuilder.js */
    "any": "une de",
    "all": "toutes",
    "none": "aucune de",
    "not all": "pas toutes",
    "Matching": "Correspondre à",
    "these conditions": "ces conditions",
    "spatial condition": "condition spatiale",
    "group": "groupe",
    "based on a point": "basée sur un point",
    "based on a line": "basée sur une ligne",
    "based on a polygon": "basée sur un polygone",
    "based on a stored geometry": "basée sur une géométrie stockée",
    "Delete this condition": "Supprimer cette condition",
    /* GeoExt.data.CSW.js */
    "no abstract": "pas de résumé",
    /* Styler.js */
    "Unable to read capabilities from WMS":
        "Impossible de lire les 'capabilities' depuis WMS",
    "Unable to read capabilities from WFS":
        "Impossible de lire les 'capabilities' depuis WFS",
    "Add new": "Créer une nouvelle règle",
    "Delete selected": "Supprimer la selection",
    "styler.delete.rule":
        "Supprimer la règle ${NAME} ?",
    "Delete rule": "Supprimer la règle",
    "Layers": "Couches",
    "styler.feature": "Objet : ${FEATURE}",
    "Rules used to render this feature:":
        "Règles utilisées pour cet objet :",
    "Attributes of this feature:": "Attributs de cet objet :",
    "styler.style": "Style : ${STYLE}",
    "Untitled": "Sans titre",
    "styler.div.zoomlevel": "<div>{zoomType} Niveau de zoom : {zoom}</div>",
    "styler.div.mapzoom": "<div>Zoom actuel de la carte: {mapZoom}</div>",
    "Circle": "Cercle",
    "Square": "Carré",
    "Triangle": "Triangle",
    "Star": "Étoile",
    "Cross": "Croix",
    "X": "X",
    "Custom...": "Personnalisé...",
    "Cancel": "Annuler",
    "Save": "Sauvegarder",
    "Could not load features from the WFS":
        "Impossible de charger les objets par WFS",
    /* ColorManager.js */
    "Color Picker": "Selecteur de couleur",
    /* FillSymbolizer.js */
    "Fill": "Remplissage",
    "Color": "Couleur",
    "Opacity": "Opacité",
    /* LegendPanel.js */
    "Untitled ": "Sans titre ",
    /* PointSymbolizer.js */
    "Circle": "Cercle",
    "Square": "Carré",
    "Triangle": "Triangle",
    "Star": "Étoile",
    "Cross": "Croix",
    "X": "X",
    "External": "Externe",
    "URL": "URL",
    "Opacity": "Opacité",
    "Symbol": "Symbole",
    "Radius": "Taille",
    "Rotation": "Rotation",
    /* RuleBuilder.js */
    "Add rule": "Créer une règle",
    "Untitled ": "Sans titre ",
    /* RuleChooser.js */
    "Styling rules that apply for this feature":
        "Règles de style qui peuvent s'appliquer à cet objet",
    "Default": "Par défaut",
    '{type} for the "{layer}" layer':
        '{type} pour la couche "{layer}"',
    'Create a new styling rule': "Créer une nouvelle règle de style",
    "Other styling rules": "Autres règles de style",
    "Styling rules": "Règles de style",
    /* RulePanel.js */
    "{type} scale 1:{scale}": "Échelle {type} 1:{scale}",
    "Labels": "Libellés",
    "Simple": "Simple",
    "Advanced": "Avancé",
    "Limit by scale": "Limite par échelle",
    "Limit by condition": "Limite par condition",
    "Symbol": "Symbole",
    "Name": "Nom",
    /* ScaleLimitPanel.js */
    "Min scale": "Échelle min",
    "Max scale": "Échelle max",
    /* StrokeSymbolizer.js */
    "Solid": "Plein",
    "Dash": "Tiret",
    "Dot": "Point",
    "Border": "Contour",
    "Style": "Style",
    "Color": "Couleur",
    "Width": "Épaisseur",
    "Opacity": "Opacité",
    /* TextSymbolizer */
    "Attribute": "Attribut",
    "Size: ": "Taille : ",
    "Halo": "Halo",
    "Size": "Taille",
    /* ScaleSliderTip.js */
    "Zoom Level: {zoom}": "Niveau de zoom : {zoom}",
    "Resolution: {resolution}": "Résolution: {resolution}",
    "Scale: 1 : {scale}": "Échelle: 1 : {scale}",
    /*GEOR_addonsmenu.js */
    "Tools": "Outils"
    // no trailing comma
});
