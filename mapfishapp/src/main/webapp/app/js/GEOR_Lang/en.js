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
    "NAME layer on VALUE": "${NAME} layer on ${VALUE}",
    "Open the URL url in a new window":
        "Open the ${URL} url in a new window",
    "NB layers": "${NB} layers",
    " in NB metadata": " in ${NB} metadata",
    /* GEOR_editing.js strings */
    /* GEOR_geonames.js strings */
    /* GEOR_getfeatureinfo.js strings */
    "<div>Search on objects active for NAME layer. Clic on the map.</div>":
         "<div>Search on objects active for ${NAME} layer. " +
         "Clic on the map.</div>",
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
    "The <b>NAME</b> layer could not appear for that reason: ":
        "The <b>${NAME}</b> layer could not appear for that reason: ",
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
    /* GEOR_querier.js strings */
    "Request on NAME": "Request on ${NAME}",
    "querier.layer.no.geom":
        "That layer has no geometric column." +
        "<br />The geometric request module will not work",
    "querier.layer.error":
        "Impossible to get the characteristics of that layer" +
        "<br />The geometric request module will be disabled",
    /* GEOR_resultspanel.js */
    "resultspanel.maxfeature.reached":
        " <span ext:qtip=\"Use a more powerful browser " +
        "to increase the number of visible objects\">" +
        "Max number of objects reached (${NB})</span>",
    "NB results": "${NB} results",
    /* GEOR_scalecombo.js strings */
    /* GEOR_styler.js strings */
    "styler.guidelines":
        "Use the \"+\" button to create a class," +
        "and the \"Analyze\" button to create a set " +
        "of classes defined by a thematic analyze.</p>",
    /* GEOR_tools.js strings */
    "Could not load addon ADDONNAME": "Could not load addon ${ADDONNAME}",
    /* GEOR_wmc.js strings */
    "Warning: trying to restore WMC with a different projection (PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !": "Warning: trying to restore WMC with a different projection (${PROJCODE1}, while map SRS is ${PROJCODE2}). Strange things might occur !",
    /* GEOR_wmcbrowser.js strings */
    "(default)": "<br/>(current default context)",
    /* GEOR_EditingPanel.js */
    "editingpanel.geom.error": "This layer's geometry type is ${TYPE}.<br/>" +
        "Only point, line and polygons" +
        " (and multi-*) are editable.",
    /* GEOR_LayerEditingPanel.js */
    "layereditingpanel.cancel.confirm": "Do you really want to cancel all modifications<br />since last synchronisation ?",
    "layereditingpanel.changes.confirm": "Please confirm or cancel current modifications",
    "Please select one feature.": "Veuillez sélectionner un objet."
    // no trailing comma
});
