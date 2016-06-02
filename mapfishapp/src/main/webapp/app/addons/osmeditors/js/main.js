/*global
 Ext, GeoExt, OpenLayers, GEOR
 */
Ext.namespace("GEOR.Addons");

//Replace Template by a representative name
GEOR.Addons.OsmEditors = Ext.extend(GEOR.Addons.Base, {

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {



        this.map = GEOR.mappanel;

        if (this.target) {
            // create a button to be inserted in toolbar:
            this.components = this.target.insertButton(this.position, {
                tooltip: this.getTooltip(record),
                iconCls: "addon-osmeditors",
                text: this.getText(record),
                plugins: [{
                    ptype: "menuqtips"
                }],
                menu: this._selectEditors(this.options),
                scope: this
            });
            this.target.doLayout();

        } else {
            // create a menu item for the "tools" menu:
            this.item = new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: "addon-osmeditors",
                checked: false,
                plugins: [{
                    ptype: "menuqtips"
                }],
                menu: this._selectEditors(this.options),
                scope: this
            });
        }
    },

    _selectEditors: function(options) {
        var editors = [];

        var map = this.mapPanel.map;

        /**
         * Method: editOSM
         * Creates handlers for OSM edition
         */
        var editOSM = function(options) {
            var round = GEOR.util.round;
            return function() {
                var url, bounds = map.getExtent();
                bounds.transform(
                    map.getProjectionObject(),
                    new OpenLayers.Projection("EPSG:4326")
                );
                if (options.protocol === "lbrt") {
                    url = options.base + OpenLayers.Util.getParameterString({
                            left: round(bounds.left, 5),
                            bottom: round(bounds.bottom, 5),
                            right: round(bounds.right, 5),
                            top: round(bounds.top, 5)
                        });
                    frames[0].location.href = url;
                } else if (options.protocol === "llz") {
                    var c = bounds.getCenterLonLat();
                    /*
                     Zoom level determined based on the idea that, for OSM:
                     maxResolution: 156543 -> zoom level 0
                     numZoomLevels: 19
                     */
                    var zoom = Math.round((Math.log(156543) - Math.log(map.getResolution())) / Math.log(2));
                    url = options.base + OpenLayers.Util.getParameterString({
                            lon: round(c.lon, 5),
                            lat: round(c.lat, 5),
                            zoom: Math.min(19, zoom - 1)
                        });
                    window.open(url);
                }
            };
        };

        if (options.editors.iD) {
            editors.push({
                text: this.tr("with iD"),
                qtip: this.tr("Recommended scale is 1:10.000"),
                handler: editOSM.call(this, {
                    base: "http://www.openstreetmap.org/edit?editor=id&",
                    protocol: "llz"
                })
            });
        }

        if (options.editors.potlach) {
            editors.push(
                {
                    text: this.tr("with Potlatch2"),
                    qtip: this.tr("Recommended scale is 1:10.000"),
                    handler: editOSM.call(this, {
                        base: "http://www.openstreetmap.org/edit?editor=potlatch2&",
                        protocol: "llz"
                    })
                }
            );
        }

        if (options.editors.JOSM) {
            editors.push(
                {
                    text: this.tr("with JOSM"),
                    qtip: this.tr("JOSM must be started with the remote control option"),
                    handler: editOSM.call(this, {
                        base: "http://127.0.0.1:8111/load_and_zoom?",
                        protocol: "lbrt"
                    })
                }
            );
        }

        if (options.editors.WalkingPapers) {
            editors.push({
                text: this.tr("with Walking Papers"),
                qtip: this.tr("Recommended scale is 1:10.000"),
                handler: editOSM.call(this, {
                    base: "http://walking-papers.org/?",
                    protocol: "llz"
                })
            });
        }

        if (editors.length < 1) {
            //TODO tr
            GEOR.util.errorDialog("No editor available. Please review osmeditors configuration.")
        }

        return editors;
    },

    tr: function(str) {
        return OpenLayers.i18n(str);
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        //Place addon specific destroy here

        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});
