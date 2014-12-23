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
 * Spanish translation file
 */
OpenLayers.Lang.es = OpenLayers.Util.extend(OpenLayers.Lang.es, {
    /* General purpose strings */
    "Yes": "Si",
    "No": "No",
    "OK": "OK",
    "or": "o",
    "Cancel": "Cancelar",
    "Save": "Guardar",
    "Loading...": "Cargando...",
    "File": "Archivo",
    "Layer": "Capa",
    "layers": "capas",
    "Description": "Descripción",
    "Error": "Error",
    "Server": "Servidor",
    "Close": "Cerrar",
    "labelSeparator": " : ",
    "File submission failed or invalid file": "El envío del archivo falló - " +
        "verificar si el archivo es valido",
    "Type": "Tipo",
    "Title": "Título",
    "Actions": "Acciones",
    "Incorrect server response.": "Respuesta incorrecta del servidor.",
    "No features found.": "No se encontró ningún objeto geográfico.",
    /* GEOR.js strings */
    "Cities": "Ciudades",
    "Recentering on GeoNames cities":
        "Posicionarse sobre una ciudad<br />(base GeoNames)",
    "Referentials": "Unidades administrativas",
    "Recentering on a selection of referential layers":
        "Posicionarse sobre una unidad administrativa",
    "Addresses": "Direcciones",
    "Recentering on a given address": "Posicionarse sobre una dirección",
    "Available layers": "Capas disponibles",
    "WMS Search": "WMS Buscar",
    "WFS Search": "WFS Buscar",
    "resultspanel.emptytext":
        "<p>Seleccione la herramienta de interrogación " +
        "o construya una consulta sobre una capa.<br />" +
        "Los atributos de los objetos se mostrarán en este espacio.</p>",
    /* GEOR_ClassificationPanel.js strings */
    "Attribute": "Atributo",
    "Number of classes": "Número de clases",
    "Minimum size": "Tamaño mínimo",
    "Maximum size": "Tamaño máximo",
    "First color": "Primer color",
    "Last color": "Último color",
    "Palette": "Paleta",
    "Auto classification": "Clasificación automática",
    "Classify": "Clasificar",
    "Unique values": "Valores únicos",
    "Color range": "Rangos de colores",
    "Proportional symbols": "Símbolos proporcionales",
    /* GEOR_FeatureDataModel.js strings */
    "objects": "objetos",
    /* GEOR_address.js strings */
    "Go to: ": "Ir a: ",
    "searching...": "buscando...",
    "adressSearchExemple": "por ejemplo: 2, Ciudadela Ferroviaria, La Paz",
    /* GEOR_ajaxglobal.js strings strings */
    "Server did not respond.": "El servidor no respondió.",
    "Server access denied.": "El servidor denegó el acceso.",
    "ajax.badresponse":
        "El server respondió, pero el contenido de la respuesta no es " +
        "conforme a la respuesta esperada",
    "Server unavailable.": "El servidor está temporalmente fuera de servicio" +
        "Por favor vuelva a intentar más tarde.",
    "Too much data.": "Datos demasiado pesados.",
    "Server exception.": "El servidor devolvió una excepción.",
    "ajax.defaultexception":
        "Para más información, busque el código de error en <a href=\"http://" +
        "en.wikipedia.org/wiki/List_of_HTTP_status_codes\" target=\"_blank\">" +
        "wikipedia</a>.",
    "An error occured.<br />": "Ocurrió un error.<br />",
    "Warning : browser may freeze": "OjO: el navegador se podría bloquear",
    "ajaxglobal.data.too.big": "Los datos que provienen del servidor son " +
        "demasiado pesados.<br />El servidor envió ${SENT}KBytes " +
        "(el límite es ${LIMIT}KBytes)<br />¿Desea continuar?",
    /* GEOR_config.js strings */
    /* GEOR_cswbrowser.js strings */
    "NAME layer": "Capa ${NAME}",
    "Metadata without a name": "Metadata sin nombre",
    "The getDomain CSW query failed": "La consulta CSW getDomain falló",
    "Error for the thesaurus": "Error para el tesauro",
    "Missing key to access the thesaurus":
        "No se encuentra la clave para acceder a este tesauro",
    "Keywords query failed": "La consulta de las palabras clave falló",
    "Thesaurus:": "Tesauro:",
    "Thesaurus": "Tesauro",
    "cswbrowser.default.thesaurus.mismatch":
        "Administrador: problema de configuración - " +
        "la variable DEFAULT_THESAURUS_KEY no corresponde a ningún " +
        "valor exportado por GeoNetwork",
    /* GEOR_cswquerier.js strings */
    "cswquerier.help.title": "Ayuda para consulta avanzada",
    "cswquerier.help.message": '<ul><li><b>@carretera</b> busca "carretera" en el nombre de la organización.</li><li><b>#carretera</b> busca "carretera" en las palabras-clave de los metadatos.</li><li><b>?carretera</b> abre la búsqueda de "carretera" a todos los campos de los metadatos.</li></ul>',
    "NAME layer on VALUE": "Capa ${NAME} sobre ${VALUE}",
    "Show metadata sheet in a new window":
        "Mostrar la ficha de metadatos en una nueva ventana",
    "more": "más",
    "Click to select or deselect the layer":
        "Haga clic para seleccionar o deseleccionar la capa",
    "Open the URL url in a new window":
        "Abrir la URL ${URL} en una nueva ventana",
    "Unreachable server": "El servidor no está disponible",
    "Catalogue": "Catálogo",
    "Find": "Buscar",
    "in": "en",
    "No linked layer.": "Ninguna capa.",
    "One layer found.": "1 capa fundar.",
    "NB layers found.": "${NB} capas fundar.",
    "NB metadata match the query.": "${NB} metadatos coincida con la consulta.",
    "A single metadata matches the query.": "Un solo metadato coincida con la consulta.",
    "Precise your request.": "Precise su consulta.",
    "No metadata matches the query.":
        "Ningún metadato coincida con la consulta.",
    /* GEOR_fileupload.js strings */
    "2D only": "2D",
    "Local file": "Archivo local",
    "The service is inactive": "El servicio esta deshabilitado",
    "Upload a vector data file.": "Subir un archivo de datos vectoriales.",
    "The allowed formats are the following: ":
        "Los formatos aceptados son los siguientes: ",
    "Use ZIP compression for multifiles formats, such as":
        "Usar compresión ZIP para los formatos multiarchivos, como",
    "fileupload_error_incompleteMIF": "Archivo MIF/MID incompleto.",
    "fileupload_error_incompleteSHP": "Archivo shapefile incompleto.",
    "fileupload_error_incompleteTAB": "Archivo TAB incompleto.",
    "fileupload_error_ioError": "Error de I/O en el servidor. Contactar el administrador de la plataforma para más detalles.",
    "fileupload_error_multipleFiles": "El ZIP contiene varios archivos de datos. Solo tiene que contener uno.",
    "fileupload_error_outOfMemory": "El servidor ya no tiene memoria disponible. Contactar el administrador de la plataforma para más detalles.",
    "fileupload_error_sizeError": "El tamaño del archivo es demasiado grande.",
    "fileupload_error_unsupportedFormat": "Este formato no esta soportado.",
    "fileupload_error_projectionError": "Error al leer las coordenadas geográficas.",
    "server upload error: ERROR":
        "El archivo local no pudó ser subido. ${ERROR}",
    /* GEOR_geonames.js strings */
    /* GEOR_getfeatureinfo.js strings */
    "<div>Searching...</div>": "<div>Buscando...</div>",
    "<div>No layer selected</div>": "<div>No capa seleccionada</div>",
    "<div>Search on objects active for NAME layer. Click on the map.</div>":
         "<div>Búsqueda de objetos activada sobre la capa ${NAME}. " +
         "Haga clic sobre el mapa.</div>",
    "WMS GetFeatureInfo at ": "GetFeatureInfo WMS sobre ",
    /* GEOR_layerfinder.js strings */
    "metadata": "metadatos",
    "Add layers from local files": "Añadir capas a partir de archivos locales",
    "Find layers searching in metadata":
        "Encontrar capas buscando en los metadatos",
    "Find layers from keywords": "Encontrar capas desde palabras clave",
    "Find layers querying OGC services":
        "Encontrar capas interrogando servicios OGC",
    "layerfinder.layer.unavailable":
        "La capa ${NAME} no pudo ser encontrada en el servicio WMS.<br/<br/>" +
        "Tiene el derecho de acceder a esta capa ? " +
        "Quizás la capa no está disponible actualmente",
    "Layer projection is not compatible":
        "La proyección de la capa no es compatible.",
    "The NAME layer does not contain a valid geometry column":
        "La capa ${NAME} no contiene una columna de geometría válida.",
    "Add": "Añadir",
    "Add layers from a ...": "Añadir capas desde un ...",
    "Malformed URL": "URL mal formada.",
    "Queryable": "Interrogable",
    "Opaque": "Opaco",
    "OGC server": "OGC servicio",
    "I'm looking for ...": "Estoy buscando ...",
    "Service type": "Tipo de servicio",
    "Choose a server": "Elija un servicio",
    "... or enter its address": "... o llene su dirección",
    "The server is publishing one layer with an incompatible projection":
        "El servicio está publicando una capa cuya proyección no es compatible",
    "The server is publishing NB layers with an incompatible projection":
        "El servicio esta publicando ${NB} capas cuya proyección no es " +
        "compatible",
    "This server does not support HTTP POST": "Este servicio no admite HTTP POST",
    "Unreachable server or insufficient rights": "Respuesta del servidor " +
        "invalida. Razones posibles: datos demasiado pesados, derechos insuficientes, " +
        "servidor inalcanzable, etc.",
    /* GEOR_managelayers.js strings */
    "Service": "Service",
    "Protocol": "Servicio",
    "About this layer": "Sobre esta capa",
    "Set as overlay": "Pasar en capa de superposición",
    "Set as baselayer": "Pasar en capa de fondo",
    "Confirm NAME layer deletion ?":
        "¿Confirma que se suprimirá la capa ${NAME}?",
    "1:MAXSCALE to 1:MINSCALE": "1:${MAXSCALE} a 1:${MINSCALE}",
    "Visibility range (indicative):<br />from TEXT":
        "Rango de visibilidad (indicativo):<br />de ${TEXT}",
    "Information on objects of this layer":
        "Información sobre los objetos de esta capa",
    "default style": "estilo por omisión",
    "no styling": "no estilo",
    "Recenter on the layer": "Ajustar vista sobre la capa",
    "Impossible to get layer extent":
        "No se puede obtener la extensión de la capa.",
    "Refresh layer": "Recargar la capa",
    "Show metadata": "Mostrar los metadatos",
    "Edit symbology": "Editar la simbología",
    "Build a query": "Construir una consulta",
    "Download data": "Descargar los datos",
    "Choose a style": "Elegir un estilo",
    "Modify format": "Modificar el formato",
    "Delete this layer": "Quitar esta capa",
    "Push up this layer": "Subir esta capa",
    "Push down this layer": "Bajar esta capa",
    "Add layers": "Añadir capas",
    "Remove all layers": "Retirar todas las capas",
    "Are you sure you want to remove all layers ?": "¿Seguro que desea retirar todas las capas?",
    "source: ": "fuente: ",
    "unknown": "desconocido",
    "Draw new point": "Dibujar un nuevo punto",
    "Draw new line": "Dibujar una nueva línea",
    "Draw new polygon": "Dibujar un nuevo polígono",
    "Edition": "Edición",
    "Editing": "Edición en curso",
    "Switch on/off edit mode for this layer": "Activar/desactivar el modo de edición para esta capa",
    "No geometry column.": "Ninguna columna geométrica encontrada.",
    "Geometry column type (TYPE) is unsupported.": "El tipo de la columna geométrica (${TYPE}) no esta soportado.",
    "Switching to attributes-only edition.": "Solo se podrá editar los atributos de los objetos existentes.",
    /* GEOR_map.js strings */
    "Location map": "Mapa de ubicación",
    "Warning after loading layer":
        "Advertencia después de cargar la capa",
    "The <b>NAME</b> layer could not appear for this reason: ":
        "La capa <b>${NAME}</b> podría no aparecer por la siguiente razón: ",
    "Min/max visibility scales are invalid":
        "Las escalas min/max de visibilidad son invalidas.",
    "Visibility range does not match map scales":
        "El rango de visibilidad no corresponde a las escalas del mapa.",
    "Geografic extent does not match map extent":
        "La extensión geográfica no corresponde a la extensión del mapa",
    /* GEOR_mapinit.js strings */
    "Add layers from WMS services": "Añadir capas desde servicios WMS",
    "Add layers from WFS services": "Añadir capas desde servicios WFS",
    "NB layers not imported": "${NB} capas no importadas",
    "One layer not imported": "Una capa no importada",
    "mapinit.layers.load.error":
        "Las capas siguientes: ${LIST}, no se lograron cargar: " +
        "SRS incompatible o la capa no existe",
    "NB layers imported": "${NB} capas importadas",
    "One layer imported": "Una capa importada",
    "No layer imported": "Ninguna capa importada",
    "The provided context is not valid": "El contexto proveído no es valido",
    "The default context is not defined (and it is a BIG problem!)":
        "El contexto por omisión no está definido ",
    "Error while loading file": "Error al cargar el archivo",
    /* GEOR_mappanel.js strings */
    "Coordinates in ": "Coordenadas en ",
    "scale picker": "Escala",
    /* GEOR_ows.js strings */
    "The NAME layer was not found in WMS service.":
        "La capa ${NAME} no se encuentra en el servicio WMS",
    "Problem restoring a context saved with buggy Chrome 36 or 37":
        "No se puede restaurar un contexto cartográfico guardado con Chrome 36 o 37",
    /* GEOR_print.js strings */
    "Sources: ": "Fuentes: ",
    "Source: ": "Fuente: ",
    "Projection: PROJ": "Proyección: ${PROJ}",
    "Print error": "Error de impresión",
    "Print server returned an error":
        "El servicio de impresión señaló un error.",
    "Contact platform administrator":
        "Contacte el administrador de la plataforma.",
    "Layer unavailable for printing": "La capa no está disponible para " +
        "imprimir",
    "The NAME layer cannot be printed.": "La capa ${NAME} no puede imprimirse.",
    "Unable to print": "La impresión no está disponible",
    "The print server is currently unreachable":
        "El servicio de impresión está actualmente inaccesible.",
    "print.unknown.layout":
         "Error de configuración: DEFAULT_PRINT_LAYOUT " +
         "${LAYOUT} no se encuentra en los formatos de impresión",
    "print.unknown.resolution":
         "Error de configuración: DEFAULT_PRINT_RESOLUTION " +
         "${RESOLUTION} no se encuentra en las resoluciones de impresión",
    "print.unknown.format":
        "Error de configuración: " +
        "${FORMAT} no se encuentra en los formatos de impresión",
    "Pick an output format": "Seleccione un formato de salida",
    "Comments": "Comentarios",
    "Scale: ": "Escala: ",
    "Date: ": "Fecha: ",
    "Minimap": "Mini-mapa",
    "North": "Norte",
    "Scale": "Escala",
    "Date": "Fecha",
    "Legend": "Leyenda",
    "Format": "Formato",
    "Resolution": "Resolución",
    "Print the map": "Impresión del mapa",
    "Print": "Imprimir",
    "Printing...": "Imprimiendo...",
    "Print current map": "Imprimir el mapa actual",
    /* GEOR_querier.js strings */
    "Fields of filters with a red mark are mandatory": "Tiene que llenar " +
        "los campos de filtros con rojo.",
    "Request on NAME": "Consultas sobre ${NAME}",
    "WFS GetFeature on filter": "GetFeature WFS sobre un filtro",
    "Search": "Búsqueda",
    "querier.layer.no.geom":
        "La capa no contiene ninguna columna geométrica." +
        "<br />El módulo de consultas geométricas no funcionará.",
    "querier.layer.error":
        "No se pueden obtener las características de la capa." +
        "<br />El módulo de consultas no está disponible.",
    /* GEOR_referentials.js strings */
    "Referential": "Unidad administrativa",
    "There is no geometry column in the selected referential":
        "La unidad administrativa seleccionada no contiene ningúna columna " +
        "geométrica",
    "Choose a referential": "Elija una unidad administrativa",
    /* GEOR_resultspanel.js strings */
    "resultspanel.maxfeature.reached":
        " <span ext:qtip=\"Utilice un navegador más potente " +
        "para aumentar el número de objetos visibles\">" +
        "Número de objetos máximo alcanzado (${NB})</span>",
    "NB results": "${NB} resultados",
    "One result": "1 resultado",
    "No result": "Ningún resultado",
    "Clean": "Quitar",
    "All": "Todos",
    "None": "Ninguno",
    "Invert selection": "Invertir la selección",
    "Actions on the selection or on all results if no row is selected":
        "Acciones en la selección o en todos los resultados, si no se selecciona ninguna fila",
    "Store the geometry": 
        "Guardar la geometría",
    "Aggregates the geometries of the selected features and stores it in your browser for later use in the querier": 
        "La geometría de los objetos seleccionados se almacenan en el navegador para su uso posterior en consultas",
    "Geometry successfully stored in this browser": 
        "La geometría fue registrada correctamente en el navegador",
    "Clean all results on the map and in the table": "Quitar los resultados" +
        "del mapa y en la tabla",
    "Zoom": "Zoom",
    "Zoom to results extent": "Zoom sobre la extensión de los resultados",
    "CSV Export": "Exportar en CSV",
    "Export results as CSV": "Exportar todos los resultados en CSV",
    "<p>No result for this request.</p>": "<p>Ningún objeto " +
        "corresponde a su consulta.</p>",
    /* GEOR_scalecombo.js strings */
    /* GEOR_selectfeature.js strings */
    "<div>Select features activated on NAME layer. Click on the map.</div>":
         "<div>Seleccion de objetos activada sobre la capa ${NAME}. " +
         "Haga clic sobre el mapa.</div>",
    "OpenLayers SelectFeature":"Selección de objetos",
    /* GEOR_styler.js strings */
    "Download style": "Descargar el estilo",
    "You can download your SLD style at ": "Su estilo SLD está disponible " +
        "a la dirección siguiente: ",
    "Thanks!": "¡Gracias!",
    "Saving SLD": "Guardando el SLD",
    "Some classes are invalid, verify that all fields are correct":
        "Algunas clases no son validas, verifique que los campos son correctos",
    "Get SLD": "Recuperando el SLD",
    "Malformed SLD": "El SLD está mal formado.",
    "circle": "círculo",
    "square": "cuadrado",
    "triangle": "triángulo",
    "star": "asterisco",
    "cross": "cruz",
    "x": "x",
    "customized...": "personalizado...",
    "Classification ...<br/>(this operation can take some time)":
        "Clasificación...<br/>(esta operación puede tomar tiempo)",
    "Class": "Clase",
    "Untitled": "Sin título",
    "styler.guidelines":
        "Utilizar el botón \"+\" para crear una clase, y el botón " +
        "\"Análisis\" para crear un conjunto de clases definidas por una " +
        "análisis temática.</p>",
    "Analyze": "Análisis",
    "Add a class": "Añadir una clase",
    "Delete the selected class": "Quitar la clase seleccionada",
    "Styler": "Estilizador",
    "Apply": "Aplicar",
    "Impossible to complete the operation:":
        "Imposible completar la operación:",
    "no available attribute": "ningún atributo disponible.",
    /* GEOR_toolbar.js strings */
    "m": "m",
    "hectares": "hectáreas",
    "zoom to global extent of the map": "Zoom sobre la extensión global " +
        "del mapa",
    "pan": "Agarrar - desplazar el mapa",
    "zoom in": "Acercamiento",
    "zoom out": "Alejamiento",
    "back to previous zoom": "Volver al zoom anterior",
    "go to next zoom": "Ir al zoom siguiente",
    "Login": "Conexión",
    "Logout": "Desconexión",
    "Help": "Ayuda",
    "Query all active layers": "Consultar todas las activas capas",
    "Extractor help": "Ayuda del extractor",
    "Show legend": "Mostrar la leyenda",
    "Leave this page ? You will lose the current cartographic context.":
        "Usted desea salir de esta página ? Se perderá el contexto cartográfico actual.",
    "Online help": "Ayuda en línea",
    "Display the user guide": "Mostrar la guía de usuario",
    "Contextual help": "Ayuda contextual",
    "Activate or deactivate contextual help bubbles": "Activar o desactivar las ventanas de ayuda contextual",
    /* GEOR_tools.js strings */
    "distance measure": "Medir una distancia",
    "area measure": "Medir un área",
    "Measure": "Medida",
    "Tools": "Herramientas",
    "tools": "herramientas",
    "tool": "herramienta",
    "No tool": "ninguna herramienta",
    "Manage tools": "Manejar las herramientas",
    "remember the selection": "acordarse de la selección",
    "Available tools:": "Herramientas disponibles:",
    "Click to select or deselect the tool": "Haga clic para (de)seleccionar la herramienta",
    "Could not load addon ADDONNAME": "No se pudo cargar el addon ${ADDONNAME}",
    "Your new tools are now available in the tools menu.": 'Sus nuevos herramientas ya están disponibles en el menú "herramientas"',
    /* GEOR_util.js strings */
    "Characters": "Caracteres",
    "Digital": "Digital",
    "Boolean": "Boolean",
    "Other": "Otro",
    "Confirmation": "Confirmación",
    "Information": "Información",
    /* GEOR_waiter.js strings */
    /* GEOR_wmc.js strings */
    "The provided file is not a valid OGC context":
		"El archivo proveido no es un contexto OGC valido",
    "Warning: trying to restore WMC with a different projection (PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !": "Advertencia: para cargar el contexto WMC, se necesita reproyectar desde ${PROJCODE1} hacía la proyección del mapa ${PROJCODE2}. ¡El resultado puede ser diferente de lo esperado!",
    /* GEOR_wmcbrowser.js strings */
    "Could not find WMC file": "El contexto WMC especificado no existe",
    "... or a local context": "... o un contexto local",
    //"Load or add the layers from one of these map contexts:" :
	//	"Remplazar la composición actual del mapa por uno de estos contextos:",
    "A unique OSM layer": "Una capa OpenStreetMap única",
    "default viewer context": "contexto por omisión",
    "(default)": "<br/>(contexto por omisión actual)",
    /* GEOR_workspace.js strings */
    "Save to metadata": "Crear metadatos",
    "in group": "en el grupo",
    "The context title is mandatory": "Se requiere que el título contexto",
    "There was an error creating the metadata.": "Se ha producido un error al crear los metadatos.",
    "Share this map": "Comparte este mapa",
    /*
    "Mobile viewer": "Visualiseur mobile",
    "Mobile compatible viewer on sdi.georchestra.org": "Visualiseur mobile sur sdi.georchestra.org",
    "Desktop viewer": "Visualiseur web",
    "Desktop viewer on sdi.georchestra.org": "Visualiseur web sur sdi.georchestra.org",
    */
    "Abstract": "Resumen",
    "Context saving": "Guardando el contexto",
    "The file is required.": "Se requiere el nombre de archivo.",
    "Context restoring": "Restaurando el contexto",
    "<p>Please note that the WMC must be UTF-8 encoded</p>": "<p>OjO: el " +
        "archivo de contexto tiene que ser codificado en UTF-8.</p>",
    "Load": "Cargar",
    "Workspace": "Espacio de trabajo",
    "Save the map context": "Guardar el mapa",
    "Load a map context": "Cargar un mapa",
    "Get a permalink": "Crear un permalink",
    "Permalink": "Permalink",
    "Share your map with this URL: ": "Compartir su mapa con esta URL",
    "Edit in OSM": "Editar en OSM",
    "with JOSM": "con JOSM",
    "JOSM must be started with the remote control option": "Usted tiene " +
        "que lanzar JOSM y activar el control remoto",
    "with Potlatch": "con Potlatch",
    "Recommended scale is 1:10.000": "La escala de trabajo recomendada es " +
        "1:10.000",
    "with Potlatch2": "con Potlatch2",
    "with Walking Papers": "con Walking Papers",
    /* GEOR_edit.js */
    "Req.": "Req.",
    "Required": "Requerido",
    "Not required": "No requerido",
    "Synchronization failed.": "Error durante la sincronización.",
    "Edit activated": "Edición activada",
    "Hover the feature you wish to edit, or choose \"new feature\" in the edit menu": 
        "Sobrevolar los objetos de la capa que " +
        "quiere modificar, o seleccionar \"nuevo objeto\" en el menú " +
        " de edición",
    /* GeoExt.data.CSW.js */
    "no abstract": "ningún abstracto"
    // no trailing comma
});

GeoExt.Lang.add("fr", {
    "GeoExt.ux.FeatureEditorGrid.prototype": {
        deleteMsgTitle: "¿Borrar el objeto?",
        deleteMsg: "¿Confirma la supresión del objeto?",
        deleteButtonText: "Borrar",
        deleteButtonTooltip: "Borrar este objeto",
        cancelMsgTitle: "¿Cancelar la edición?",
        cancelMsg: "Varios cambios no han sido guardados. ¿Confirma la cancelación?",
        cancelButtonText: "Cancelar",
        cancelButtonTooltip: "Parar la edición, descartando los cambios",
        saveButtonText: "Guardar",
        saveButtonTooltip: "Guardar los cambios",
        nameHeader: "Nombre",
        valueHeader: "Valor"
    }
});
