/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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
 * Deutsch translation file
 */
OpenLayers.Lang.de = OpenLayers.Util.extend(OpenLayers.Lang.de, {
    /* Common strings */
    "Email": "Email",
    "OK": "OK",
    /* GEOR.js strings */
    "Enter a valid email address: ": "Gültige Emailadresse angeben : ",
    "The email address is not valid. Stopping extraction.":
        "Email ungültig",
    "No layer in the cart": "kein layer im Warenkorb",
    "You did not select any layer for extracting. Extract all ?":
        "Alles extrahieren ?",
    "Yes": "Ya",
    "No": "Nein",
    "Cancel": "Abbrechen",
    "Extraction parameters applied by default to all cart layers":
        "Standard Parameter für alle Layers anwendbar " +
        "im Warenkorb",
    "paneltext1":
        "Benutzen Sie die Liste rechts um die allgemeinen " +
        "Parameter der Extraktion zu definieren.",
    "paneltext2":
        "Sie können jetzt extrahieren veranlassen, indem " +
        "'Extraire les couches cochées'.",
    "paneltext3":
        "Si vous souhaitez préciser des paramètres d'extraction spécifiques " +
        "Sie den Button 'Angekreuzte Layer extrahieren' drücken",
    "Extractor": "Extractor",
    "Configuration": "Konfiguration",
    "Extract the selected layers": "Angekreuzte Layer extrahieren",
    "Recenter": "Rezentrieren",
    "Use limits": "Benutzerlimit",
    "Extraction parameters only for the NAME layer (raster)":
        "Parameter für Raster Layers anwendbar ${NAME} (raster)",
    "Extraction parameters only for the NAME layer (vector)":
        "Parameter für Raster Layers anwendbar ${NAME} (vector)",
    "Oversized coverage extraction": "Extraktionsgebiet zu groß",
    "Extraction area for layers LAYERS is too large.<br/><br/>We cannot produce images with more than MAX million RGB pixels.<br/>Continue anyway ?": 
        "Das gewählte Extraktionsgebiet für die Layer ${LAYERS} ist zu groß.<br/><br/>Wir können keine Bilder erstellen mit mehr als ${MAX} Million RGB-Pixel.<br/>Trotzdem fortfahren?",
    /* GEOR_ajaxglobal.js */
    "The server did not return nothing.": "Der Server hat nicht geantwortet.",
    "The server did not allow access.": "Der Server verweigert die Verbindung.",
    "ajaxglobal.error.406":
        "ajaxglobal.error.406",
    "ajaxglobal.error.default":
        "Für weitere Informationen " +
        "<a href=\"http://" +
        "en.wikipedia.org/wiki/List_of_HTTP_status_codes\">" +
        "diese Seite</a>.",
    "ajaxglobal.error.title": "HTTP Fehler ${ERROR}",
    "ajaxglobal.error.body": "Fehler aufgetreten<br />${TEXT}",
    "Warning: the browser may freeze":
        "Achtung: Blockierrisiko des Navigators",
    "ajaxglobal.toobig":
        "Datei zu groß.<br />" +
        "Der Rechner sendet ${WEIGHT}KO (das Limit ist " +
        "${LIMIT}KO). <br />Wollen Sie weitermachen?",
    /* GEOR_config.js */
    "None": "kein",
    "BUFFER meters": "${BUFFER} meter",
    "BUFFER kilometer": "${BUFFER} kilometer",
    "BUFFER kilometers": "${BUFFER} kilometer",
    /* GEOR_data.js */
    /* GEOR_layeroptions.js */
    "layeroptions.boundingbox":
        "Kartenansicht (im ${UNIT}, " +
        'SRS = <a href="http://spatialreference.org/ref/epsg/${NUMBER}/"' +
        'target="_blank" style="text-decoration:none">${CRS}</a>)',
    " km²": " km²",
    " m²": " m²",
    "Output projection": "Projektionsausdruck",
    "Raster resolution (m/pixel)": "Raster-Auflösung (m/pixel)",
    "Raster output format": "Ausdruckformat Raster",
    "Vector output format": "Ausdruckformat Vektor",
    "Output format": "Ausdruckformat",
    "Bounding box": "Kartenansicht",
    /* GEOR_layerstree */
    "ERROR: owsinfo.layer should always be defined":
        "ERREUR: owsinfo.layer  muss immer definiert sein",
    "layerstree.qtip.wfs": "WFS Service <b>${TEXT}</b><br/>${URL}",
    "layerstree.maxfeatures":
        "Maximale Anzahl erreicht : nur " +
        "${NB} Objekte sind angezeigt.",
    "layerstree.qtip.missingwfs":
        "WFS Layer <b>${NAME}</b> existiert nicht in diesem Service " +
        "(${URL})",
    "layerstree.qtip.unavailablewfs":
        "WFS Service <b>${NAME}</b> nicht verfügbar<br/>${URL}",
    "layerstree.qtip.wms": "WMS Service <b>${NAME}</b><br/>${URL}",
    "layerstree.qtip.badprojection":
        "Keine vergleichbare Projektion gefunden " +
        "für den WMS Layer <b>${NAME}</b>",
    "layerstree.qtip.missingwms":
        "WMS Layer <b>${NAME}</b> existiert nicht in diesem Service " +
        "(${URL})",
    "layerstree.qtip.unavailablewms":
        "WMS Service <b>${NAME}</b> nicht verfügbar<br/>${URL}",
    "layerstree.layer.tip":
        "Layer zur Ansicht auwählen " +
        "Extraktionsparameter konfigurieren.<br/>" +
        "Ankreuzen um Layer in den Warenkorb zu legen. " +
        "Markierung löschen um Layer aus dem Warenkorb zu nehmen.",
    "layerstree.qtip.noextraction":
        "Layer <b>${NAME}</b> nicht verfügbar : " +
        "kein Extraktions Service",
    "layerstree.describelayer":
        "Layer <b>${NAME}</b> nicht verfügbar : die WMS Anfrage " +
        "DescribeLayer auf ${URL} ohne Erfolg.",
    "Your extraction cart": "Ihr Warenkorb",
    "Loading...": "im Aufbau...",
    "Default parameters": "Standardparameter",
    "layerstree.qtip.defaultparameters":
        "<b>Standardparameter</b>",
    "OGC Layers": "OGC Layer",
    "OGC layers available for extraction":
        "OGC Layers zur Extraktion",
    "OGC services": "OGC Service",
    "The layers of these OGC services can be extracted":
        "OGC Service mit Möglichkeit der Layerextraktion",
    "layerstree.email":
        "Extraktion läuft.\n" +
        "Senden einer Email ${EMAIL} " +
        "sobald Extraktion beendet.",
    "The extraction request failed.": "Extraktionsuche ohne Erfolg.",
    /* GEOR_map.js */
    "Layer probably invisible at this scale: ":
        "Layer in diesem Maßstab wahrscheinlich nicht sichtbar : ",
    "Base Layer": "Basislayer",
    /* GEOR_mappanel.js */
    "mappanel.qtip.coordinates": "Koordinaten im ${SRS}",
    /* GEOR_ows.js */
    /* GEOR_proj4jsdefs.js */
    /* GEOR_referentials.js */
    "Referential": "Referential",
    "Select": "Auswählen",
    "The selected layer does not have a geometric column":
        "Geometrische Spalte nicht vorhanden",
    "Recenter on": "Rezentrieren auf",
    "location ?": "Standorte ?",
    "referentials.help":
        "<span>Dieses Modul berechnet den Ausschnitt der Kartenansicht " +
        "aus einem Referenzort.</span>",
    /* GEOR_toolbar.js */
    "Zoom on the global extent of the map":
        "Maximaler Kartenausschnitt",
    "Pan": "Karte verschieben",
    "Zoom in": "Vergrössern",
    "Back to the previous extent": "vorherige Kartenansicht",
    "Go to the next extent": "Zur nächsten Kartenansicht gehen",
    "Help": "Hilfe",
    "Show help": "Hilfe anzeigen",
    "Extractor help": "Extraktor Hilfe",
    "Login": "Login",
    "Logout": "Logout",
    "toolbar.confirm.login":
        "Sie verlassen diese Seite und verlieren die erstellte Karten ",
    /* GEOR_util.js */
    "Confirm": "Bestätigung",
    "Information": "Information",
    "Error": "Fehler",
    "degrees": "Winkelgrade",
    "meters": "meter",
    /* GEOR_waiter.js */
    /* OpenLayers.Control.OutOfRangeLayers.js */
    "List of layers out of range: ": "Layer außerhalb Zoom.",
    /* BoundingBoxPanel.js */
    "Modify the bounding box": "Kartenansicht ändern",
    "Modify the bounding box drawing a new one on the map":
        "Kartenansicht ändern durch Zeichnen einer neuen Karte"
    // no trailing comma
});
