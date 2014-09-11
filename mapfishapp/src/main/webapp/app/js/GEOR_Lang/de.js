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
 * @requires GeoExt/Lang.js
 */

/*
 * German translation file
 */
OpenLayers.Lang.de = OpenLayers.Util.extend(OpenLayers.Lang.de, {
    /* General purpose strings */
    "Yes": "Ya",
    "No": "Nein",
    "OK": "OK",
    "or": "oder",
    "Cancel": "Annulieren",
    "Save": "Speichern",
    "Loading...": "im Aufbau...",
    "File": "Datei",
    "Layer": "Layer",
    "Description": "Beschreibung",
    "Error": "Fehler",
    "Server": "Server",
    "Close": "Schlieβen",
    "labelSeparator": " : ",
    "File submission failed or invalid file": "Sendung abgebrochen - fehlerhafte Datei",
    "Type": "Typ",
    "Title": "Titel",
    "Actions": "Aktionen",
    /* GEOR.js strings */
    "Cities": "Standorte",
    "Recentering on GeoNames cities": "Auf die Standorte der GeoNames <br />Datenbasis neu ausrichten",
    "Referentials": "Datenbasis",
    "Recentering on a selection of referential layers": "Auf Referenzlayer neu ausrichten",
    "Addresses": "Adressen",
    "Recentering on a given address": "Auf der Adresse neu ausrichten",
    "Available layers": "Layers verfügbar",
    "WMS Search": "WMS suchen",
    "WFS Search": "WFS suchen",
    "resultspanel.emptytext":
        "<p>Wählen Sie das Tools aus oder setzen " +
        "Sie eine Anfrage auf einen Layer.<br /> " +
        "Die Attribute der Objekte werden in diesem Rahmen angezeigt.</p>",
    /* GEOR_ClassificationPanel.js strings */
    "Attribute": "Attribut",
    "Number of classes": "Anzahl der Klassen",
    "Minimum size": "Minimalgröβe",
    "Maximum size": "Maximalgröβe",
    "First color": "Erste Farbe",
    "Last color": "Letzte Farbe",
    "Palette": "Farbpalette",
    "Auto classification": "Automatische Klassifizierung",
    "Classify": "Klassifizieren",
    "Unique values": "Einzelwerte",
    "Color range": "Farbnuancen",
    "Proportional symbols": "Proportionale Symbole",
    /* GEOR_FeatureDataModel.js strings */
    "objects": "Objekte",
    /* GEOR_address.js strings */
    "Go to: ": "Gehen zu : ",
    "searching...": "suchen...",
    "adressSearchExemple": "z.B.: Hausnummer, Straße, Ort",
    /* GEOR_ajaxglobal.js strings strings */
    "Server did not respond.": "Der Server hat nicht geantwortet.",
    "Server access denied.": "Der Server verweigert die Verbindung.",
    "ajax.badresponse":
        "Der Server hat geantwortet, aber der " +
        "Inhalt entspricht nicht der erwarteten Antwort",
    "Server unavailable.": "Der Server ist vorläufig unerreichbar Bitte versuchen Sie es später noch einmal.",
    "Too much data.": "Daten zu umfangreich.",
    "Server exception.": "Der Server hat eine Ausnahme zurückgesendet.",
    "ajax.defaultexception":
        "Für mehr Informationen benützten Sie " +
        "den Rückkehrkode dieser <a href=\"http://" +
        "en.wikipedia.org/wiki/List_of_HTTP_status_codes\" target=\"_blank\">" +
        "Seite</a>.",
    "An error occured.<br />": "Fehler aufgetreten.<br />",
    "Warning : browser may freeze": "Achtung: Blockierrisiko des Navigators",
    "ajaxglobal.data.too.big": "Die vom Rechner kommenden Daten sind zu " +
        "umfangreich.<br />der Rechner hat gesendet ${SENT}KO " +
        "(das Limit ist ${LIMIT}KO)<br />Wollen Sie trotzdem weitergehen?",
    /* GEOR_config.js strings */
    /* GEOR_cswbrowser.js strings */
    "NAME layer": "Layer ${NAME}",
    "Metadata without a name": "Namenlose Metadaten",
    "The getDomain CSW query failed": "Die CSW getDomain Anfrage ist gescheitert",
    "Error for the thesaurus": "Fehler auf Thesaurus",
    "Missing key to access the thesaurus":
        "Schlüssel fehlt um Thesaurus zu öffnen",
    "Keywords query failed": "Anfrage nach Schlüsselwörtern ist gescheitert",
    "Thesaurus:": "Thesaurus :",
    "Thesaurus": "Thesaurus",
    "cswbrowser.default.thesaurus.mismatch":
        "Administrator: Konfigurationsproblem - " +
        "die Variable DEFAULT_THESAURUS_KEY entspricht keinem" +
        " von GeoNetwork exportierten Wert",
    /* GEOR_cswquerier.js strings */
    "NAME layer on VALUE": "Layer ${NAME} auf ${VALUE}",
    "Show metadata sheet in a new window":
        "Metadatei in neuem Fenster öffnen",
    "more": "mehr",
    "Click to select or deselect the layer":
        "Klicken Sie um den Layer anzuwählen oder auszuschalten",
    "Open the URL url in a new window":
        "Url ${URL}  in neuem Fenster öffnen",
    "Unreachable server": "Server  nicht verfügbar",
    "Catalogue": "Katalog",
    "Find": "suchen",
    "in": "in",
    "No layer": "Kein Layer",
    "One layer found.": "Layer gefunden.",
    "NB layers found.": "${NB} Layers gefunden.",
    "NB metadata match the query.": "${NB} Metadaten entsprechen der Anfrage",
    "A single metadata matches the query.": "eine einzige Metadatei entspricht der Anfrage",
    "Precise your request.": "Präzisieren Sie Ihre Suche",
    "No metadata matches the query.":
        "Keine Metadaten entsprechen der Anfrage.",
    /* GEOR_fileupload.js strings */
    "2D only": "nur 2D",
    "Local file": "Datei",
    "The service is inactive": "Dienstleistung ist nicht aktiv",
    "Upload a vector data file.": "Laden einer Datei mit vektoriellen Daten",
    "The allowed formats are the following: ": "folgende Formate sind gestattet : ",
    "Use ZIP compression for multifiles formats, such as": "Benützen Sie ZIP um Multidatei-Format zu komprimieren wie",
    "fileupload_error_incompleteMIF": "Datei MIF/MID nicht komplett.",
    "fileupload_error_incompleteSHP": "Datei SHP nicht komplett.",
    "fileupload_error_incompleteTAB": "Datei TAB nicht komplett.",
    "fileupload_error_ioError": "I/O Fehler. Kontaktieren Sie den Administrator.",
    "fileupload_error_multipleFiles": "ZIP Archiv beinhaltet mehrere Dateien. Nur eine Datei möglich.",
    "fileupload_error_outOfMemory": "Speicherkapazität nicht ausreichend",
    "fileupload_error_sizeError": "Datei zu groß.",
    "fileupload_error_unsupportedFormat": "Dateiformat ungültig.",
    "fileupload_error_projectionError": "Lesefehler der geografischen Koordinaten. Überprüfen Sie die Projektioninformation.",
    "server upload error: ERROR":
        "Fehler beim Dateihochladen. ${ERROR}",
    "Incorrect server response.": "Server Antwort fehlerhaft.",
    "No features found.": "Kein Betreff gefunden.",
    /* GEOR_geonames.js strings */
    /* GEOR_getfeatureinfo.js strings */
    "<div>Searching...</div>": "<div>suchen...</div>",
    "<div>No layer selected</div>": "<div>kein Layer markiert</div>",
    "<div>Search on objects active for NAME layer. Click on the map.</div>":
         "<div>Betreffsuche aktiviert auf Layer ${NAME}. " +
         "klicken Sie auf die Karte.</div>",
    "WMS GetFeatureInfo at ": "GetFeatureInfo WMS auf ",
    /* GEOR_layerfinder.js strings */
    "Add layers from local files": "Layer aus lokalen Daten hinzufügen",
    "Find layers searching in metadata":
        "Layer finden durch Suche in Metadaten",
    "Find layers from keywords": "Layer finden durch Suche mit Schlüsselwörtern",
    "Find layers querying WMS servers":
        "Layer finden durch Abfrage des Servers WMS",
    "Find layers querying WFS servers":
        "Layer finden durch Abfrage des Servers WFS",
    "layerfinder.layer.unavailable":
        "Layer ${NAME} im WMS Service nicht gefunden.<br/<br/>" +
        "Mögliche Gründe : " +
        "Nicht ausreichendes Recht, Layer nicht verfügbar",
    "Layer projection is not compatible":
        "Layerprojektion nicht kompatibel",
    "The NAME layer does not contain a valid geometry column":
        "Layer  ${NAME} besitzt keine geometrisch gültige Spalte",
    "Add": "Hinzufügen",
    "Add layers from a ...": "Layer hinzufügen aus einem...",
    "Malformed URL": "URL ungültig.",
    "Queryable": "abfragbar",
    "Opaque": "Undurchlässig",
    "Choose a WMS server: ": "Wählen Sie einen WMS Server: ",
    "The server is publishing one layer with an incompatible projection":
        "der Server zeigt Layer mit nicht kompatibler Projektion",
    "The server is publishing NB layers with an incompatible projection":
        "der Server zeigt  ${NB} Layers mit nicht kompatibler Projektion",
    "WMS server": "WMS Server",
    "Choose a WMTS server: ": "Wählen Sie einen WMTS Server: ",
    "WMTS server": "WMTS Server",
    "Choose a WFS server: ": "Wählen Sie einen WFS Server: ",
    "... or enter its address: ": "... oder geben Sie Ihre:",
    "Unreachable server or insufficient rights": "Server nicht erreichbar " +
        " oder nicht ausreichende Zugriffsrechte, " +
        "Datenmenge zu groß...",
    "WFS server": "WFS Server",
    /* GEOR_managelayers.js strings */
    "Set as overlay": "Wechseln in nächsthöhere Schicht",
    "Set as baselayer": "Wechseln in nächsttiefere Schicht",
    "Confirm NAME layer deletion ?":
        "Bestätigung des Löschvorgangs des Layers ${NAME} ?",
    "1:MAXSCALE to 1:MINSCALE": "1:${MAXSCALE} bis 1:${MINSCALE}",
    "Visibility range (indicative):<br />from TEXT":
        "Zoomschärfe : :<br /> ${TEXT}",
    "Information on objects of this layer":
        "Layer abfragen",
    "default style": "Standartstil",
    "no styling": "kein Stil",
    "Recenter on the layer": "Neuzentrieren auf dem Layer",
    "Impossible to get layer extent":
        "Finden der Layerweite unmöglich.",
    "Refresh layer": "Layer refresh",
    "Show metadata": "Metadaten anzeigen",
    "Edit symbology": "Symbologie bearbeiten",
    "Build a query": "Suche aufstellen",
    "Download data": "Daten downloaden",
    "Choose a style": "Stil",
    "Modify format": "Format ändern",
    "Delete this layer": "Layer löschen",
    "Push up this layer": "Layer darüberlegen",
    "Push down this layer": "Layer darunterlegen",
    "Add layers": "Layer hinzufügen",
    "Remove all layers": "Alle Layers löschen",
    "Are you sure you want to remove all layers ?": "Sind Sie sicher, dass Sie alle Layers löschen wollen ?",
    "source: ": "Quelle : ",
    "unknown": "Unbekannt",
    "Draw new point": "Neuen Punkt zeichnen",
    "Draw new line": "Neue Linie zeichnen",
    "Draw new polygon": "Neuen Polygon zeichnen",
    "Edition": "Bearbeitung",
    "Editing": "Bearbeitung im Aufbau",
    "Switch on/off edit mode for this layer": "Layer in Bearbeitungsmodus wechseln",
    "No geometry column.": "Geometrische Spalte nicht gefunden.",
    "Geometry column type (TYPE) is unsupported.": "${TYPE}) Typ geometrische Spalte ungültig.",
    "Switching to attributes-only edition.": "Nur die Objektattribute sind veränderbar.",
    /* GEOR_map.js strings */
    "Location map": "Lageplan",
    "Warning after loading layer":
        "Warnug wegen Layer - Ladung",
    "The <b>NAME</b> layer could not appear for this reason: ":
        "Layer <b>${NAME}</b> könnte aus folgendem Grund " +
        "nicht aufgerufen werden : ",
    "Min/max visibility scales are invalid":
        "die Maßstäbe min/max sind ungültig.",
    "Visibility range does not match map scales":
        "Zoomschärfe entspricht nicht dem Kartenmaßstab.",
    "Geografic extent does not match map extent":
        "die geografische Weite entspricht nicht der der Karte.",
    /* GEOR_mapinit.js strings */
    "Add layers from WMS services":
        "Layer aus WMS Service hinzufügen",
    "Add layers from WFS services":
        "Layer aus WFS Service hinzufügen",
    "NB layers not imported": "${NB} Layers nicht zugefügt",
    "One layer not imported": "Layer nicht zugefügt",
    "mapinit.layers.load.error":
        "Layerlist ${LIST} keine Ladung erfolgt.  " +
        "Möglicher Grund : nicht ausreichende Zugriffsrechte " +
		"SRS nicht kompatibel oder Layer existiert nicht... ",
    "NB layers imported": "${NB} Layers zufügen",
    "One layer imported": "Layer zugefügt",
    "No layer imported": "kein Layer zugefügt",
    "The provided context is not valid": "WMC ungültig",
    "The default context is not defined (and it is a BIG problem!)":
        "Standard WMC nicht definiert(and it is a BIG problem!)",
    /* GEOR_mappanel.js strings */
    "Coordinates in ": "Koordinaten im ",
    "scale picker": "Maβstab",
    /* GEOR_ows.js strings */
    "The NAME layer was not found in WMS service.":
        "Layer ${NAME} im WMS Servier nicht gefunden.",
    /* GEOR_print.js strings */
    "Sources: ": "Quellen : ",
    "Source: ": "Quelle : ",
    "Projection: PROJ": "Projektion : ${PROJ}",
    "Print error": "Ausdruck unmöglich",
    "Print server returned an error":
        "Druckservice signalisiert Fehler.",
    "Contact platform administrator":
        "Administrator Informieren ",
    "Layer unavailable for printing": "Layer nicht ausdruckbar",
    "The NAME layer cannot be printed.":
        "Layer ${NAME}  kann noch nicht ausgedruckt werden.",
    "Unable to print": "Ausdrucke nicht verfügbar",
    "The print server is currently unreachable":
        "Druckservice zur Zeit nicht zugänglich.",
    "print.unknown.layout":
        "Konfigurationsfehler: DEFAULT_PRINT_LAYOUT " +
        "${LAYOUT} befindet sich nicht in der Druckformatliste",
    "print.unknown.resolution":
        "Konfigurationsfehler: DEFAULT_PRINT_RESOLUTION " +
        "${RESOLUTION} befindet sich nicht in der Druckbildauflösungsliste",
    "Comments": "Kommentare",
    "Scale: ": "Maβstab : ",
    "Date: ": "Datum : ",
    "Minimap": "Minikarte",
    "North": "Norden",
    "Scale": "Maβstab",
    "Date": "Datum",
    "Legend": "Legende",
    "Format": "Format",
    "Resolution": "Auflösung",
    "Print the map": "Karte drucken",
    "Print": "Drucken",
    "Printing...": "Druckvorgang aktiv...",
    "Print current map": "Drucken der aktiven Karte",
    /* GEOR_querier.js strings */
    "Fields of filters with a red mark are mandatory": "Rote Felder sind auszufüllen",
    "Request on NAME": "Sucher auf ${NAME}",
    "WFS GetFeature on filter": "GetFeature WFS auf einem Filter",
    "Search": "Suchen",
    "querier.layer.no.geom":
        "Geometrische Spalte nicht vorhanden." +
        "<br />Sucher nicht verfügbar.",
    "querier.layer.error":
        "Charakteristiken der angeforderten Layer nicht möglich." +
        "<br />Sucher nicht verfügbar.",
    /* GEOR_referentials.js strings */
    "Referential": "Datenbasis",
    "There is no geometry column in the selected referential":
        "Geometrische Spalte nicht auffindbar",
    "Choose a referential": "Wählen Sie die Referenzen",
    /* GEOR_resultspanel.js strings */
    "resultspanel.maxfeature.reached":
        "<span ext:qtip=\  Leistungsfähigeren Navigator benützen " +
        "um Anzahl darstellbarer Objekte zu erhöhen\">" +
        "Max. Anzahl der Objekte erreicht (${NB})</span>",
    "NB results": "${NB} Ergebnisse",
    "One result": "1 Ergebnis",
    "No result": "kein Ergebnis",
    "Clean": "löchen",
    "All": "Alle",
    "None": "Keiner",
    "Invert selection": "Auswahl umkehren",
    /*
    "Actions on the selection or on all results if no row is selected":
        "Aktien an der Auswahl oder an alle Ergebnisse, wenn keine Zeile ausgewählt ist",
    "Store the geometry": 
        "Enregistrer la géométrie",
    "Aggregates the geometries of the selected features and stores it in your browser for later use in the querier": 
        "La géométrie des objets sélectionnés est enregistrée pour un usage ultérieur dans le requêteur",
    "Geometry successfully stored in this browser": 
        "Géométrie enregistrée avec succès sur ce navigateur",
    */
    "Clean all results on the map and in the table": "Löschen " +
        "der aufgezeichneten Ergebnisse auf Karte und Liste",
    "Zoom": "Zoom",
    "Zoom to results extent": "Zoom auf Ergebnis",
    "CSV Export": "CSV exportieren",
    "Export results as CSV": "Exportieren der Gesamtergebnisse aus CSV",
    "<p>No result for this request.</p>": "<p>Kein Ergebnis zu Ihrer Anfrage.</p>",
    /* GEOR_scalecombo.js strings */
    /* GEOR_selectfeature.js strings */
    "<div>Select features activated on NAME layer. Click on the map.</div>":
        "<div>Objektabfragen activiert ${NAME}. " +
        "Klicken Sie auf die Karte.</div>",
    "OpenLayers SelectFeature":"Objektselektion",
    /* GEOR_styler.js strings */
    "Download style": "Stil downladen",
    "You can download your SLD style at ": "SLD auf folgender Adresse verfügbar : ",
    "Thanks!": "Danke !",
    "Saving SLD": "die SLD-Speicherung",
    "Some classes are invalid, verify that all fields are correct": "Des " +
        "Klassen ungültig, überprüfen Sie ob die Felder korrekt sind",
    "Get SLD": "SLD holen",
    "Malformed SLD": "SLD nicht konform.",
    "circle": "Kreis",
    "square": "Viereck",
    "triangle": "Dreieck",
    "star": "Stern",
    "cross": "Kreuz",
    "x": "x",
    "customized...": "personalisiert...",
    "Classification ...<br/>(this operation can take some time)":
        "Klassifikation ...<br/>(Diese Operation kann Zeit benötigen)",
    "Class": "Klass",
    "Untitled": "Ohne Titel",
    "styler.guidelines":
        "Benutzen Sie den Button \"+\" Um eine Klasse zu schaffen und den Button " +
        "\"Analyse\" Um eine Gesamtklasse der thematischen Analyse zu schaffen.</p>", 
    "Analyze": "Analyse",
    "Add a class": "Klasse hinzufügen",
    "Delete the selected class": "Gewählte Klasse löschen",
    "Styler": "Styler",
    "Apply": "Ausführen",
    "Impossible to complete the operation:": "Operation nicht möglich :",
    "no available attribute": "kein Attribut verfügbar.",
    /* GEOR_toolbar.js strings */
    "m": "m",
    "hectares": "Hektare",
    "zoom to global extent of the map": "Maximaler Kartenausschnitt",
    "pan": "Karte verschieben",
    "zoom in": "Vergrössern (Um auf eine Teilansicht zu zoomen, bitte SHIFT drücken und Ansicht zeichnen)",
    "zoom out": "Verkleinern",
    "back to previous zoom": "vorherige Kartenansicht",
    "go to next zoom": "nächste Kartenansicht",
    "Login": "Login",
    "Logout": "Logout",
    "Help": "Hilfe",
    "Query all active layers": "Alle aktivierten Layer abfragen",
    "Show legend": "Legende anzeigen",
    "Leave this page ? You will lose the current cartographic context.":
        "Sie verlassen diese Seite und verlieren die geschaffenen Karten",
    "Online help": "Hilfe",
    "Display the user guide": "Hilfe anzeigen",
    "Contextual help": "Hinweise",
    "Activate or deactivate contextual help bubbles": "Aktivieren der Hinweisblasen",
    /* GEOR_tools.js strings */
    "distance measure": "Distanz messen",
    "area measure": "Oberfläche messen",
    "Measure": "Maß",
    "Tools": "Utensilien",
    "tools": "Utensilien",
    "tool": "Utensil",
    "No tool": "kein Utensil",
    "Manage tools": "Utensilien verwalten",
    "remember the selection": "Erinnern der Selektion",
    "Available tools:": "Utensilien vorhanden :",
    "Click to select or deselect the tool": "Klicken um Utensil abzuwählen",
    "Could not load addon ADDONNAME": "${ADDONNAME} Addon laden unmöglich ",
    /* GEOR_util.js strings */
    "Characters": "Zeichen",
    "Digital": "Digital",
    "Boolean": "Boolesche",
    "Other": "Andere",
    "Confirmation": "Bestätigung",
    "Information": "Information",
    /* GEOR_waiter.js strings */
    /* GEOR_wmc.js strings */
    "The provided file is not a valid OGC context": "OGC entspricht nicht dieser Datei",
    "Warning: trying to restore WMC with a different projection (PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !": "Achtung: die restaurierte Karte wurde mit ${PROJCODE1} abgespeichert, die aktuelle Karte mit ${PROJCODE2}. Unerwartete Probleme können auftreten.",
    /* GEOR_wmcbrowser.js strings */
    "Could not find WMC file": "Gewählte Karte nicht vorhanden",
    "... or a custom context": "... oder einer personalisierten Karte",
    "Replace current map composition with one of these contexts:": "Aktuelle Karte ersetzen durch :",
    "A unique OSM layer": "Ein einziger Layer OpenStreetMap",
    "default viewer context": "Standarteinstellung",
    "(default)": "<br/>(Standardeinstellung)",
    /* GEOR_workspace.js strings */
    "Share this map": "Karte teilen",
    "Mobile viewer": "Kartenviewer für mobile Technologie",
    "Mobile compatible viewer on sdi.georchestra.org": "Kartenviewer für mobile Technologie auf sdi.georchestra.org",
    "Desktop viewer": "Web Kartenviewer",
    "Desktop viewer on sdi.georchestra.org": "Web Kartenviewer auf sdi.georchestra.org",
    "Abstract": "Zusammen-" +
        "fassung",
    "Context saving": "Kontextspeicherung",
    "The file is required.": "Dateiname nötig.",
    "Context restoring": "Kontext restaurieren",
    "<p>Please note that the WMC must be UTF-8 encoded</p>": "<p>der WMC muss UTF8 kodiert sein</p>",
    "Load": "laden",
    "Workspace": "Menü",
    "Save the map context": "die Kartenspeicherung",
    "Load a map context": "Karte laden",
    "Get a permalink": "Permalink bekommen",
    "Permalink": "Permalink",
    "Share your map with this URL: ": "Karte mit folgender Adresse teilen : ",
    "Edit in OSM": "Berarbeitet im OSM",
    "with JOSM": "mit JOSM",
    "JOSM must be started with the remote control option": "Bitte " +
        "vorher JOSM und Distanzkontrolle aktivieren",
    "with Potlatch": "mit Potlatch",
    "Recommended scale is 1:10.000": "Empfohlener Maßstab : 1/10000",
    "with Potlatch2": "mit Potlatch2",
    "with Walking Papers": "mit Walking Papers",
    /* GEOR_edit.js */
    "Req.": "Req.", // requis
    "Required": "Pflicht",
    "Not required": "Nicht Pflicht",
    "Synchronization failed.": "Synchronisationsfehler",
    "Edit activated": "Bearbeitung aktiviert", 
    "Hover the feature you wish to edit, or choose \"new feature\" in the edit menu": "Uberfliegen der zu verändernden Layerobjekte, oder wählen Sie \"neues Objekt\" im Layermenü",
    /* GeoExt.data.CSW.js */
    "no abstract": "Keine Zusammenfassung"
    // no trailing comma
});

GeoExt.Lang.add("fr", {
    "GeoExt.ux.FeatureEditorGrid.prototype": {
        deleteMsgTitle: "Löschung",
        deleteMsg: "löschen bestätigen ?",
        deleteButtonText: "löschen",
        deleteButtonTooltip: "Objekt löschen",
        cancelMsgTitle: "Annulierung",
        cancelMsg: "Objekt verändert. Bestätigen Sie das Löschen der Änderung ?",
        cancelButtonText: "Annulieren",
        cancelButtonTooltip: "Aktuelle Änderungen verwerfen",
        saveButtonText: "speichern",
        saveButtonTooltip: "Änderungen speichern",
        nameHeader: "Attribut",
        valueHeader: "Wert"
    }
});
