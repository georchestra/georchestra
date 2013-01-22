Ext.namespace("GEOR.Addons");


// TODO: change "module pattern" -> "class"

GEOR.Addons.Magnifier = (function () {

    var map, options, control;

    var onCheckchange = function(item, checked) {
        if (checked) {
            control = new OpenLayers.Control.Magnifier(options);
            map.addControl(control);
            control.update();
        } else {
            control.destroy();
        }
    };
    
    return {

        init: function(m, o) {
            map = m;
            options = o;

            return new Ext.menu.CheckItem({
                text: OpenLayers.i18n("magnifier"),
                group: "measure",
                iconCls: "addon-magnifier",
                listeners: {
                    "checkchange": onCheckchange
                }
            });
            
        }
    }
})();
