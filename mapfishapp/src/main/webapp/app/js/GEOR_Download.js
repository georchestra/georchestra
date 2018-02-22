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
 * @include GEOR_waiter.js
 * @include GEOR_ows.js
 */

/* globals Ext, OpenLayers, GEOR, Styler */

Ext.namespace("GEOR");


// https://www.geopicardie.fr/geoserver/uccsa/ows?request=GetFeature&service=WFS&typeName=uccsa%3Apdccomplets3oct13&version=1.0.0&outputFormat=SHAPE-ZIP

GEOR.Download = Ext.extend(Ext.Window, {

    layout: "fit",
    closeAction: "close",
    border: false,
    constrainHeader: true,
    modal: false,
    width: 400,
    height: 300,
    format: null,

    /**
     * Property: record
     * {Ext.data.Record} The WMS or WFS layer record
     */
    record: null,

    /**
     * Property: map
     * {OpenLayers.Map}
     */
    map: null,

    /*
     * Method: initComponent.
     * Overridden constructor. Set up widgets and lay them out
     */
    initComponent: function() {
        var r = this.record,
            type = r.get("type"),
            isWFS = type === "WFS",
            layer = r.get("layer"), 
            name = r.get("title") || layer.name || "";

        this.title = OpenLayers.i18n("Download NAME", {
            "NAME": name
        });

        this.format = new OpenLayers.Format.WFSCapabilities();

        var pseudoRecord = {
            typeName: isWFS ? 
                r.get("WFS_typeName") : r.get("name"),
            owsURL: isWFS ? 
                layer.protocol.url : r.get("WFS_URL")
        };
        
        GEOR.waiter.show();
        OpenLayers.Request.GET({
            url: GEOR.ows.getWFSCapURL(pseudoRecord),
            success: function(response) {
                try {
                    var c = this.format.read(response.responseXML || response.responseText);
                    debugger;
                } catch(e) {
                    GEOR.util.errorDialog({
                        msg: this.tr("download.capabilities.error")
                    });
                }
            },
            failure: function() {
                GEOR.util.errorDialog({
                    msg: this.tr("download.capabilities.error")
                });
            },
            scope: this
        });
        
        
        //~ var store = GEOR.ows.WFSCapabilities({
            //~ url: pseudoRecord.owsURL,
            //~ storeOptions: {
                //~ url: pseudoRecord.owsURL
            //~ },
            //~ success: function(s, ) {
                //~ debugger;
            //~ },
            //~ failure: function() {
                //~ GEOR.util.errorDialog({
                    //~ msg: this.tr("download.capabilities.error")
                //~ });
            //~ },
            //~ scope: this
        //~ });
        
        // get layer model through WFS DescribeFeatureType:
        //~ this.attributeStore = GEOR.ows.WFSDescribeFeatureType(pseudoRecord, {
            //~ extractFeatureNS: true,
            //~ success: function() {
                //~ // we get the geometry column name, and remove the corresponding record from store
                //~ var idx = this.attributeStore.find("type", GEOR.ows.matchGeomProperty);
                //~ if (idx > -1) {
                    //~ // we have a geometry
                    //~ var r = this.attributeStore.getAt(idx),
                        //~ geometryName = r.get("name");
                    //~ // create the protocol:
                    //~ this.protocol = GEOR.ows.WFSProtocol(pseudoRecord, this.map, {
                        //~ geometryName: geometryName
                    //~ });
                    //~ this._geometryName = geometryName;
                    //~ // remove geometry from attribute store:
                    //~ this.attributeStore.remove(r);
                //~ } else {
                    //~ GEOR.util.infoDialog({
                        //~ msg: this.tr("querier.layer.no.geom")
                    //~ });
                //~ }
            //~ },
            //~ failure: function() {
                //~ GEOR.util.errorDialog({
                    //~ msg: this.tr("querier.layer.error")
                //~ });
            //~ },
            //~ scope: this
        //~ });


        //~ this.items = [this.filterbuilder];

        this.buttons = [{
            text: OpenLayers.i18n("Close"),
            handler: this.close,
            scope: this
        }, {
            text: OpenLayers.i18n("Search"),
            handler: this.search,
            scope: this
        }];


        GEOR.Download.superclass.initComponent.call(this);
    },


    /**
     * Method: tr
     */
    tr: function(s) {
        return OpenLayers.i18n(s);
    },


    /** private: method[destroy]
     */
    destroy: function() {
        GEOR.Download.superclass.destroy.call(this);
    }

});
