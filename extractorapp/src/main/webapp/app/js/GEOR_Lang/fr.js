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
    /* Common strings */
    "Email": "Email",
    "OK": "OK",
    /* GEOR.js strings */
    "Enter a valid email address: ": "Saisissez une adresse email valide : ",
    "The email address is not valid. Stopping extraction.":
        "L'email n'est pas valide. Abandon de l'extraction.",
    "Not any layer in the cart": "Aucune couche dans le panier",
    "You did not select any layer for extracting. Extract all ?":
        "Vous n'avez pas sélectionné de couche pour l'extraction. " +
        "Tout extraire ?",
    "Yes": "Oui",
    "No": "Non",
    "Cancel": "Annuler",
    "Extraction parameters applied by default to all cart layers":
        "Paramètres d'extraction appliqués par défaut à toutes les couches " +
        "du panier",
    "paneltext1":
        "Configurez les paramètres généraux de votre extraction en utilisant " +
        "le panneau ci-contre à droite (affiché en sélectionnant " +
        "'Paramètres par défaut').",
    "paneltext2":
        "Vous pouvez ensuite lancer l'extraction en cliquant sur le bouton " +
        "'Extraire les couches cochées'.",
    "paneltext3":
        "Si vous souhaitez préciser des paramètres d'extraction spécifiques " +
        "pour une couche donnée, sélectionnez la dans l'arbre ci-dessous.",
    "Extractor": "Extracteur",
    "Configuration": "Configuration",
    "Extract the selected layers": "Extraire les couches cochées",
    "Recenter": "Recentrage",
    "Use limits": "Limites d'utilisation",
    "Extraction parameters only for the NAME layer (raster)":
        "Paramètres d'extraction spécifiques à la couche ${NAME} (raster)",
    "Extraction parameters only for the NAME layer (vector)":
        "Paramètres d'extraction spécifiques à la couche ${NAME} (vecteur)",
    /* GEOR_ajaxglobal.js */
    "The server did not return nothing.": "Le serveur n'a pas répondu.",
    "The server did not allow access.": "Le serveur a refusé de répondre.",
    "ajaxglobal.error.406":
        "Le serveur distant a répondu, mais le contenu de la " +
        "réponse n'est pas conforme à ce que nous attendons. " +
        "FireFox s'en sort mieux que Internet Explorer dans certaines " +
        "de ces situations. Ce serait probablement une bonne idée que " +
        "d'essayer avec un autre navigateur !",
    "ajaxglobal.error.default":
        "Pour plus d'information, nous vous invitons à " +
        "chercher le code de retour sur <a href=\"http://" +
        "en.wikipedia.org/wiki/List_of_HTTP_status_codes\">" +
        "cette page</a>.",
    "ajaxglobal.error.title": "Erreur HTTP ${ERROR}",
    "ajaxglobal.error.body": "Une erreur est survenue.<br />${TEXT}",
    "Warning: the browser may freeze":
        "Attention : risque de blocage du navigateur",
    "ajaxglobal.toobig":
        "Les données provenant du serveur sont trop volumineuses.<br />" +
        "Le serveur a envoyé ${WEIGHT}KO (la limite est à " +
        "${LIMIT}KO). <br />Voulez-vous tout de même continuer ?",
    /* GEOR_config.js */
    "None": "Aucun",
    "BUFFER meters": "${BUFFER} mètres",
    "1 kilometer": "1 kilomètre",
    "BUFFER kilometers": "${BUFFER} kilomètres",
    /* GEOR_data.js */
    /* GEOR_dlform.js */
    "Firstname": "Prénom",
    "Lastname": "Nom",
    "Company": "Organisme",
    "Phone": "Téléphone",
    "Applications": "Applications",
    "Comments": "Commentaires",
    "dlform.blanktext":
        "Cochez la case pour accepter les conditions d'utilisation",
    "dlform.checkbox":
        "<span style='font-weight:bold;'>J'accepte sans réserve les <a href='" +
        "${URL}' target='_blank'>conditions d'utilisation</a> " +
        "des données.</span>",
    "Invalid form": "Formulaire invalide",
    "dlform.save.error":
        "Impossible de sauver le formulaire. " +
        "Merci de contacter l'administrateur de la plateforme.",
    "Take one minute to indicate how you use the data":
        "Prenez quelques instants pour nous indiquer l'utilisation des données",
    "dlform.mandatory.fields":
        "<div style='font-size:12px;'>Les champs en " +
        "<span style='font-weight:bold;'>gras</span>" +
        " sont obligatoires.</div>",
    /* GEOR_layeroptions.js */
    "layeroptions.boundingbox":
        "Emprise (en ${UNIT}, " +
        'SRS = <a href="http://spatialreference.org/ref/epsg/${NUMBER}/"' +
        'target="_blank" style="text-decoration:none">${CRS}</a>)',
    " km²": " km²",
    " m²": " m²",
    "Output projection": "Projection de sortie",
    "Raster resolution (m/pixel)": "Résolution raster (m/pixel)",
    "Raster output format": "Format de sortie raster",
    "Vector output format": "Format de sortie vecteur",
    "Output format": "Format de sortie",
    "Bounding box": "Emprise",
    /* GEOR_layerstree */
    "ERROR: owsinfo.layer should always be defined":
        "ERREUR: owsinfo.layer devrait être toujours défini",
    "layerstree.qtip.wfs": "Service WFS <b>${TEXT}</b><br/>${URL}",
    "layerstree.maxfeatures":
        "Le nombre maximal d'objets a été atteint : seulement " +
        "${NB} objets sont affichés.",
    "layerstree.qtip.missingwfs":
        "La couche WFS <b>${NAME}</b> n'existe pas sur le service spécifié " +
        "(${URL})",
    "layerstree.qtip.unavailablewfs":
        "Service WFS <b>${NAME}</b> non disponible<br/>${URL}",
    "layerstree.qtip.wms": "Service WMS <b>${NAME}</b><br/>${URL}",
    "layerstree.qtip.badprojection":
        "Impossible de trouver une projection supportée " +
        "pour la couche WMS <b>${NAME}</b>",
    "layerstree.qtip.missingwms":
        "La couche WMS <b>${NAME}</b> n'existe pas sur le service spécifié " +
        "(${URL})",
    "layerstree.qtip.unavailablewms":
        "Service WMS <b>${NAME}</b> non disponible<br/>${URL}",
    "layerstree.layer.tip":
        "Sélectionnez la couche pour la visualiser " +
        "et configurer ses paramètres d'extraction spécifiques.<br/>" +
        "Cochez la case pour ajouter la couche au panier d'extraction. " +
        "Décochez la case pour retirer la couche du panier.",
    "layerstree.qtip.noextraction":
        "La couche <b>${NAME}</b> n'est pas disponible : " +
        "aucun service d'extraction",
    "layerstree.qtip.invalidwcs":
        "La couche <b>${NAME}</b> n'est pas disponible : Le service WCS " +
        "${URL} n'est pas valide",
    "layerstree.describelayer":
        "La couche <b>${NAME}</b> n'est pas disponible : la requête WMS " +
        "DescribeLayer sur ${URL} n'a pas abouti.",
    "Your extraction cart": "Votre panier d'extraction",
    "Loading...": "Chargement...",
    "Default parameters": "Paramètres par défaut",
    "layerstree.qtip.defaultparameters":
        "<b>Paramètres par défaut</b><br/>" +
        "Ces paramètres sont appliqués à l'extraction de toute couche " +
        "ne faisant pas l'objet de paramètres spécifiques.",
    "OGC Layers": "Couches OGC",
    "OGC layers available for extraction":
        "Couches OGC disponibles pour extraction",
    "OGC services": "Services OGC",
    "The layers of these OGC services can be extracted":
        "Services OGC dont les couches peuvent être extraites",
    "layerstree.email":
        "Extraction en cours.\n" +
        "Un email sera envoyé à l'adresse ${EMAIL} " +
        "lorsque l'extraction sera terminée.",
    "The extraction request failed.": "La requête d'extraction n'a pas abouti.",
    /* GEOR_map.js */
    "Layer probably invisible at this scale: ":
        "Couche probablement non visible à cette échelle : ",
    "Base Layer": "Couche de base",
    /* GEOR_mappanel.js */
    "mappanel.qtip.coordinates": "Coordonnées du pointeur en ${SRS}",
    /* GEOR_ows.js */
    /* GEOR_proj4jsdefs.js */
    /* GEOR_referentials.js */
    "Referential": "Référentiel",
    "Select": "Sélectionnez",
    "The selected layer does not have a geometric column":
        "La couche sélectionnée ne possède pas de colonne géométrique",
    "Recenter on": "Recentrer sur",
    "location ?": "localité ?",
    "referentials.help":
        "<span>Cet outil permet de calquer l'emprise d'extraction courante " +
        "sur celle d'une entité de référence.</span>",
    /* GEOR_toolbar.js */
    "Zoom on the global extent of the map":
        "Zoom sur l'étendue globale de la carte",
    "Pan": "Glisser - déplacer la carte",
    "Zoom in": "Zoom en avant",
    "Back to the previous extent": "Revenir à la précédente emprise",
    "Go to the next extent": "Aller à l'emprise suivante",
    "Help": "Aide",
    "Show help": "Afficher l'aide",
    "Extractor help": "Aide de l'extracteur",
    "Login": "Connexion",
    "Logout": "Déconnexion",
    "toolbar.confirm.login":
        "Vous allez quitter cette page et perdre le contexte cartographique " +
        "courant",
        // ou : "Pour vous connecter, nous vous redirigeons vers une autre
        // page web. Vous risquez de perdre le contexte cartographique courant.
        // Vous pouvez le sauvegarder en annulant cette opération, et en
        // cliquant sur Espace de travail > Sauvegarder la carte" ?
    /* GEOR_util.js */
    "Confirm": "Confirmation",
    "Information": "Information",
    "Error": "Erreur",
    "degrees": "degrés",
    "meters": "mètres",
    /* GEOR_waiter.js */
    /* OpenLayers.Control.OutOfRangeLayers.js */
    "List of layers out of range: ": "Liste des couches hors du zoom.",
    /* BoundingBoxPanel.js */
    "Modify the bounding box": "Modifier cette emprise",
    "Modify the bounding box drawing a new one on the map":
        "Modifier l'emprise en en dessinant une nouvelle sur la carte",
    // no trailing comma
});
