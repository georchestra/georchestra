/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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
    /* GEOR.js strings */
    "paneltext1": "Configure the general parameters for your extraction " +
                  "using the panel on the right (deployed with 'Parameters " +
                  "by default').",
    "paneltext2": "You will then be able to launch the extraction with " +
                  "a click on the button called 'Extract the selected " +
                  "layers'.",
    "paneltext3": "If you want to precise specific extraction parameters " +
                  "for one layer, select it in the tree above.",
    "Extraction parameters only for the NAME layer (raster)":
        "Extraction parameters only for the ${NAME} layer (raster)",
    "Extraction parameters only for the NAME layer (vector)":
        "Extraction parameters only for the ${NAME} layer (vector)",
    "Extraction area for layers LAYERS is too large.<br/><br/>We cannot produce images with more than MAX million RGB pixels.<br/>Continue anyway ?": 
        "Extraction area for layers ${LAYERS} is too large.<br/><br/>We cannot produce images with more than ${MAX} million RGB pixels.<br/>Continue anyway ?",
    /* GEOR_ajaxglobal.js */
    "ajaxglobal.error.406":
        "The remote server returned an inesperated answer. If you are using " +
        "Internet Explorer, changing your browser could help to solve " +
        "the problem.",
    "ajaxglobal.error.default":
        "For more information, you may search the return code in " +
        "<a href=\"http://" +
        "en.wikipedia.org/wiki/List_of_HTTP_status_codes\">" +
        "Wikipedia</a>.",
    "ajaxglobal.error.title": "Error HTTP ${ERROR}",
    "ajaxglobal.error.body": "An error has been raised.<br />${TEXT}",
    "ajaxglobal.toobig":
        "Data from the server is too big.<br />" +
        "The server sent ${WEIGHT}KBytes (the limit is " +
        "${LIMIT}KBytes). <br />Do you want to continue ?",
    /* GEOR_config.js */
    "BUFFER meters": "${BUFFER} meters",
    "BUFFER kilometer": "${BUFFER} kilometer",
    "BUFFER kilometers": "${BUFFER} kilometers",
    /* GEOR_data.js */
    /* GEOR_layeroptions.js */
    "layeroptions.boundingbox":
        "Bounding box (in ${UNIT}, " +
        'SRS = <a href="http://spatialreference.org/ref/epsg/${NUMBER}/"' +
        'target="_blank" style="text-decoration:none">${CRS}</a>)',
    /* GEOR_layerstree */
    "layerstree.qtip.wfs": "WFS service <b>${TEXT}</b><br/>${URL}",
    "layerstree.maxfeatures":
        "Max number of objects has been reached: only " +
        "${NB} objects will be displayed.",
    "layerstree.qtip.missingwfs":
        "The <b>${NAME}</b> WFS layer does not exist in the specified service" +
        "(${URL})",
    "layerstree.qtip.unavailablewfs":
        "<b>${NAME}</b> WFS service unavailable<br/>${URL}",
    "layerstree.qtip.wms": "<b>${NAME}</b> WMS service<br/>${URL}",
    "layerstree.qtip.badprojection":
        "Impossible to find a supported projection " +
        "for the <b>${NAME}</b> WMS layer",
    "layerstree.qtip.missingwms":
        "The <b>${NAME}</b> WMS layer does not exist in the specified service" +
        "(${URL})",
    "layerstree.qtip.unavailablewms":
        "<b>${NAME}</b> WMS service unavailable<br/>${URL}",
    "layerstree.layer.tip":
        "Select the layer to view it " +
        "and configure its specific extraction parameters.<br/>" +
        "Tick the checkbox to add the layer to the extraction cart. " +
        "Untick the checkbox to eject the layer from the cart.",
    "layerstree.qtip.noextraction":
        "The <b>${NAME}</b> layer is not available: " +
        "there is no extraction service",
    "layerstree.describelayer":
        "The <b>${NAME}</b> layer is not available: the DescribeLayer WMS " +
        "request on ${URL} could not conclude succesfully.",
    "layerstree.qtip.defaultparameters":
        "<b>Default parameters</b><br/>" +
        "These parameters will be applied to the extraction of every layer " +
        "that does not define its own parameters.",
    "layerstree.email":
        "The extraction is running.\n" +
        "An email will be sent to the ${EMAIL} address " +
        "when the extraction is complete.",
    /* GEOR_map.js */
    /* GEOR_mappanel.js */
    "mappanel.qtip.coordinates": "Coordinates of mouse in ${SRS}",
    /* GEOR_ows.js */
    /* GEOR_proj4jsdefs.js */
    /* GEOR_referentials.js */
    "referentials.help":
        "<span>This tool allows to use a reference bounding box for the " +
        "current extraction bounding box</span>",
    /* GEOR_scalecombo.js */
    /* GEOR_toolbar.js */
    "toolbar.confirm.login":
        "You will be sent to another page and lose the current map context."
    /* GEOR_util.js */
    /* GEOR_waiter.js */
    /* OpenLayers.Control.OutOfRangeLayers.js */
    // no trailing comma
});
