/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @requires GeoExt/Lang.js
 */

/*
 * French translation file
 */
OpenLayers.Lang.fr = OpenLayers.Util.extend(OpenLayers.Lang.fr, {
    /* General purpose strings */
    "Yes": "Oui",
    "No": "Non",
    "OK": "OK",
    "or": "ou",
    "Cancel": "Annuler",
    "Save": "Sauvegarder",
    "Loading...": "Chargement...",
    "File": "Fichier",
    "Layer": "Couche",
    "layers": "couches",
    "Description": "Description",
    "Error": "Erreur",
    "Server": "Serveur",
    "Close": "Fermer",
    "labelSeparator": " : ",
    "File submission failed or invalid file": "L'envoi du fichier a échoué - le fichier est peut-être non valide",
    "Type": "Type",
    "Title": "Titre",
    "Actions": "Actions",
    "Incorrect server response.": "Réponse du serveur incorrecte.",
    "No features found.": "Aucun objet trouvé.",
    /* GEOR.js strings */
    "Cities": "Localités",
    "Recentering on GeoNames cities": "Recentrage sur localités<br />de la base GeoNames",
    "Referentials": "Référentiels",
    "Recentering on a selection of referential layers": "Recentrage sur une sélection<br />de couches \"référentiels\"",
    "Addresses": "Adresses",
    "Recentering on a given address": "Recentrage sur point adresse",
    "Available layers": "Couches disponibles",
    "WMS Search": "Recherche WMS",
    "WFS Search": "Recherche WFS",
    "resultspanel.emptytext":
        "<p>Sélectionnez l'outil d'interrogation " +
        "ou construisez une requête sur une couche.<br />" +
        "Les attributs des objets s'afficheront dans ce cadre.</p>",
    /* GEOR_ClassificationPanel.js strings */
    "Attribute": "Attribut",
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
        "Pour plus d'information, vous pouvez " +
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
    "NAME layer": "Couche ${NAME}",
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
    "cswquerier.help.title": "Syntaxe pour la recherche avancée",
    "cswquerier.help.message": "<ul><li><b>@mot</b> cherche dans le nom de l'organisation.</li><li><b>#mot</b> cherche dans les mots clés de la métadonnée.</li><li><b>?mot</b> élargit la recherche à tous les champs de la métadonnée.</li></ul>",
    "NAME layer on VALUE": "Couche ${NAME} sur ${VALUE}",
    "Show metadata essentials in a window":
        "Afficher les métadonnées basiques",
    "Show metadata sheet in a new browser tab": 
        "Afficher la métadonnée complète dans un nouvel onglet",
    "more": "plus",
    "Click to select or deselect the layer":
        "Cliquez pour sélectionner ou désélectionner la couche",
    "Open the URL url in a new window":
        "Ouvrir l'url ${URL} dans une nouvelle fenêtre",
    "Unreachable server": "Serveur non disponible",
    "Catalogue": "Catalogue",
    "Find": "Chercher",
    "in": "dans",
    "No linked layer.": "Aucune couche.",
    "One layer found.": "Une couche trouvée.",
    "NB layers found.": "${NB} couches trouvées.",
    "NB metadata match the query.": "${NB} métadonnées correspondent à la requête.",
    "A single metadata matches the query.": "Une unique métadonnée correspond à la requête.",
    "Precise your request.": "Précisez votre requête.",
    "No metadata matches the query.":
        "Aucune métadonnée ne correspond à la requête.",
    "Limit to map extent": "Limiter à l'étendue de la carte",
    "Search limited to current map extent.": "Recherche limitée à l'étendue de la carte.",
    /* GEOR_fileupload.js strings */
    "2D only": "géométries 2D",
    "Local file": "Fichier",
    "The service is inactive": "Le service est inactif",
    "Upload a vector data file.": "Uploadez un fichier de données vectorielles.",
    "The allowed formats are the following: ": "Les formats acceptés sont les suivants : ",
    "Use ZIP compression for multifiles formats, such as": "Utilisez la compression ZIP pour les formats multi-fichiers comme",
    "fileupload_error_incompleteMIF": "Fichier MIF/MID incomplet.",
    "fileupload_error_incompleteSHP": "Fichier shapefile incomplet.",
    "fileupload_error_incompleteTAB": "Fichier TAB incomplet.",
    "fileupload_error_ioError": "Erreur d'I/O sur le serveur. Contacter l'administrateur de la plateforme pour plus de détails.",
    "fileupload_error_multipleFiles": "L'archive ZIP contient plusieurs fichiers de données. Elle ne doit en contenir qu'un seul.",
    "fileupload_error_outOfMemory": "Le serveur ne dispose plus de la mémoire suffisante. Contacter l'administrateur de la plateforme pour plus de détails.",
    "fileupload_error_sizeError": "Le fichier est trop grand pour pouvoir être traité.",
    "fileupload_error_unsupportedFormat": "Ce format de données n'est pas géré par l'application.",
    "fileupload_error_projectionError": "Une erreur est survenue lors de la lecture des coordonnées géographiques. Êtes-vous sûr que le fichier contient les informations de projection ?",
    "server upload error: ERROR":
        "L'upload du fichier a échoué. ${ERROR}",
    /* GEOR_geonames.js strings */
    /* GEOR_getfeatureinfo.js strings */
    "<div>Searching...</div>": "<div>Recherche en cours...</div>",
    "<div>No layer selected</div>": "<div>Aucune couche sélectionnée</div>",
    "<div>Search on objects active for NAME layer. Click on the map.</div>":
         "<div>Recherche d\'objets activée sur la couche ${NAME}. " +
         "Cliquez sur la carte.</div>",
    "WMS GetFeatureInfo at ": "GetFeatureInfo WMS sur ",
    /* GEOR_layerfinder.js strings */
    "metadata": "métadonnée",
    "Add layers from local files": "Ajouter des couches en uploadant un fichier depuis votre ordinateur",
    "Find layers searching in metadata":
        "Trouvez des couches en cherchant dans les métadonnées",
    "Find layers from keywords": "Trouvez des couches par mots clés",
    "Find layers querying OGC services":
        "Trouvez des couches en interrogeant des services OGC",
    "layerfinder.layer.unavailable":
        "La couche ${NAME} n'a pas été trouvée dans le service WMS.<br/<br/>" +
        "Peut-être n'avez-vous pas le droit d'y accéder " +
        "ou alors cette couche n'est plus disponible",
    "Layer projection is not compatible":
        "La projection de la couche n'est pas compatible.",
    "The NAME layer does not contain a valid geometry column":
        "La couche ${NAME} ne possède pas de colonne géométrique valide.",
    "Add": "Ajouter",
    "Add layers from a ...": "Ajouter des couches depuis un ...",
    "Malformed URL": "URL non conforme.",
    "Queryable": "Interrogeable",
    "Opaque": "Opaque",
    "OGC server": "Serveur OGC",
    "I'm looking for ...": "Je recherche ...",
    "Service type": "Type de service",
    "Choose a server": "Choisissez un serveur",
    "... or enter its address": "... ou saisissez son adresse",
    "The server is publishing one layer with an incompatible projection":
        "Le serveur publie une couche dont la projection n'est pas compatible",
    "The server is publishing NB layers with an incompatible projection":
        "Le serveur publie ${NB} couches dont la projection n'est pas " +
        "compatible",
    "This server does not support HTTP POST": "Ce serveur ne supporte pas HTTP POST",
    "Unreachable server or insufficient rights": "Réponse invalide du " +
        "serveur. Raisons possibles : droits insuffisants, " +
        "serveur injoignable, trop de données, etc.",
    /* GEOR_managelayers.js strings */
    "layergroup": "couche composite",
    "Service": "Service",
    "Protocol": "Protocole",
    "About this layer": "A propos de cette couche",
    "Set as overlay": "Passer en calque",
    "Set as baselayer": "Passer en couche de fond",
    "Tiled mode" : "Mode tuilé",
    "Confirm NAME layer deletion ?":
        "Voulez-vous réellement supprimer la couche ${NAME} ?",
    "1:MAXSCALE to 1:MINSCALE": "1:${MAXSCALE} à 1:${MINSCALE}",
    "Visibility range (indicative):<br />from TEXT":
        "Plage de visibilité (indicative):<br />de ${TEXT}",
    "Information on objects of this layer":
        "Interroger les objets de cette couche",
    "default style": "style par défaut",
    "no styling": "absence de style",
    "Recenter on the layer": "Recentrer sur la couche",
    "Impossible to get layer extent":
        "Impossible d'obtenir l'étendue de la couche.",
    "Refresh layer": "Recharger la couche",
    "Show metadata": "Afficher les métadonnées",
    "Edit symbology": "Éditer la symbologie",
    "Build a query": "Construire une requête",
    "Download data": "Extraire les données",
    "Choose a style": "Choisir un style",
    "Modify format": "Modifier le format",
    "Delete this layer": "Supprimer cette couche",
    "Push up this layer": "Monter cette couche",
    "Push down this layer": "descendre cette couche",
    "Add layers": "Ajouter des couches",
    "Remove all layers": "Supprimer toutes les couches",
    "Are you sure you want to remove all layers ?": "Voulez vous réellement supprimer toutes les couches ?",
    "source: ": "source : ",
    "unknown": "inconnue",
    "Draw new point": "Dessiner un nouveau point",
    "Draw new line": "Dessiner une nouvelle ligne",
    "Draw new polygon": "Dessiner un nouveau polygone",
    "Edition": "Edition",
    "Editing": "Edition en cours",
    "Switch on/off edit mode for this layer": "Basculer cette couche en mode édition",
    "No geometry column.": "Colonne géométrique non détectée.",
    "Geometry column type (TYPE) is unsupported.": "Le type de la colonne géométrique (${TYPE}) n'est pas supporté.",
    "Switching to attributes-only edition.": "Seuls les attributs des objets existants seront éditables.",
    /* GEOR_map.js strings */
    "Location map": "Carte de situation",
    "Warning after loading layer":
        "Avertissement suite au chargement de couche",
    "The <b>NAME</b> layer could not appear for this reason: ":
        "La couche <b>${NAME}</b> pourrait ne pas apparaître pour " +
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
    "Add layers from WFS services":
        "Ajouter des couches depuis des services WFS",
    "NB layers not imported": "${NB} couches non importées",
    "One layer not imported": "Une couche non importée",
    "mapinit.layers.load.error":
        "Les couches nommées ${LIST} n'ont pas pu être chargées. " +
        "Raisons possibles : droits insuffisants, SRS incompatible ou couche non existante",
    "NB layers imported": "${NB} couches importées",
    "One layer imported": "Une couche importée",
    "No layer imported": "Aucune couche importée",
    "The provided context is not valid": "Le contexte fourni n'est pas valide",
    "The default context is not defined (and it is a BIG problem!)":
        "Le contexte par défaut n'est pas défini " +
        "(et ce n'est pas du tout normal !)",
    "Error while loading file": "Erreur au chargement du fichier",
    /* GEOR_mappanel.js strings */
    "Coordinates in ": "Coordonnées en ",
    "scale picker": "échelle",
    /* GEOR_ows.js strings */
    "The NAME layer was not found in WMS service.":
        "La couche ${NAME} n'a pas été trouvée dans le service WMS.",
    "Problem restoring a context saved with buggy Chrome 36 or 37":
        "Nous ne pouvons restaurer un contexte enregistré avec Chrome 36 ou 37",
    /* GEOR_print.js strings */
    "Sources: ": "Sources : ",
    "Source: ": "Source : ",
    "Projection: PROJ": "Projection : ${PROJ}",
    "Print error": "Impression impossible",
    "Print server returned an error":
        "Le service d'impression a signalé une erreur.",
    "Contact platform administrator":
        "Contactez l'administrateur de la plateforme.",
    "Layer unavailable for printing": "Couche non disponible pour impression",
    "The NAME layer cannot be printed.":
        "La couche ${NAME} ne peut pas encore être imprimée.",
    "Unable to print": "Impression non disponible",
    "The print server is currently unreachable":
        "Le service d'impression est actuellement inaccessible.",
    "print.unknown.layout":
        "Erreur de configuration: DEFAULT_PRINT_LAYOUT " +
        "${LAYOUT} n'est pas dans la liste des formats d'impression",
    "print.unknown.resolution":
        "Erreur de configuration: DEFAULT_PRINT_RESOLUTION " +
        "${RESOLUTION} n'est pas dans la liste des résolutions d'impression",
    "print.unknown.format":
        "Erreur de configuration: le format " +
        "${FORMAT} n'est pas supporté par le serveur d'impression",
    "Pick an output format": "Choisissez un format de sortie",
    "Comments": "Commentaires",
    "Scale: ": "Échelle : ",
    "Date: ": "Date : ",
    "Minimap": "Mini-carte",
    "North": "Nord",
    "Scale": "Échelle",
    "Date": "Date",
    "Legend": "Légende",
    "Format": "Format",
    "Resolution": "Résolution",
    "Print the map": "Impression de la carte",
    "Print": "Imprimer",
    "Printing...": "Impression en cours...",
    "Print current map": "Imprimer la carte courante",
    /* GEOR_Querier.js strings */
    "Fields of filters with a red mark are mandatory": "Vous devez remplir " +
        "les champs des filtres marqués en rouge.",
    "Request on NAME": "Requêteur sur ${NAME}",
    "WFS GetFeature on filter": "GetFeature WFS sur un filtre",
    "Search": "Rechercher",
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
    "Symbology": "Symbologie",
    "Edit this panel's features symbology": "Editer la symbologie de la sélection",
    "Reset": "Réinitialiser",
    "Export is not possible: features have no geometry": "Export impossible : absence de géométries",
    "resultspanel.maxfeature.reached":
        " <span ext:qtip=\"Utilisez un navigateur plus performant " +
        "pour augmenter le nombre d'objets affichables\">" +
        "Nombre maximum d'objets atteint (${NB})</span>",
    "NB results": "${NB} résultats",
    "One result": "1 résultat",
    "No result": "Aucun résultat",
    "Clean": "Effacer",
    "All": "Tous",
    "None": "Aucun",
    "Invert selection": "Inverser la sélection",
    "Actions on the selection or on all results if no row is selected":
        "Actions sur la sélection ou sur tous les résultats si aucun n'est sélectionné",
    "Store the geometry": 
        "Enregistrer la géométrie",
    "Aggregates the geometries of the selected features and stores it in your browser for later use in the querier": 
        "La géométrie des objets sélectionnés est enregistrée pour un usage ultérieur dans le requêteur",
    "Geometry successfully stored in this browser": 
        "Géométrie enregistrée avec succès sur ce navigateur",
    "Clean all results on the map and in the table": "Supprimer les " +
        "résultats affichés sur la carte et dans le tableau",
    "Zoom": "Zoom",
    "Zoom to results extent": "Cadrer l'étendue de la carte sur celle " +
        "des résultats",
    "Export": "Export",
    "Export results as": "Exporter l'ensemble des résultats en",
    "<p>No result for this request.</p>": "<p>Aucun objet ne " +
        "correspond à votre requête.</p>",
    /* GEOR_scalecombo.js strings */
    /* GEOR_selectfeature.js strings */
    "<div>Select features activated on NAME layer. Click on the map.</div>":
        "<div>Sélection d\'objets activée sur la couche ${NAME}. " +
        "Cliquez sur la carte.</div>",
    "OpenLayers SelectFeature":"Sélection d\'objets",
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
    "no available attribute": "aucun attribut disponible.",
    /* GEOR_toolbar.js strings */
    "m": "m",
    "hectares": "hectares",
    "zoom to global extent of the map": "zoom sur l'étendue globale de la " +
        "carte",
    "pan": "glisser - déplacer la carte",
    "zoom in": "zoom en avant (pour zoomer sur une emprise: appuyer sur SHIFT + dessiner l'emprise)",
    "zoom out": "zoom en arrière",
    "back to previous zoom": "revenir à la précédente emprise",
    "go to next zoom": "aller à l'emprise suivante",
    "Login": "Connexion",
    "Logout": "Déconnexion",
    "Help": "Aide",
    "Query all active layers": "Interroger toutes les couches actives",
    "Show legend": "Afficher la légende",
    "Leave this page ? You will lose the current cartographic context.":
        "Vous allez quitter cette page et perdre le contexte cartographique " +
        "courant",
    "Online help": "Aide en ligne",
    "Display the user guide": "Afficher le guide de l'utilisateur",
    "Contextual help": "Aide contextuelle",
    "Activate or deactivate contextual help bubbles": "Activer ou désactiver les bulles d'aide contextuelle",
    /* GEOR_tools.js strings */
    "Tools": "Outils",
    "tools": "outils",
    "tool": "outil",
    "No tool": "aucun outil",
    "Manage tools": "Gérer les outils",
    "remember the selection": "se souvenir de la sélection",
    "Available tools:": "Outils disponibles :",
    "Click to select or deselect the tool": "Cliquez pour (dé)sélectionner l'outil",
    "Could not load addon ADDONNAME": "Impossible de charger l'addon ${ADDONNAME}",
    "Your new tools are now available in the tools menu.": 'Vos nouveaux outils sont disponibles dans le menu "outils"',
    /* GEOR_util.js strings */
    "Characters": "Caractères",
    "Digital": "Numérique",
    "Boolean": "Booléen",
    "Other": "Autre",
    "Confirmation": "Confirmation",
    "Information": "Information",
    "pointOfContact": "contact",
    "custodian": "producteur",
    "distributor": "distributeur",
    "originator": "instigateur",
    "More": "Plus",
    "Could not parse metadata.": "Impossible d'analyser la métadonnée",
    "Could not get metadata.": "Impossible d'obtenir la métadonnée",
    /* GEOR_waiter.js strings */
    /* GEOR_wmc.js strings */
    "The provided file is not a valid OGC context": "Le fichier fourni n'est pas un contexte OGC valide",
    "Warning: trying to restore WMC with a different projection (PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !": "Attention: le contexte restauré avait été sauvegardé en ${PROJCODE1} alors que la carte actuelle est en ${PROJCODE2}. Il pourrait y avoir des comportements inattendus.",
    /* GEOR_wmcbrowser.js strings */
    "all contexts": "toutes les cartes",
    "Could not find WMC file": "Le fichier spécifié n'existe pas",
    "... or a local context": "... ou une carte locale",
    "Load or add the layers from one of these map contexts:": "Charger ou ajouter les couches de l'une de ces cartes :",
    "A unique OSM layer": "Une unique couche OpenStreetMap",
    "default viewer context": "carte par défaut",
    "(default)": "<br/>(carte par défaut)",
    /* GEOR_workspace.js strings */
    "Created:": "Date de création : ",
    "Last accessed:": "Date de dernier accès : ",
    "Access count:": "Nombre d'accès : ",
    "Permalink:": "Permalien : ",
    "My contexts": "Mes cartes",
    "Created": "Création",
    "Accessed": "Accédé",
    "Count": "Accès",
    "View": "Visualiser",
    "View the selected context": "Visualiser la carte sélectionnée (attention : remplacera la carte courante)",
    "Download": "Télécharger",
    "Download the selected context": "Télécharger la carte sélectionnée",
    "Delete": "Supprimer",
    "Delete the selected context": "Supprimer la carte sélectionnée",
    "Failed to delete context": "Impossible de supprimer la carte",
    "Manage my contexts": "Gérer mes cartes",
    "Keywords": "Mots clés",
    "comma separated keywords": "mots clés séparés par une virgule",
    "Save to metadata": "Créer une métadonnée",
    "in group": "dans le groupe",
    "The context title is mandatory": "Le titre de la carte est obligatoire",
    "There was an error creating the metadata.": "La création de la métadonnée a échoué.",
    "Share this map": "Partager cette carte",
    "Mobile viewer": "Visualiseur mobile",
    "Mobile compatible viewer on sdi.georchestra.org": "Visualiseur mobile sur sdi.georchestra.org",
    "Desktop viewer": "Visualiseur web",
    "Desktop viewer on sdi.georchestra.org": "Visualiseur web sur sdi.georchestra.org",
    "Abstract": "Résumé",
    "Context saving": "Sauvegarde de la carte",
    "The file is required.": "Un nom de fichier est nécessaire.",
    "Context restoring": "Restauration d'une carte",
    "<p>Please note that the WMC must be UTF-8 encoded</p>": "<p>Notez que le" +
        " fichier de contexte doit être encodé en UTF-8.</p>",
    "Load": "Charger",
    "Workspace": "Espace de travail",
    "Save the map context": "Sauvegarder la carte",
    "Load a map context": "Charger une carte",
    "Get a permalink": "Obtenir un permalien",
    "Permalink": "Permalien",
    "Share your map with this URL: ": "Partagez la carte avec l'adresse suivante : ",
    /* GEOR_edit.js */
    "Req.": "Req.", // requis
    "Required": "Requis",
    "Not required": "Non requis",
    "Synchronization failed.": "Erreur lors de la synchronisation.",
    "Edit activated": "Edition activée", 
    "Hover the feature you wish to edit, or choose \"new feature\" in the edit menu": "Survolez les objets de la couche que vous souhaitez modifier, ou choisissez \"nouvel objet\" dans le menu d'édition de la couche",
    /* GeoExt.data.CSW.js */
    "no abstract": "pas de résumé"
    // no trailing comma
});

GeoExt.Lang.add("fr", {
    "GeoExt.ux.FeatureEditorGrid.prototype": {
        deleteMsgTitle: "Suppression",
        deleteMsg: "Confirmer la suppression de cet objet vectoriel ?",
        deleteButtonText: "Supprimer",
        deleteButtonTooltip: "Supprimer cet objet",
        cancelMsgTitle: "Annulation",
        cancelMsg: "L'objet a été modifié localement. Confirmer l'abandon des changements ?",
        cancelButtonText: "Annuler",
        cancelButtonTooltip: "Abandonner les modifications en cours",
        saveButtonText: "Enregistrer",
        saveButtonTooltip: "Enregistrer les modifications",
        nameHeader: "Attribut",
        valueHeader: "Valeur"
    }
});
