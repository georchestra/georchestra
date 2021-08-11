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
 * @requires GeoExt/Lang.js
 */

/*
 * Dutch translation file
 */
OpenLayers.Lang.nl = OpenLayers.Util.extend(OpenLayers.Lang.nl, {
    /* General purpose strings */
    "Yes": "Oui",
    "No": "Nee",
    "OK": "OK",
    "or": "of",
    "Cancel": "Annuleren",
    "Save": "Opslaan",
    "Loading...": "Bezig met laden...",
    "File": "Bestand",
    "Layer": "Laag",
    "layers": "lagen",
    "Description": "Beschrijving",
    "Error": "Error",
    "Server": "Serveur",
    "Close": "Sluiten",
    "labelSeparator": " : ",
    "File submission failed or invalid file": "Bestandsverzending mislukt - bestand kan ongeldig zijn",
    "Type": "Type",
    "Title": "Titel",
    "Actions": "Acties",
    "Incorrect server response.": "Onjuiste serverreactie.",
    "No features found.": "Geen items gevonden.",
    "Reload": "Herladen",
    /* GEOR.js strings */
    "Cities": "Locaties",
    "Recentering on GeoNames cities": "Opnieuw focussen op plaatsen <br /> van de GeoNames-database",
    "Referentials": "Referentials",
    "Recentering on a selection of referential layers": "Opnieuw focussen op een selectie <br /> lagen \" repositories \"",
    "Addresses": "Adressen",
    "Recentering on a given address": "Opnieuw richten op puntadres",
    "Available layers": "Beschikbare lagen",
    "WMS Search": "WMS zoeken",
    "WFS Search": "WFS zoeken",
    "resultspanel.emptytext":
        "<p> Selecteer de querytool" +
		          "of bouw een query op een laag. <br />" +
		          "Objectkenmerken worden in dit frame weergegeven. </p>",
    /* GEOR_ClassificationPanel.js strings */
    "Attribute": "Attribuut",
    "Number of classes": "Aantal klassen",
    "Minimum size": "Minimale grootte",
    "Maximum size": "Maximale grootte",
    "First color": "Eerste kleur",
    "Last color": "Laatste kleur",
    "Palette": "Palet",
    "Auto classification": "Automatische classificatie",
    "Classify": "Classifier",
    "Unique values": "Unieke waarden",
    "Color range": "Kleurbereiken",
    "Proportional symbols": "Proportionele symbolen",
    /* GEOR_FeatureDataModel.js strings */
    "objects": "objets",
    /* GEOR_ajaxglobal.js strings strings */
    "Server did not respond.": "De server heeft niet gereageerd.",
    "Server access denied.": "De server weigerde te reageren.",
    "ajax.badresponse":
		  "De service reageerde, maar de inhoud van de" +
		  "reactie is niet zoals verwacht",
    "Server unavailable.": "De server is tijdelijk niet beschikbaar. Probeer het later opnieuw.",
    "Too much data.": "Te veel gegevens.",
    "Server exception.": "De server heeft een uitzondering geretourneerd.",
    "ajax.defaultexception":
		  "Voor meer informatie kunt u" +
		  "zoek de retourcode op <a href =\"http://" +
		  "en.wikipedia.org/wiki/List_of_HTTP_status_codes\" target = \"_blank\"> " +
		  "deze pagina </a>." +
        "cette page</a>.",
    "An error occured.<br />": "Er is een fout opgetreden. <br />",
    "Warning : browser may freeze": "Waarschuwing: browser kan vastlopen",
    "ajaxglobal.data.too.big": 
		  "De gegevens van de server zijn te veel" +
		  "groot. <br /> De server heeft ${SENT} KO verzonden" +
		  "(de limiet is ${LIMIT} KO) <br /> Wilt u nog steeds doorgaan ?",
    /* GEOR_config.js strings */
    /* GEOR_cswbrowser.js strings */
    "NAME layer": "Laag ${NAME}",
    "Metadata without a name": "Metadata zonder een naam",
    "The request to GeoNetwork 4 aggregations failed": "De Geonetwork 4 aggregations-zoekopdracht is mislukt",
    "Error for the thesaurus": "Fout voor de thesaurus",
    "Missing key to access the thesaurus":
        "Geen sleutel voor toegang tot deze thesaurus",
    "Keywords query failed": "Sleutelwoordenquery mislukt",
    "Thesaurus:": "Thesaurus :",
    "Thesaurus": "Thesaurus",
    "cswbrowser.default.thesaurus.mismatch":
		  "Beheerder: configuratieprobleem -" +
		  "de variabele DEFAULT_THESAURUS_KEY komt overeen met geen enkele" +
		  "waarde geëxporteerd door GeoNetwork",
    /* GEOR_cswquerier.js strings */
    "cswquerier.help.title": "Syntaxis voor geavanceerd zoeken",
    "cswquerier.help.message": 
		  "<ul><li><b>@woord</b> zoekt in de naam van de organisatie.</li><li><b>#woord </b> zoekopdrachten in de metadata-trefwoorden.</li><li><b>?woord</b> verbreedt de zoekopdracht naar alle velden van de metadata.</li></ul>",
    "NAME layer on VALUE": "Laag ${NAME} op ${VALUE}",
    "Show metadata essentials in a window":
        "Toon metagegevensbenodigdheden in een venster",
    "Show metadata sheet in a new browser tab":
        "Toon metagegevensblad in een nieuw browsertabblad",
    "more": "meer",
    "Click to select or deselect the layer":
        "Klik om de laag te selecteren of deselecteren",
    "Open the URL url in a new window":
        "Open de url ${URL} in een nieuw venster",
    "Unreachable server": "Server niet beschikbaar",
    "Catalogue": "Catalog",
    "Find": "Zoeken",
    "in": "in",
    "No linked layer.": "Geen gekoppelde laag.",
    "One layer found.": "Eén laag gevonden.",
    "NB layers found.": "${NB} lagen gevonden.",
    "NB metadata match the query.": "${NB} metadata komen overeen met de zoekopdracht.",
    "A single metadata matches the query.": "Een enkele metadata komt overeen met de zoekopdracht.",
    "Precise your request.": "Specificeer uw verzoek.",
    "No metadata matches the query.":
        "Geen metadata komt overeen met de zoekopdracht.",
    "Limit to map extent": "Beperking tot kaartbereik",
    "Search limited to current map extent.": "Zoeken beperkt tot huidige kaartomvang.",
    /* GEOR_fileupload.js strings */
    "2D only": "Alleen 2D",
    "Local file": "Lokaal bestand",
    "The service is inactive": "De service is inactief",
    "Upload a vector data file.": "Upload een vectorgegevensbestand.",
    "The allowed formats are the following: ": "De toegestane formaten zijn de volgende : ",
    "Use ZIP compression for multifiles formats, such as": "Gebruik ZIP-compressie voor indelingen met meerdere bestanden, zoals",
    "fileupload_error_incompleteSHP": "Onvolledig shapefile-bestand.",
    "fileupload_error_incompleteTAB": "Onvolledig TAB-bestand.",
    "fileupload_error_ioError": "I / O-fout op de server. Contactpersoon voor administratief beheer van de plateforme voor meer informatie.",
    "fileupload_error_multipleFiles": "Het ZIP-archief bevat verschillende gegevensbestanden. Het mag er maar één bevatten.",
    "fileupload_error_outOfMemory": "De server heeft niet langer voldoende geheugen. Contactpersoon voor bestelbus platformbeheer voor meer informatie.",
    "fileupload_error_sizeError": "Het bestand is te groot om te worden verwerkt.",
    "fileupload_error_unsupportedFormat": "Dit gegevensformaat wordt niet ondersteund door de applicatie.",
    "fileupload_error_projectionError": "Er is een fout opgetreden tijdens het lezen van de geografische coördinaten. weet u zeker dat het bestand de projectie-informatie bevat?",
    "server upload error: ERROR":
        "Het uploaden van het bestand is mislukt. ${ERROR}",
    /* GEOR_geonames.js strings */
    "Go to: ": "Ga naar : ",
    /* GEOR_getfeatureinfo.js strings */
    "<div>Searching...</div>": "<div>Zoeken...</div>",
    "<div>No layer selected</div>": "<div>Geen laag geselecteerd</div>",
    "<div>Search on objects active for NAME layer. Click on the map.</div>":
         "<div>Zoeken op objecten die actief zijn voor de laag ${NAME}. " +
         "Klik op de kaart.</div>",
    "WMS GetFeatureInfo at ": "WMS GetFeatureInfo aan ",
    /* GEOR_layerfinder.js strings */
    "metadata": "metadata",
    "Add layers from local files": "Lagen toevoegen door een bestand vanaf uw computer te uploaden",
    "Find layers searching in metadata":
        "Vind lagen door metadata te zoeken",
    "Find layers from keywords": "Lagen zoeken met trefwoorden",
    "Find layers querying OGC services":
        "Lagen zoeken met OGC-services",
    "layerfinder.layer.unavailable":
		  "De laag ${NAME} is niet gevonden in de WMS-service.<br/><br/>" +
		  "Misschien heb je geen recht op toegang" +
		  "of deze laag is niet langer beschikbaar",
    "Layer projection is not compatible":
        "Laagprojectie is niet compatibel.",
    "The NAME layer does not contain a valid geometry column":
        "De laag ${NAME} bevat geen geldige geometriekolom.",
    "Add": "Toevoegen",
    "Add layers from a ...": "Lagen toevoegen vanuit een ...",
    "Malformed URL": "Misvormde URL.",
    "Queryable": "Doorzoekbaar",
    "Opaque": "Ondoorzichtig",
    "OGC server": "OGC server",
    "I'm looking for ...": "Ik ben op zoek naar ...",
    "Service type": "Soort dienst",
    "Choose a server": "Kies een server",
    "... or enter its address": "... of voer zijn adres in",
    "The server is publishing one layer with an incompatible projection":
        "De server publiceert één laag met een incompatibele projectie",
    "The server is publishing NB layers with an incompatible projection":
        "De server publiceert ${NB} lagen met een incompatibele projectie",
    "This server does not support HTTP POST": "Deze server ondersteunt geen HTTP POST",
    "Unreachable server or insufficient rights": 
		  "Ongeldige reactie van" +
		  "server. Mogelijke redenen: onvoldoende rechten," +
		  "onbereikbare server, te veel gegevens, enz.",
    /* GEOR_managelayers.js strings */
    "layergroup": "samengestelde laag",
    "Service": "Service",
    "Protocol": "Protocol",
    "About this layer": "Over deze laag",
    "Set as overlay": "Instellen als overlay",
    "Set as baselayer": "Instellen als basislaag",
    "Tiled mode" : "Tegelmodus",
    "Confirm NAME layer deletion ?":
        "Bevestig verwijdering van laag ${NAME} ?",
    "1:MAXSCALE to 1:MINSCALE": "1:${MAXSCALE} tot 1:${MINSCALE}",
    "Visibility range (indicative):<br />from TEXT":
        "Zichtbereik (indicatief): <br /> van ${TEXT}",
    "Information on objects of this layer":
        "Informatie over objecten van deze laag",
    "default style": "standaard stijl",
    "no styling": "geen stijl",
    "Recenter on the layer": "Focus opnieuw op de laag",
    "Impossible to get layer extent":
        "Onmogelijk om laagomvang te krijgen.",
    "Refresh layer": "Laag vernieuwen",
    "Show metadata": "Toon metadata",
    "Edit symbology": "Bewerk symbologie",
    "Build a query": "Bouw een zoekopdracht",
    "Download layer": "Download laag",
    "Extract data": "Gegevens extraheren",
    "Choose a style": "Kies een stijl",
    "Modify format": "Wijzig het formaat",
    "Delete this layer": "Verwijder deze laag",
    "Push up this layer": "Duw deze laag naar boven",
    "Push down this layer": "Duw deze laag naar beneden",
    "Add layers": "Lagen toevoegen",
    "Remove all layers": "Verwijder alle lagen",
    "Are you sure you want to remove all layers ?": "Wilt u echt alle lagen verwijderen ?",
    "source: ": "source : ",
    "unknown": "onbekend",
    "Draw new point": "Teken een nieuw punt",
    "Draw new line": "Teken een nieuwe lijn",
    "Draw new polygon": "Teken een nieuwe veelhoek",
    "Edition": "Bewerken",
    "Editing": "Bezig met bewerken",
    "Switch on/off edit mode for this layer": "Schakel deze laag naar bewerkingsmodus",
    "No geometry column.": "Geometrische kolom niet gedetecteerd.",
    "Geometry column type (TYPE) is unsupported.": "Geometry column type (${TYPE}) wordt niet ondersteund.",
    "Switching to attributes-only edition.": "Overschakelen naar editie met alleen attributen.",
    /* GEOR_map.js strings */
    "Location map": "Locatiekaart",
    "Warning after loading layer":
        "Waarschuwing na het laden van een laag",
    "The <b>NAME</b> layer could not appear for this reason: ":
		  "De laag <b>${NAME}</b> wordt mogelijk niet weergegeven voor" +
		  "de volgende reden:",
    "Min/max visibility scales are invalid":
        "De min/max zichtbaarheidsschalen zijn ongeldig.",
    "Visibility range does not match map scales":
        "Het zichtbaarheidsbereik komt niet overeen met de schalen van de kaart.",
    "Geografic extent does not match map extent":
        "De geografische omvang komt niet overeen met die van de kaart.",
    /* GEOR_mapinit.js strings */
    "Add layers from WMS services":
        "Lagen toevoegen vanuit WMS-services",
    "Add layers from WFS services":
        "Lagen toevoegen vanuit WFS-services",
    "NB layers not imported": "${NB} lagen niet geïmporteerd",
    "One layer not imported": "Eén laag niet geïmporteerd",
    "mapinit.layers.load.error":
		  "De lagen met de naam ${LIST} konden niet worden geladen." +
		  "Mogelijke redenen: onvoldoende rechten, incompatibele SRS of niet-bestaande laag",
    "NB layers imported": "${NB} geïmporteerde lagen",
    "One layer imported": "Eén laag geïmporteerd",
    "No layer imported": "Geen laag geïmporteerd",
    "The provided context is not valid": "De verstrekte context is niet geldig",
    "The default context is not defined (and it is a BIG problem!)":
		  "De standaardcontext is niet gedefinieerd" +
		  "(en het is helemaal niet normaal!)",
    "Error while loading file": "Fout bij laden van bestand",
    /* GEOR_mappanel.js strings */
    "Coordinates in ": "Coördinaten in ",
    "coordinates.short.x": "X",
    "coordinates.short.y": "Y",
    "coordinates.short.longitude": "Len",
    "coordinates.short.latitude": "Bre",
    "scale picker": "schaal",
    /* GEOR_ows.js strings */
    "The NAME layer was not found in WMS service.":
        "De laag ${NAME} is niet gevonden in de WMS-service.",
    "Problem restoring a context saved with buggy Chrome 36 or 37":
        "We kunnen een context die is opgeslagen met Chrome 36 of 37 niet herstellen",
    /* GEOR_print.js strings */
    "Sources: ": "Bronnen : ",
    "Source: ": "Bron : ",
    "Projection: PROJ": "Projectie : ${PROJ}",
    "Print error": "Afdrukken onmogelijk",
    "Print server returned an error":
        "Print server heeft een fout geretourneerd.",
    "Contact platform administrator":
        "Neem contact op met de platformbeheerder.",
    "Layer unavailable for printing": "Laag niet beschikbaar voor afdrukken",
    "The NAME layer cannot be printed.":
        "De laag ${NAME} kan nog niet worden afgedrukt.",
    "Unable to print": "Afdrukken niet beschikbaar",
    "The print server is currently unreachable":
        "De afdrukservice is momenteel niet beschikbaar.",
    "print.unknown.layout":
		   "Configuratiefout: DEFAULT_PRINT_LAYOUT" +
		   "${LAYOUT} staat niet in de lijst met afdrukformaten",
    "print.unknown.resolution":
		  "Configuratiefout: DEFAULT_PRINT_RESOLUTION" +
		  "${RESOLUTION} staat niet in de lijst met afdrukresoluties",
    "print.unknown.format":
		  "Configuratiefout: formaat" +
		  "${FORMAT} wordt niet ondersteund door de afdrukservice",
    "Pick an output format": "Kies een uitvoerformaat",
    "Comments": "Opmerkingen",
    "Scale: ": "Schaal: ",
    "Date: ": "Datum: ",
    "Minimap": "Mini-kaart",
    "North": "Noorden",
    "Scale": "Schaal",
    "Date": "Datum",
    "Legend": "Legende",
    "Format": "Formaat",
    "Resolution": "Resolutie",
    "Print the map": "Kaart afdrukken",
    "Print": "Afdrukken",
    "Printing...": "Bezig met afdrukken...",
    "Print current map": "Huidige kaart afdrukken",
    /* GEOR_Querier.js strings */
    "Fields of filters with a red mark are mandatory": 
		  "Velden met filters met een rode markering zijn verplicht",
    "Request on NAME": "Aanvraag op ${NAME}",
    "WFS GetFeature on filter": "WFS GetFeature op filter",
    "Search": "Zoeken",
    "querier.layer.no.geom":
		  "De laag heeft geen geometrische kolom." +
		  "<br />De geometrische aanvrager zal niet functioneel zijn.",
    "querier.layer.error":
		  "Kan de kenmerken van de gevraagde laag niet verkrijgen." +
		  "<br /> De aanvrager is niet beschikbaar.",
    /* GEOR_referentials.js strings */
    "Referential": "Referentie",
    "There is no geometry column in the selected referential":
		  "Er is geen geometriekolom in de geselecteerde referentie",
    "Choose a referential": "Kies een referentie",
    /* GEOR_resultspanel.js strings */
    "Symbology": "Symbologie",
    "Edit this panel's features symbology": "Bewerk de symbologie van de selectie",
    "Reset": "Reset",
    "Export is not possible: features have no geometry": "Exporteren is niet mogelijk: elementen hebben geen geometrie",
    "resultspanel.maxfeature.reached":
		  "<span ext:qtip=\" Gebruik een betere browser " +
		  "om het aantal weer te geven objecten te vergroten\">"+
		  "Maximaal aantal objecten bereikt (${NB})</span>",
    "NB results": "${NB} resultaten",
    "One result": "1 resultat",
    "No result": "Geen resultat",
    "Clean": "Uitwissen",
    "All": "Alle",
    "None": "Geen",
    "Invert selection": "Selectie omkeren",
    "Actions on the selection or on all results if no row is selected":
        "Acties op de selectie of op alle resultaten als er geen is geselecteerd",
    "Store the geometry":
        "Bewaar de geometrie",
    "Aggregates the geometries of the selected features and stores it in your browser for later use in the querier":
        "De geometrie van de geselecteerde objecten wordt opgeslagen voor later gebruik in de aanvrager",
    "Geometry successfully stored in this browser":
        "Geometrie succesvol opgeslagen in deze browser",
    "Clean all results on the map and in the table": 
		  "Verwijderen" +
		  "resultaten weergegeven op de kaart en in de tabel",
    "Zoom": "Zoom",
    "Zoom to results extent": "Kader het bereik van de kaart daarop in",
    "Export": "Exporteren",
    "Export results as": "Resultaten exporteren als",
    "<p>No result for this request.</p>": 
		 "<p>Geen object" +
		 "komt overeen met uw verzoek.</p>",
    /* GEOR_scalecombo.js strings */
    /* GEOR_selectfeature.js strings */
    "<div>Select features activated on NAME layer. Click on the map.</div>":
		  "<div>Selectie van objecten ingeschakeld op de laag ${NAME}." +
		  "Klik op de kaart.</div>",
    "OpenLayers SelectFeature":"Selectie van objecten",
    /* GEOR_styler.js strings */
    "Download style": "Download stijl",
    "You can download your SLD style at ": 
		  "Uw SLD is beschikbaar op" +
		  "het volgende adres:",
    "Thanks!": "Bedankt!",
    "Saving SLD": "SLD opslaan",
    "Some classes are invalid, verify that all fields are correct": 
		  "Klassen zijn niet geldig, controleer of de velden correct zijn",
    "Get SLD": "SLD herstel",
    "Malformed SLD": "De SLD voldoet niet.",
    "circle": "cirkel",
    "square": "vierkant",
    "triangle": "driehoek",
    "star": "ster",
    "cross": "kruis",
    "x": "x",
    "customized...": "gepersonaliseerde...",
    "Classification ...<br/>(this operation can take some time)":
		  "Classificatie ... <br/>(deze bewerking kan enige tijd duren)",
    "Class": "Klasse",
    "Untitled": "Zonder titel",
    "styler.guidelines":
		   "Gebruik de \"+\" knop om een klasse te maken en de" +
		   "\"Analyse\" om een set klassen te maken gedefinieerd door een" +
		   "thematische analyse.</p>",
    "Analyze": "Analyse",
    "Add a class": "Voeg een klas toe",
    "Delete the selected class": "Verwijder de geselecteerde klasse",
    "Styler": "Stijler",
    "Apply": "Toepassen",
    "Impossible to complete the operation:": "Onmogelijk om de bewerking te voltooien :",
    "no available attribute": "geen attributen beschikbaar.",
    /* GEOR_toolbar.js strings */
    "m": "m",
    "hectares": "ha",
    "zoom to global extent of the map": 
		  "zoom naar globale omvang van de kaart",
    "pan": "sleep de kaart",
    "zoom in": "inzoomen (om voorwaarts in te zoomen: druk op SHIFT + teken de voorrang)",
    "zoom out": "uitzoomen",
    "back to previous zoom": "terug naar vorige zoom",
    "go to next zoom": "ga naar volgende zoom",
    "Login": "Log in",
    "Logout": "Uitloggen",
    "Help": "Hulp",
    "Query all active layers": "Alle actieve lagen opvragen",
    "Show legend": "Legende tonen",
    "Leave this page ? You will lose the current cartographic context.":
        "Deze pagina verlaten? U zal de huidige cartografische context verliezen.",
    "Online help": "Online hulp",
    "Display the user guide": "Bekijk gebruikershandleiding",
    "Contextual help": "Contextuele hulp",
    "Activate or deactivate contextual help bubbles": "Actuele hulpbellen activeren of deactiveren",
    /* GEOR_tools.js strings */
    "Tools": "Gereedschap",
    "tools": "gereedschap",
    "tool": "gereedschap",
    "No tool": "geen gereedschap",
    "Manage tools": "Beheer gereedschap",
    "remember the selection": "onthoud de selectie",
    "Available tools:": "Beschikbare gereedschap :",
    "Click to select or deselect the tool": "Klik om het gereedschap te (de)selecteren",
    "Could not load addon ADDONNAME": "Kan add-on ${ADDONNAME} niet laden",
    "New tools are now available": "Nieuwe gereedschappen zijn beschikbaar",
    /* GEOR_util.js strings */
    "Characters": "Karakters",
    "Digital": "Digital",
    "Boolean": "Boolean",
    "Other": "Anders",
    "Confirmation": "Bevestiging",
    "Information": "Informatie",
    "pointOfContact": "contact",
    "custodian": "producent",
    "distributor": "verdeler",
    "originator": "aanstichter",
    "More": "Meer",
    "Could not parse metadata.": "Kon metadata niet ontleden",
    "Could not get metadata.": "Kon geen metadata krijgen",
    /* GEOR_waiter.js strings */
    /* GEOR_wmc.js strings */
    "The provided file is not a valid OGC context": "Het verstrekte bestand is geen geldige OGC-context",
    "Warning: trying to restore WMC with a different projection (PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !": 
		  "Waarschuwing: de herstelde context is opgeslagen in ${PROJCODE1} terwijl de huidige kaart in ${PROJCODE2} is. Er kan onverwacht gedrag zijn.",
    /* GEOR_wmcbrowser.js strings */
    "all contexts": "alle kaarten",
    "Could not find WMC file": "Kon WMC-bestand niet vinden",
    "... or a local context": "... of een lokale kaart",
    "Load or add the layers from one of these map contexts:": "Laad of voeg de lagen van een van deze kaarten toe :",
    "A unique OSM layer": "Een enkele OpenStreetMap-laag",
    "default viewer context": "standaard kaart",
    "(default)": "<br/>(standaard kaart)",
    /* GEOR_workspace.js strings */
    "Created:": "Aanmaakdatum : ",
    "Last accessed:": "Datum van laatste toegang : ",
    "Access count:": "Aantal toegangen : ",
    "Permalink:": "Permalink : ",
    "My contexts": "Mijn kaarten",
    "Created": "Schepping",
    "Accessed": "Geraadpleegde",
    "Count": "Aantal toegangen",
    "View": "Tonen",
    "View the selected context": "Bekijk de geselecteerde kaart (let op: vervangt de huidige kaart)",
    "Download": "Downloaden",
    "Download the selected context": "Download de geselecteerde kaart",
    "Delete": "Verwijderen",
    "Delete the selected context": "Verwijder de geselecteerde kaart",
    "Failed to delete context": "Kan kaart niet verwijderen",
    "Manage my contexts": "Beheer mijn kaarten",
    "Keywords": "Zoekwoorden",
    "comma separated keywords": "door komma's gescheiden zoekwoorden",
    "Save to metadata": "Maak metadata",
    "in group": "in de groep",
    "The context title is mandatory": "De titel van de kaart is verplicht",
    "There was an error creating the metadata.": "Het maken van de metadata is mislukt.",
    "Share this map": "Deel deze kaart",
    "Mobile viewer": "Mobiele viewer",
    "Mobile compatible viewer": "Compatibele mobiele viewer",
    "Desktop viewer": "Web wiewer",
    "Desktop viewer": "Web viewer",
    "Abstract": "Samenvatting",
    "Context saving": "De kaart opslaan",
    "The file is required.": "Een bestandsnaam is vereist.",
    "Context restoring": "Kaart herstellen",
    "<p>Please note that the WMC must be UTF-8 encoded</p>": 
		  "<p>Merk op dat de " +
		  "contextbestand moet gecodeerd zijn in UTF-8.</p>",
    "Load": "Laden",
    "Workspace": "Omgeving",
    "Save the map context": "De kaart opslaan",
    "Load a map context": "Een kaart laden",
    "Get a permalink": "Krijg een permalink",
    "Permalink": "Permalink",
    "Share your map with this URL: ": "Deel de kaart met het volgende adres : ",
    /* GEOR_edit.js */
    "Req.": "Vereist", // requis
    "Required": "Vereist",
    "Not required": "Niet vereist",
    "Synchronization failed.": "Synchronisatie mislukt.",
    "Edit activated": "Bewerken geactiveerd",
    "Hover the feature you wish to edit, or choose \"new feature\" in the edit menu": 
		  "Beweeg over de objecten in de laag die u wilt wijzigen of kies \"nieuw object \" in het bewerkingsmenu van de laag",
    /* GeoExt.data.CSW.js */
    "no abstract": "geen samenvatting"
    // no trailing comma
});

GeoExt.Lang.add("nl", {
    "GeoExt.ux.FeatureEditorGrid.prototype": {
        deleteMsgTitle: "Verwijderen",
        deleteMsg: "Bevestig de verwijdering van dit vectorobject ?",
        deleteButtonText: "Verwijderen",
        deleteButtonTooltip: "Verwijder dit object",
        cancelMsgTitle: "Annuleren",
        cancelMsg: "Het object is lokaal aangepast. Bevestigen dat de wijzigingen zijn verlaten ?",
        cancelButtonText: "Annuleren",
        cancelButtonTooltip: "Annuleer de lopende wijzigingen",
        saveButtonText: "Opslaan",
        saveButtonTooltip: "Wijzigingen opslaan",
        nameHeader: "Attribuut",
        valueHeader: "Waarde"
    }
});
