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
 * Spanish translation file
 */
OpenLayers.Lang.es = OpenLayers.Util.extend(OpenLayers.Lang.es, {
    /* Common strings */
    "Email": "Correo electrónico",
    "OK": "OK",
    /* GEOR.js strings */
    "Enter a valid email address: ": "Ingrese una dirección de correo valida: ",
    "The email address is not valid. Stopping extraction.":
        "La dirección de correo no es valida. Parando la extracción.",
    "No layer in the cart": "Ninguna capa en la canasta",
    "You did not select any layer for extracting. Extract all ?":
        "No seleccionó ninguna capa para extracción. ¿Extraer todo?",
    "Yes": "Si",
    "No": "No",
    "Cancel": "Anular",
    "Extraction parameters applied by default to all cart layers":
        "Parámetros de extracción aplicados por omisíon a todas las capas " +
        "de la canasta",
    "paneltext1":
        "Configure los parámetros generales para la extracción utilizando " +
        "el panel derecho (aparece seleccionando 'Parámetros por omisión').",
    "paneltext2":
        "Después puede lanzar la extracción haciendo click sobre el botón " +
        "'Extraer las capas seleccionadas'.",
    "paneltext3":
        "Si desea precisar otros parámetros de extracción específicos para " +
        "una capa en particular, puede usar la jerarquía que aparece en " +
        "configuración.",
    "Extractor": "Extractor",
    "Configuration": "Configuración",
    "Extract the selected layers": "Extraer las capas seleccionadas",
    "Recenter": "Ajustar vista sobre la capa",
    "Use limits": "Limites de utilización",
    "Extraction parameters only for the NAME layer (raster)":
        "Parámetros de extracción específicos a la capa ${NAME} (raster)",
    "Extraction parameters only for the NAME layer (vector)":
        "Parámetros de extracción específicos a la capa ${NAME} (vectorial)",
    /*
    "Oversized coverage extraction": "Aire d'extraction de couche raster trop importante",
    "Extraction area for layers LAYERS is too large.<br/><br/>We cannot produce images with more than MAX million RGB pixels.<br/>Continue anyway ?": 
        "L'aire d'extraction des couches ${LAYERS} est trop importante.<br/><br/>Nous ne pouvons produire des images contenant plus de ${MAX} millions de pixels RGB.<br/>Souhaitez vous poursuivre tout de même ?",
    */
    /* GEOR_ajaxglobal.js */
    "The server did not return nothing.": "El servidor no respondió",
    "The server did not allow access.": "El servidor no permitió el acceso.",
    "ajaxglobal.error.406":
        "El servidor distante respondió, pero el contenido de la " +
        "respuesta no es lo esperado. Si utiliza Internet Explorer, cambiar " +
        "de navegador podría ayudar a resolver este problema.",
    "ajaxglobal.error.default":
        "Para más información, puede buscar el código de respuesta " +
        "en <a href=\"http://" +
        "es.wikipedia.org/wiki/Anexo:Códigos_de_estado_HTTP\">" +
        "Wikipedia</a>.",
    "ajaxglobal.error.title": "Error HTTP ${ERROR}",
    "ajaxglobal.error.body": "Un error occurió.<br />${TEXT}",
    "Warning: the browser may freeze":
        "Atención: el navegador podría bloquearse",
    "ajaxglobal.toobig":
        "Los datos del servidor son muy pesados.<br />" +
        "El servidor mandó ${WEIGHT}KBytes (el limite es de " +
        "${LIMIT}KBytes). <br />¿Desea usted continuar?",
    /* GEOR_config.js */
    "None": "Ninguno",
    "BUFFER meters": "${BUFFER} metros",
    "BUFFER kilometer": "${BUFFER} kilometro",
    "BUFFER kilometers": "${BUFFER} kilometros",
    /* GEOR_data.js */
    /* GEOR_dlform.js */
    "Firstname": "Nombre",
    "Lastname": "Apellido",
    "Company": "Organismo",
    "Phone": "Teléfono",
    "Applications": "Aplicaciones",
    "Comments": "Comentarios",
    "dlform.blanktext":
        "Marque la casilla para aceptar las condiciones de utilización",
    "dlform.checkbox":
        "<span style='font-weight:bold;'>Acepto sin reserva los <a href='" +
        "${URL}' target='_blank'>términos de uso</a> " +
        "de los datos.</span>",
    "Invalid form": "Formulario inválido",
    "dlform.save.error":
        "Error al momento de guardar el formulario. " +
        "Por favor contactese con el administrador de la plataforma.",
    "Take one minute to indicate how you use the data":
        "Tomese un tiempo para indicarnos como utilizará los datos",
    "dlform.mandatory.fields":
        "<div style='font-size:12px;'>Los campos en " +
        "<span style='font-weight:bold;'>negrita</span>" +
        " son obligatorios.</div>",
    /* GEOR_layeroptions.js */
    "layeroptions.boundingbox":
        "Extensión (en ${UNIT}, " +
        'SRS = <a href="http://spatialreference.org/ref/epsg/${NUMBER}/"' +
        'target="_blank" style="text-decoration:none">${CRS}</a>)',
    " km²": " km²",
    " m²": " m²",
    "Output projection": "Proyección de salida",
    "Raster resolution (m/pixel)": "Resolución raster (m/pixel)",
    "Raster output format": "Formato de salida raster",
    "Vector output format": "Formato de salida vectorial",
    "Output format": "Formato de salida",
    "Bounding box": "Extensión",
    /* GEOR_layerstree */
    "ERROR: owsinfo.layer should always be defined":
        "ERROR: owsinfo.layer siempre debería estar definido",
    "layerstree.qtip.wfs": "Servicio WFS <b>${TEXT}</b><br/>${URL}",
    "layerstree.maxfeatures":
        "El número máximo de objetos ha sido alcanzado: sólo " +
        "${NB} objetos aparecerán.",
    "layerstree.qtip.missingwfs":
        "La capa WFS <b>${NAME}</b> no existe en el servicio especificado " +
        "(${URL})",
    "layerstree.qtip.unavailablewfs":
        "El servicio WFS <b>${NAME}</b> no está disponible<br/>${URL}",
    "layerstree.qtip.wms": "Servicio WMS <b>${NAME}</b><br/>${URL}",
    "layerstree.qtip.badprojection":
        "No se encuentra ninguna proyección soportada " +
        "para la capa WMS <b>${NAME}</b>",
    "layerstree.qtip.missingwms":
        "La capa WMS <b>${NAME}</b> no existe en el servicio especificado " +
        "(${URL})",
    "layerstree.qtip.unavailablewms":
        "El servicio WMS <b>${NAME}</b> no está disponible<br/>${URL}",
    "layerstree.layer.tip":
        "Seleccionar la capa para visualizarla " +
        "y configurar sus parámetros de extracción específicos.<br/>" +
        "Selecciona la casilla para añadir la capa a la canasta de " +
        "extracción. Deselecciona la casilla para sacar la capa de la canasta.",
    "layerstree.qtip.noextraction":
        "La capa <b>${NAME}</b> no está disponible: " +
        "ningún servicio de extracción",
    "layerstree.describelayer":
        "La capa <b>${NAME}</b> no está disponible: la consulta WMS " +
        "DescribeLayer sobre ${URL} no se pudo realizar exitosamente.",
    "Your extraction cart": "Su canasta de extracción",
    "Loading...": "Cargando...",
    "Default parameters": "Parámetros por omisión",
    "layerstree.qtip.defaultparameters":
        "<b>Parámetros por omisión</b><br/>" +
        "Estos parámetros serán aplicados a la extracción de todas las capas " +
        "que no definen sus propios parámetros.",
    "OGC Layers": "Capas OGC",
    "OGC layers available for extraction":
        "Capas OGC disponibles para extracción",
    "OGC services": "Servicios OGC",
    "The layers of these OGC services can be extracted":
        "Servicios OGC cuyas capas pueden ser extraidas",
    "layerstree.email":
        "Extracción en curso.\n" +
        "Un correo electrónico será enviado a la dirección ${EMAIL} " +
        "cuando la extracción habrá acabado.",
    "The extraction request failed.": "El pedido de extracción falló.",
    /* GEOR_map.js */
    "Layer probably invisible at this scale: ":
        "Capa probablemente invisible a esta escala: ",
    "Base Layer": "Capa de base",
    /* GEOR_mappanel.js */
    "mappanel.qtip.coordinates": "Coordenadas del ratón en ${SRS}",
    /* GEOR_ows.js */
    /* GEOR_proj4jsdefs.js */
    /* GEOR_referentials.js */
    "Referential": "Región",
    "Select": "Seleccione",
    "The selected layer does not have a geometric column":
        "La capa seleccionada no tiene una columna geométrica",
    "Recenter on": "Posicionarse sobre",
    "location ?": "¿localidad?",
    "referentials.help":
        "<span>Esta herramienta permite utilizar una extensión geográfica " +
        "de referencia para la extracción actual</span>",
    /* GEOR_toolbar.js */
    "Zoom on the global extent of the map":
        "Zoom sobre la extensión global del mapa",
    "Pan": "Agarrar - desplazar el mapa",
    "Zoom in": "Acercamiento",
    "Back to the previous extent": "Volver al zoom anterior",
    "Go to the next extent": "Ir al zoom siguiente",
    "Help": "Ayuda",
    "Show help": "Mostrar la ayuda",
    "Extractor help": "Ayuda del extractor",
    "Login": "Conectarse",
    "Logout": "Desconectarse",
    "toolbar.confirm.login":
        "El contexto cartógrafico actual será perdido al momento de entrar " +
        "en la próxima página.",
    /* GEOR_util.js */
    "Confirm": "Confirmación",
    "Information": "Información",
    "Error": "Error",
    "degrees": "grados",
    "meters": "metros",
    /* GEOR_waiter.js */
    /* OpenLayers.Control.OutOfRangeLayers.js */
    "List of layers out of range: ": "Lista de capas fuera de la extensión.",
    /* BoundingBoxPanel.js */
    "Modify the bounding box": "Modificar la extensión geográfica",
    "Modify the bounding box drawing a new one on the map":
        "Modificar la extensión geográfica dibujando una nueva sobre el mapa"
    // no trailing comma
});
