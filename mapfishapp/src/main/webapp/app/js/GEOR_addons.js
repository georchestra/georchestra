Ext.namespace("GEOR.Addons");

GEOR.Addons.Base = Ext.extend(Object, {

    constructor: function(mp, options) {
        this.mapPanel = mp;
        this.map = mp.map;
        this.options = options;
        this.components = null;
        this.lang = OpenLayers.Lang.getCode();
        if (this.options.target) {
            var t = this.options.target.split("_"),
                target = t[0];
            this.position = parseInt(t[1]) || 0;
            this.target = null;
            switch (target) {
                // top toolbar:
                case "tbar":
                    this.target = this.mapPanel.getTopToolbar();
                    break;
                // bottom toolbar:
                case "bbar":
                    this.target = this.mapPanel.getBottomToolbar();
                    break;
                // mini tabpanel in lower right corner:
                case "tabs":
                    this.target = Ext.getCmp("tabs");
                    break;
            }
        }
    },

    /**
     * Method: getTooltip
     */
    getTooltip: function(record) {
        return [
            "<b>",
            this.getText(record),
            "</b><br>",
            this.getQtip(record)
        ].join('');
    },

    /**
     * Method: getText
     */
    getText: function(record) {
        return record.get("title")[this.lang]
            || record.get("title")["en"];
    },

    /**
     * Method: getQtip
     */
    getQtip: function(record) {
        return record.get("description")[this.lang]
            || record.get("description")["en"];
    },

    /**
     * Method: destroy
     * Called by GEOR_tools when deselecting this addon
     */
    destroy: function() {
        if (this.target) {
            Ext.each(this.components, function(cmp) {
                this.target.remove(cmp);
            }, this);
            this.components = null;
        }
        this.map = null;
        this.mapPanel = null;
    }
});