Ext.namespace("GEOR.Addons");

GEOR.Addons.Quicksearch = Ext.extend(GEOR.Addons.Base, {

    /**
     * Method: init
     *
     * Parameters:
     * record - {Ext.data.record} a record with the addon parameters
     */
    init: function(record) {
        if (!this.target) {
            alert("QuickSearch addon config error: requires a target property !");
            return;
        }
        // addon always placed in a toolbar
        this.components = this.target.insertButton(this.position, ['-', {
            xtype: 'combo',
            typeAhead: true,
            hideTrigger: true,
            selectOnFocus: true,
            mode: 'local',
            store: new Ext.data.ArrayStore({
                fields: [
                    'id',
                    'label'
                ],
                data: [[1, 'item1'], [2, 'item2']]
            }),
            displayField: 'label',
            emptyText: "Quick search...",
            listeners: {
                "render": function(c) {
                    new Ext.ToolTip({
                        target: c.getEl(),
                        html: OpenLayers.i18n("addon_qs_qtip")
                    });
                }
            }
        }, '-']);
        this.target.doLayout();
    }
});