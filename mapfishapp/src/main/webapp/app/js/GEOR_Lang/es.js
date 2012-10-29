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
 * Spanish translation file
 */
OpenLayers.Lang.es = OpenLayers.Util.extend(OpenLayers.Lang.es, {
    /* General purpose strings */
    "Yes": "Si",
    "No": "No",
    "OK": "OK",
    "Cancel": "Cancelar",
    "Save": "Guardar",
    "Loading...": "Cargando...",
    "Layer": "Capa",
    "Description": "Descripción",
    "Error": "Error",
    "The provided context is not valid.": "El contexto proveído no es valido.",
    "Server": "Servidor",
    "Close": "Cerrar",
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
    "Editing": "Edición",
    "resultspanel.emptytext":
        "<p>Seleccione la herramienta de interrogación " +
        "o construya una consulta sobre una capa.<br />" +
        "Los atributos de los objetos se mostrarán en este espacio.</p>",
    /* GEOR_ClassificationPanel.js strings */
    "Attribute": "Atributo",
    "Type": "Tipo",
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
    "NAME layer": "Capa ${name}",
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
    "Show metadata sheet in a new window":
        "Mostrar la ficha de metadatos en una nueva ventana",
    'more': 'más',
    "Clic to select or deselect the layer":
        "Haga clic para seleccionar o deseleccionar la capa",
    "Open the URL url in a new window":
        "Abrir la URL ${url} en una nueva ventana",
    "Unreachable server": "El servidor no está disponible",
    "Catalogue": "Catálogo",
    "Find": "Buscar",
    "in": "en",
    "Not any layer": "Ninguna capa",
    "1 layer": "1 capa",
    "NB layers": "${NB} capas",
    " in NB metadata": " en ${NB} metadatos",
    " in 1 metadata": " en 1 metadato",
    ": precise your request": ": precise su consulta",
    "Not any metadata correspond to the words specified":
        "Ningún metadato corresponde a los términos ingresados",
    /* GEOR_editing.js strings */
    /* GEOR_geonames.js strings */
    /* GEOR_getfeatureinfo.js strings */
    "<div>Searching...</div>": "<div>Buscando...</div>",
    "<div>Search on objects active for NAME layer. Clic on the map.</div>":
         "<div>Búsqueda de objetos activada sobre la capa ${name}. " +
         "Haga clic sobre el mapa.</div>",
    /* GEOR_layerfinder.js strings */
    "Find layers searching in metadata":
        "Encontrar capas buscando en los metadatos",
    "Find layers from keywords": "Encontrar capas desde palabras clave",
    "Find layers querying WMS servers":
        "Encontrar capas interrogando servicios WMS",
    "Find layers querying WFS servers":
        "Encontrar capas interrogando servicios WFS",
    "layerfinder.layer.unavailable":
        "La capa ${name} no pudo ser encontrada en el servicio WMS.<br/<br/>" +
        "Tiene el derecho de acceder a esta capa ? " +
        "Quizás la capa no está disponible actualmente",
    "Layer projection is not compatible":
        "La proyección de la capa no es compatible.",
    "The NAME layer does not contain a valid geometry column":
        "La capa ${name} no contiene una columna de geometría válida.",
    "Add": "Añadir",
    "Add layers from a ...": "Añadir capas desde un ...",
    "Malformed URL": "URL mal formada.",
    /* GEOR_managelayers.js strings */
    "Confirm NAME layer deletion ?":
        "¿Confirma que se suprimirá la capa ${name}?",
    "1:MAXSCALE to 1:MINSCALE": "1:${maxscale} a 1:${minscale}",
    "Visibility range (indicative):<br />from TEXT":
        "Rango de visibilidad (indicativo):<br />de ${text}",
    "Information on objects of this layer":
        "Información sobre los objetos de esta capa",
    "Default style": "Estilo por omisión",
    "Recenter on the layer": "Ajustar vista sobre la capa",
    "Impossible to get layer extent":
        "No se puede obtener la extensión de la capa.",
    "Refresh layer": "Recargar la capa",
    "Show metadata": "Mostrar los metadatos",
    "Edit symbology": "Editar la simbología",
    "Build a query": "Construir una consulta",
    "Failed to get WFS layer address. <br />The query module will be disabled":
        "No se puede obtener la dirección de la capa WFS." +
        "<br />El módulo de consultas no estará disponible.",
    "DescribeLayer WMS query failed. <br />The query module will be disabled":
        "La consulta WMS DescribeLayer falló." +
        "<br />El módulo de consultas no estará disponible.",
    "Download data": "Descargar los datos",
    "Choose a style": "Elegir un estilo",
    "Modify format": "Modificar el formato",
    "Delete this layer": "Quitar esta capa",
    "Push up this layer": "Subir esta capa",
    "Push down this layer": "Bajar esta capa",
    "Add layers": "Añadir capas",
    "source: ": "fuente: ",
    "Actions": "Acciones",
    /* GEOR_map.js strings */
    "Location map": "Mapa de ubicación",
    "Warning after loading layer":
        "Advertencia después de cargar la capa",
    "The <b>NAME</b> layer could not appear for that reason: ":
        "La capa <b>${name}</b> podría no aparecer por la siguiente razón: ",
    "Min/max visibility scales are invalid":
        "Las escalas min/max de visibilidad son invalidas.",
    "Visibility range does not match map scales":
        "El rango de visibilidad no corresponde a las escalas del mapa.",
    "Geografic extent does not match map extent":
        "La extensión geográfica no corresponde a la extensión del mapa",
    /* GEOR_mapinit.js strings */
    "Add layers from WMS services": "Añadir capas desde servicios WMS",
    "NB layers not imported": "${NB} capas no importadas",
    "One layer not imported": "Una capa no importada",
    "mapinit.layers.load.error":
        "Las capas siguientes: ${list}, no se lograron cargar: " +
        "SRS incompatible o la capa no existe",
    "NB layers imported": "${NB} capas importadas",
    "One layer imported": "Una capa importada",
    "Not any layer imported": "Ninguna capa importada",
    "The default context is not defined (and it is a BIG problem!)":
        "El contexto por omisión no está definido ",
    /* GEOR_mappanel.js strings */
    "Mouse coordinates in SRS": "Coordenadas del ratón en ${srs}",
    /* GEOR_ows.js strings */
    "The NAME layer was not found in WMS service.":
        "La capa ${name} no se encuentra en el servicio WMS",
    /* GEOR_print.js strings */
    "Sources: ": "Fuentes: ",
    "Source: ": "Fuente: ",
    "Print error": "Error de impresión",
    "Print server returned an error":
        "El servicio de impresión señaló un error.",
    "Contact platform administrator":
        "Contacte el administrador de la plataforma.",
    "Layer unavailable for printing": "La capa no está disponible para " +
        "imprimir",
    "The NAME layer cannot be printed.": "La capa ${name} no puede imprimirse.",
    "Unable to print": "La impresión no está disponible",
    "The print server is currently unreachable":
        "El servicio de impresión está actualmente inaccesible.",
    "print.unknown.format":
         "Error de configuración: DEFAULT_PRINT_FORMAT " +
         "${format} no se encuentra en los formatos de impresión",
    "print.unknown.resolution":
         "Error de configuración: DEFAULT_PRINT_RESOLUTION " +
         "${resolution} no se encuentra en las resoluciones de impresión",
    "Title": "Título",
    "Copyright": "Copyright",
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
    "Request on NAME": "Consultas sobre ${name}",
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
    "Not any result": "Ningún resultado",
    "Clean": "Quitar",
    "Clean all results on the map and in the table": "Quitar los resultados" +
        "del mapa y en la tabla",
    "Zoom": "Zoom",
    "Zoom to results extent": "Zoom sobre la extensión de los resultados",
    "CSV Export": "Exportar en CSV",
    "Export results as CSV": "Exportar todos los resultados en CSV",
    "<p>Not any result for that request.</p>": "<p>Ningún objeto " +
        "corresponde a su consulta.</p>",
    /* GEOR_scalecombo.js strings */
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
    "not any WFS service associated to that layer": "ningún servicio WFS " +
        "asociado a esta capa.",
    "not any available attribute": "ningún atributo disponible.",
    /* GEOR_toolbar.js strings */
    "m": "m",
    "hectares": "hectáreas",
    "zoom to global extent of the map": "Zoom sobre la extensión global " +
        "del mapa",
    "pan": "Agarrar - desplazar el mapa",
    "zoom in": "Acercamiento",
    "distance measure": "Medir una distancia",
    "area measure": "Medir un área",
    "back to previous zoom": "Volver al zoom anterior",
    "go to next zoom": "Ir al zoom siguiente",
    "Login": "Conexión",
    "Logout": "Desconexión",
    "Help": "Ayuda",
    "Show help": "Mostrar la ayuda",
    "Extractor help": "Ayuda del extractor",
    "Show legend": "Mostrar la leyenda",
    "Leave this page ? You will lose the current cartographic context.":
        "Usted desea salir de esta página ? Se perderá el contexto cartográfico actual.",
    /* GEOR_util.js strings */
    "Characters": "Caracteres",
    "Digital": "Digital",
    "Boolean": "Boolean",
    "Other": "Otro",
    "Confirmation": "Confirmación",
    "Information": "Información",
    /* GEOR_waiter.js strings */
    /* GEOR_wfsbrowser.js strings */
    "Choose a WFS server: ": "Elija un servicio WFS: ",
    "Unreachable server or insufficient rights": "Respuesta del servidor " +
        "invalida. Razones posibles: datos demasiado pesados, derechos insuficientes, " +
        "servidor inalcanzable, etc.",
    "WFS server": "Servicio WFS",
    /* GEOR_wmc.js strings */
    "wmc.bad.srs": "El archivo .wmc no " +
        "puede ser restaurado. Su sistema de referencia espacial es " +
        "diferente del sistema del mapa actual.",
    /* GEOR_wmsbrowser.js strings */
    "Queryable": "Interrogable",
    "Opaque": "Opaco",
    "Choose a WMS server: ": "Elija un servicio WMS: ",
    "... or enter its address: ": "... o llene su dirección: ",
    "The server is publishing one layer with an incompatible projection":
        "El servicio está publicando una capa cuya proyección no es compatible",
    "The server is publishing NB layers with an incompatible projection":
        "El servicio esta publicando ${NB} capas cuya proyección no es " +
        "compatible",
    "WMS server": "Servicio WMS",
    /* GEOR_workspace.js strings */
    "Context saving": "Guardando el contexto",
    "The file is required.": "Se requiere el nombre de archivo.",
    "Context restoring": "Restaurando el contexto",
    "<p>Please note that the WMC must be UTF-8 encoded</p>": "<p>OjO: el " +
        "archivo de contexto tiene que ser codificado en UTF-8.</p>",
    "File": "Archivo",
    "Load": "Cargar",
    "Workspace": "Espacio de trabajo",
    "Save the map context": "Guardar el mapa",
    "Load a map context": "Cargar un mapa",
    "Edit in OSM": "Editar en OSM",
    "with JOSM": "con JOSM",
    "JOSM must be started with the remote control option": "Usted tiene " +
        "que lanzar JOSM y activar el control remoto",
    "with Potlatch": "con Potlatch",
    "Recommended scale is 1:10.000": "La escala de trabajo recomendada es " +
        "1:10.000",
    "with Potlatch2": "con Potlatch2",
    "with Walking Papers": "con Walking Papers",
    /* GEOR_EditingPanel.js */
    "Read-only layer": "Esta capa no se puede editar",
    "editingpanel.geom.error": "La geometría de esta capa es de tipo " +
        "${type}.<br/>Sólo se puede editar las geometrías de tipo punto, " +
        "línea y polígono (y multi-*).",
    "choose a layer": "elija una capa",
    /* GEOR_LayerEditingPanel.js */
    "Modify object": "Modificar un objeto",
    "Cancel all": "Cancelar todo",
    "Enter ": "Entrar ",
    "layereditingpanel.cancel.confirm": "Confirma la cancelación de todas " +
        "las modificación<br />desde la última sincronización ?",
    "Synchronize": 'Sincronizar',
    "layereditingpanel.changes.confirm": "Confirmar o cancelar las " +
        "modificaciones en curso.",
    "Delete": "Quitar",
    "Confirm": "Confirmar",
    " (required)": " (requerido)",
    "Text too long": "Texto demasiado largo",
    "Text too short": "Texto demasiado corto",
    "Maximum value exceeded": "Excede el valor máximo",
    "Minimum value not reached": "No llega al valor mínimo",
    "Invalid number": "Número invalido",
    "Synchronization successful.": "Sincronización exitosa.",
    "Synchronization failed.": "Error durante la sincronización.",
    "Recover": "Restaurar",
    "No feature selected !": "Ningún objeto seleccionado !",
    "a point": "un punto",
    "a line": "una línea",
    "a polygon": "un polígono",
    "Pending changes": "Modificaciones en curso",
    "Do you want to confirm changes ?": "¿Desea confirmar las modificaciones ?",
    /* SpatialComboBox.js */
    "intersection": "intersección con",
    "inside": "adentro de",
    "contains": "alrededor de",
    /* FilterPanel.js */
    "This field is mandatory": "Este campo es requerido",
    /* SpatialFilterPanel.js */
    "Modify geometry": "Modificar la geometría",
    "Save this geometry": "Grabar la geometría",
    "spatialfilterpanel.geometry.saved": "Geometría grabada para 30 días " +
        "en este navegador.",
    /* FilterBuilder.js */
    "any": "una de",
    "all": "todas",
    "none": "ninguna de",
    "not all": "no todas",
    "Matching": "Corresponder a",
    "these conditions": "estas condiciones",
    "condition": "Condición",
    "spatial condition": "Condición espacial",
    "group": "Grupo",
    "based on a point": "Basada en un punto",
    "based on a line": "Basada en una línea",
    "based on a polygon": "Basada en un polígono",
    "based on a stored geometry": "Basada en una geometría archivada",
    "Delete this condition": "Quitar esta condición",
    /* GeoExt.data.CSW.js */
    "no abstract": "ningún abstracto",
    /* Styler.js */
    "Unable to read capabilities from WMS":
        "No se pudó leer las 'capabilities' desde el WMS",
    "Unable to read capabilities from WFS":
        "No se pudó leer las 'capabilities' desde el WFS",
    "Add new": "Crear una nueva regla",
    "Delete selected": "Borrar la selección",
    "styler.delete.rule":
        "¿Confirma la supresión de la regla ${NAME}?",
    "Delete rule": "Borrar la regla",
    "Layers": "Capas",
    "styler.feature": "Objeto: ${FEATURE}",
    "Rules used to render this feature:":
        "Reglas utilizadas para este objeto:",
    "Attributes of this feature:": "Atributos de este objeto:",
    "styler.style": "Estilo: ${STYLE}",
    "Untitled": "Sin título",
    "styler.div.zoomlevel": "<div>{zoomType} Nivel de zoom: {zoom}</div>",
    "styler.div.mapzoom": "<div>Zoom actual en el mapa: {mapZoom}</div>",
    "Circle": "Círculo",
    "Square": "Cuadrado",
    "Triangle": "Triángulo",
    "Star": "Asterisco",
    "Cross": "Cruz",
    "X": "X",
    "Custom...": "Personalizado...",
    "Cancel": "Cancelar",
    "Save": "Guardar",
    "Could not load features from the WFS":
        "Imposible cargar los objetos desde WFS",
    /* ColorManager.js */
    "Color Picker": "Selector de color",
    /* FillSymbolizer.js */
    "Fill": "Relleno",
    "Color": "Color",
    "Opacity": "Opacidad",
    /* LegendPanel.js */
    "Untitled ": "Sin título ",
    /* PointSymbolizer.js */
    "Circle": "Círculo",
    "Square": "Cuadrado",
    "Triangle": "Triángulo",
    "Star": "Asterisco",
    "Cross": "Cruz",
    "X": "X",
    "External": "Externo",
    "URL": "URL",
    "Opacity": "Opacidad",
    "Symbol": "Símbolo",
    "Radius": "Tamaño",
    "Rotation": "Rotación",
    /* RuleBuilder.js */
    "Add rule": "Crear una regla",
    "Untitled ": "Sin título ",
    /* RuleChooser.js */
    "Styling rules that apply for this feature":
        "Reglas de estilo que se pueden aplicar a este objeto",
    "Default": "Por omisión",
    '{type} for the "{layer}" layer':
        '{type} para la capa "{layer}"',
    'Create a new styling rule': "Crear una nueva regla de estilo",
    "Other styling rules": "Otras reglas de estilo",
    "Styling rules": "Reglas de estilo",
    /* RulePanel.js */
    "{type} scale 1:{scale}": "Escala {type} 1:{scale}",
    "Labels": "Etiquetados",
    "Simple": "Simple",
    "Advanced": "Avanzado",
    "Limit by scale": "Límite por escala",
    "Limit by condition": "Límite por condición",
    "Symbol": "Símbolo",
    "Name": "Nombre",
    /* ScaleLimitPanel.js */
    "Min scale": "Escala mínima",
    "Max scale": "Escala máxima",
    /* StrokeSymbolizer.js */
    "Solid": "Línea continua",
    "Dash": "Línea segmentada",
    "Dot": "Línea de puntos",
    "Border": "Borde",
    "Style": "Estilo",
    "Color": "Color",
    "Width": "Grosor",
    "Opacity": "Opacidad",
    /* TextSymbolizer */
    "Attribute": "Atributo",
    "Size: ": "Tamaño: ",
    "Halo": "Sombra",
    "Size": "Tamaño",
    /* ScaleSliderTip.js */
    "Zoom Level: {zoom}": "Nivel de zoom: {zoom}",
    "Resolution: {resolution}": "Resolución: {resolution}",
    "Scale: 1 : {scale}": "Escala: 1 : {scale}",
    /*GEOR_addonsmenu.js */
    "Tools": "Herramientas"
    // no trailing comma
});
