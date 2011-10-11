/**
 * @class Ext.ux.menu.ColorMenu
 * @extends Ext.menu.Menu
 * This class makes Ext.ux.ColorPicker available as a menu.
 * @license: BSD
 * @author: Robert B. Williams (extjs id: vtswingkid)
 * @author: Tobias Uhlig (extjs id: tobiu)
 * @author: Jerome Carbou (extjs id: jcarbou)
 * @constructor
 * Creates a new ColorMenu
 * @param {Object} config Configuration options
 * @version 1.1.3
 */

Ext.namespace("Ext.ux.menu", "Ext.ux.form");

Ext.ux.menu.ColorMenu = Ext.extend(Ext.menu.Menu, {
	enableScrolling : false,
	hideOnClick     : true,
	initComponent : function(){
		Ext.apply(this, {
			plain         : true,
			showSeparator : false,
			items: this.picker = new Ext.ux.ColorPicker(this.initialConfig)
		});
		Ext.ux.menu.ColorMenu.superclass.initComponent.call(this);
		this.relayEvents(this.picker, ['select']);
		this.on('select', this.menuHide, this);
		if (this.handler) {
			this.on('select', this.handler, this.scope || this)
		}
	},
	menuHide: function(){
		if (this.hideOnClick) {
			this.hide(true);
		}
	}
});
