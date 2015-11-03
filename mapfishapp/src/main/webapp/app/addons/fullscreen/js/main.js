Ext.namespace("GEOR.Addons");

GEOR.Addons.Fullscreen = Ext.extend(GEOR.Addons.Base, {

    /**
     * Method: init
     *
     */
    init: function(record) {
        this.api = window.fullScreenApi;
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                // toggle mode enabled if toolbars = true
                enableToggle: !!this.options.toolbars,
                tooltip: this.getTooltip(record),
                iconCls: 'fullscreen-icon',
                listeners: {
                    "click": this.onClick,
                    "toggle": this.onToggle,
                    scope: this
                }
            });
            this.target.doLayout();
        } else {
            // addon placed in "tools menu"
            this.item = new Ext.menu.CheckItem({
                text: this.getText(record),
                qtip: this.getQtip(record),
                iconCls: 'fullscreen-icon',
                listeners: {
                    "click": this.onClick, // required to go true fullscreen 
                    "checkchange": this.onToggle, // required to go fullscreen with toolbars
                    scope: this
                }
            });
        }
        // we need to know when we're back to normal mode:
        Ext.EventManager.addListener(document, 'webkitfullscreenchange', this.onFullscreenChange.createDelegate(this));
        Ext.EventManager.addListener(document, 'mozfullscreenchange', this.onFullscreenChange.createDelegate(this));
        Ext.EventManager.addListener(document, 'MSFullscreenChange', this.onFullscreenChange.createDelegate(this));
        Ext.EventManager.addListener(document, 'fullscreenchange', this.onFullscreenChange.createDelegate(this));
        // detecting if viewer should start fullscreen: (?fullscreen=true)
        var o = GEOR.util.splitURL(window.location.href);
        if (o.params.FULLSCREEN == "true" && 
            // requestFullScreen can only be initiated by a user gesture:
            (this.options.toolbars == true || !this.api.supportsFullScreen)) {
            this.goFullscreen();
        }
    },

    /**
     * Method: onFullscreenChange
     * taking care of deactivating the CheckItem in the tools menu
     */
    onFullscreenChange: function() {
        var api = this.api,
            item = this.item;
        if (!item) {
            // we have components, not an item.
            return;
        }
        // we need a delayed task to detect if we're back to normal mode
        var task = new Ext.util.DelayedTask(function(){
            if (!api.isFullScreen()) {
                // this is a hack to overcome an Ext.menu.CheckItem limitation
                item.checked = false;
                item.el.up('li').removeClass('x-menu-item-checked');
            }
        });
        if (api.supportsFullScreen) {
            task.delay(500);
        }
    },

    /**
     * Method: goFullscreen
     *
     */
    goFullscreen: function() {
        var api = this.api;
        if (this.options.toolbars == true || !api.supportsFullScreen) {
            var mp = GeoExt.MapPanel.guess(); // TODO: improve this for 15.12, with https://github.com/georchestra/georchestra/issues/1006
            // put the mappanel up-front:
            mp.el.dom.style.zIndex = 9999;
            // (this is for those with drop down menus in header)
            var p = mp.ownerCt; 
            this.size = p.items.get(0).getSize();
            p.items.get(0).setSize(0, 0);
            p.items.get(p.items.getCount() - 2).collapse();
            p.doLayout();
        } else if (api.supportsFullScreen) {
            api.requestFullScreen(
                this.map.div.childNodes[0]
            );
        }
    },

    /**
     * Method: restoreLayout
     *
     */
    restoreLayout: function() {
        if (this.options.toolbars == true) {
            var mp = GeoExt.MapPanel.guess(); // TODO: improve this for 15.12, with https://github.com/georchestra/georchestra/issues/1006
            // put the mappanel back in place:
            mp.el.dom.style.zIndex = 1;
            var p = mp.ownerCt;
            p.items.get(0).setSize(this.size);
            p.items.get(p.items.getCount() - 2).expand();
            p.doLayout();
        }
    },

    /**
     * Method: onClick
     *
     */
    onClick: function(btn) {
        // if we have a toggle button or 
        if (btn.enableToggle || btn.checked !== undefined) {
            // handled by the onToggle method.
            return;
        }
        this.goFullscreen();
    },

    /**
     * Method: onToggle
     *
     */
    onToggle: function(btn, pressed) {
        if (pressed) {
            // go fullscreen with toolbars
            this.goFullscreen();
        } else {
            // come back to normal display
            this.restoreLayout();
        }
    },

    /**
     * Method: destroy
     *
     */
    destroy: function() {
        GEOR.Addons.Base.prototype.destroy.call(this);
    }
});