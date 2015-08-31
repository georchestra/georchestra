Ext.namespace("GEOR.Addons");

GEOR.Addons.Fullscreen = function(map, options) {
    this.map = map;
    this.options = options;
    this.action = null;
};

GEOR.Addons.Fullscreen.prototype = (function() {
    var _mapContainer = null;
    var _qtip = null;
    var _iconCls = null;

    /**
     * Method: _fullScreen
     * Display map in full screen. Functionality is the same as running the F11 key.
     */
    var _fullScreen = function() {
      fullScreenApi.requestFullScreen(_mapContainer);
    }

    return {
        /*
         * Public
         */
        activateTool: function() {
            this.action = new Ext.Action({
                handler: _fullScreen,
                scope: this,
                tooltip: _qtip,
                iconCls: _iconCls
            });
            this.toolbar = this.options.placement == 'top' ? Ext.getCmp("mappanel").topToolbar : Ext.getCmp("mappanel").bottomToolbar;
            this.toolbar.insert(parseInt(this.options.position), this.action);
            this.toolbar.insert(parseInt(this.options.position), '-');
            this.toolbar.doLayout();
        },

        /**
         * Method: init
         *
         * Parameters:
         * record - {Ext.data.record} a record with the addon parameters
         */
        init: function (record) {
            _mapContainer = document.getElementById('OpenLayers_Map_9_OpenLayers_ViewPort');

            var lang = OpenLayers.Lang.getCode();
            _iconCls = 'fullscreen-icon';
            _qtip = record.get("description")[lang];

            this.item = new Ext.menu.Item({
                text: record.get("title")[lang],
                hidden: !this.options.showintoolmenu,
                iconCls: _iconCls,
                qtip: _qtip,
                handler: _fullScreen,
                scope: this
            });

            this.activateTool();
            return this.item;
        },

        destroy: function () {
            this.map = null;
            this.toolbar.remove(this.action.items[0]);
            this.toolbar.remove(this.toolbar.items.items[this.options.position]);
            this.options = null;
        }
    }
})();