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
 * English translation file
 */
OpenLayers.Lang.en = OpenLayers.Util.extend(OpenLayers.Lang.en, {
    /* General purpose strings */
    "labelSeparator": ": ",
    /* GEOR.js strings */
    "resultspanel.emptytext":
        "<p>Select the Info tool or build a query on a layer.<br />" +
        "Feature attributes will be displayed in this panel.</p>",
    /* GEOR_ClassificationPanel.js strings */
    /* GEOR_FeatureDataModel.js strings */
    /* GEOR_address.js strings */
    /* GEOR_ajaxglobal.js strings strings */
    "ajax.badresponse":
        "The server responded, but the content of the response is " +
        "different to what expected",
    "ajax.defaultexception":
        "To get more information, look for the error code at <a href=\"http://" +
        "en.wikipedia.org/wiki/List_of_HTTP_status_codes\" target=\"_blank\">" +
        "wikipedia</a>.",
    "ajaxglobal.data.too.big": "Datas sent by the server are too " +
        "big.<br />Server sent ${SENT}KBytes " +
        "(the limit is ${LIMIT}KBytes)<br />Do you want to continue ?",
    /* GEOR_config.js strings */
    /* GEOR_cswbrowser.js strings */
    "NAME layer": "${NAME} layer",
    "cswbrowser.default.thesaurus.mismatch":
        "Administrator: problem in the configuration - " +
        "the DEFAULT_THESAURUS_KEY variable does not match " +
        "any value exported by GeoNetwork",
    /* GEOR_cswquerier.js strings */
    "cswquerier.help.title": "Hints for advanced query",
    "cswquerier.help.message": '<ul><li><b>@word</b> looks for "word" in the organization name.</li><li><b>#word</b> looks for "word" in the metadata keywords.</li><li><b>?word</b> broadens the search by looking for "word" in any metadata field.</li></ul>',
    "NAME layer on VALUE": "${NAME} layer on ${VALUE}",
    "Open the URL url in a new window":
        "Open the ${URL} url in a new window",
    "NB layers found.": "${NB} layers found.",
    "NB metadata match the query.": "${NB} metadata match the query.",
    /* GEOR_editing.js strings */
    /* GEOR_fileupload.js strings */
    "fileupload_error_incompleteMIF": "Incomplete MIF/MID file.",
    "fileupload_error_incompleteSHP": "Incomplete shapefile.",
    "fileupload_error_incompleteTAB": "Incomplete TAB file.",
    "fileupload_error_ioError": "Server-side I/O exception. Contact platform administrator for more details.",
    "fileupload_error_multipleFiles": "Multiple data files encountered in ZIP archive. It should only contain one.",
    "fileupload_error_outOfMemory": "Server is out of memory. Contact platform administrator for more details.",
    "fileupload_error_sizeError": "This file is too large to be uploaded.",
    "fileupload_error_unsupportedFormat": "This format is not supported.",
    "fileupload_error_projectionError": "Error occured while trying to parse coordinates. Are you sure the file contains SRS information ?",
    "server upload error: ERROR": "Upload failed. ${ERROR}",
    /* GEOR_geonames.js strings */
    /* GEOR_getfeatureinfo.js strings */
    "<div>Search on objects active for NAME layer. Click on the map.</div>":
         "<div>Search on objects active for ${NAME} layer. " +
         "Click on the map.</div>",
    /* GEOR_layerfinder.js strings */
    "layerfinder.layer.unavailable":
        "The ${NAME} layer could not be found in WMS service.<br/<br/>" +
        "Do you have sufficient rights ? " +
        "Perhaps this layer is currently unavailable",
    "The NAME layer does not contain a valid geometry column":
        "The ${NAME} layer does not contain a valid geometry column.",
    "The server is publishing NB layers with an incompatible projection":
        "The server is publishing ${NB} layers with an incompatible projection",
    "Unreachable server or insufficient rights": "Could not get a valid response from " +
        "the server. Possible reasons: insufficient rights, server is down, too much data.",
    /* GEOR_managelayers.js strings */
    "Confirm NAME layer deletion ?": "Confirm ${NAME} layer deletion ?",
    "1:MAXSCALE to 1:MINSCALE": "1:${MAXSCALE} to 1:${MINSCALE}",
    "Visibility range (indicative):<br />from TEXT":
        "Visibility range (indicative):<br />from ${TEXT}",
    /* GEOR_map.js strings */
    "The <b>NAME</b> layer could not appear for this reason: ":
        "The <b>${NAME}</b> layer could not appear for this reason: ",
    /* GEOR_mapinit.js strings */
    "NB layers not imported": "${NB} layers not imported",
    "mapinit.layers.load.error":
        "The layers named ${LIST} could not be loaded. Possible reasons: " +
        "insufficient rights, incompatible CRS or layer does not exist",
    "NB layers imported": "${NB} layers imported",
    /* GEOR_mappanel.js strings */
    /* GEOR_ows.js strings */
    "The NAME layer was not found in WMS service.":
        "The ${NAME} layer was not found in WMS service.",
    /* GEOR_print.js strings */
    "Projection: PROJ": "Projection: ${PROJ}",
    "The NAME layer cannot be printed.":
        "The ${NAME} layer cannot be printed.",
    "print.unknown.layout":
         "Configuration error: DEFAULT_PRINT_LAYOUT " +
         "${LAYOUT} not found in print capabilities",
    "print.unknown.resolution":
         "Configuration error: DEFAULT_PRINT_RESOLUTION " +
         "${RESOLUTION} not found in print capabilities",
    "print.unknown.format":
        "Configuration error: the " +
        "${FORMAT} format is not supported by the print server",
    /* GEOR_Querier.js strings */
    "Request on NAME": "Request on ${NAME}",
    "querier.layer.no.geom":
        "This layer has no geometric column." +
        "<br />The geometric request module will not work",
    "querier.layer.error":
        "Impossible to get the characteristics of this layer" +
        "<br />The geometric request module will be disabled",
    /* GEOR_resultspanel.js */
    "resultspanel.maxfeature.reached":
        " <span ext:qtip=\"Use a more powerful browser " +
        "to increase the number of visible objects\">" +
        "Max number of objects reached (${NB})</span>",
    "NB results": "${NB} results",
    /* GEOR_scalecombo.js strings */
    /* GEOR_selectfeature.js strings */
    "<div>Select features activated on NAME layer. Click on the map.</div>":
         "<div>Select features activated on ${NAME} layer. " +
         "Click on the map.</div>",
    "OpenLayers SelectFeature":"Select features",
    /* GEOR_styler.js strings */
    "styler.guidelines":
        "Use the \"+\" button to create a class," +
        "and the \"Analyze\" button to create a set " +
        "of classes defined by a thematic analyze.</p>",
    /* GEOR_toolbar.js strings */
    /* GEOR_tools.js strings */
    "Could not load addon ADDONNAME": "Could not load addon ${ADDONNAME}",
    /* GEOR_util.js strings */
    "pointOfContact": "contact",
    /* GEOR_waiter.js strings */
    /* GEOR_wmc.js strings */
    "Warning: trying to restore WMC with a different projection (PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !": "Warning: trying to restore WMC with a different projection (${PROJCODE1}, while map SRS is ${PROJCODE2}). Strange things might occur !",
    /* GEOR_wmcbrowser.js strings */
    "(default)": "<br/>(current default context)"
    /* GEOR_edit.js */
    /* GEOR_workspace.js strings */
    // no trailing comma
});
