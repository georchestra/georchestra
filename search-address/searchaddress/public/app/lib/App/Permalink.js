/**
 * @requires GeoExt/state/PermalinkProvider.js
 */

Ext.namespace('App');

/**
 * Constructor: App.Permalink
 * Creates an {Ext.Action} that opens a window displaying the map permalink.
 *
 * Parameters:
 * options - {Object} Options passed to the {Ext.Action}.
 */
App.Permalink = function(options) {

    // Private

    /**
     * Property: permalinkTextField
     * {Ext.form.TextField} The permalink text field.
     */
    var permalinkTextField = null;

    /**
     * Property: permalinkWindow
     * {Ext.Window} The permalink window.
     */
    var permalinkWindow = null;

    /**
     * Method: showPermalink
     * Handler of the {Ext.Action}.
     */
    var showPermalink = function() {
        permalinkWindow.show();
    };

    // Public

    Ext.apply(this, {

        /**
         * APIProperty: action
         * {Ext.Action} The permalink action. Read-only.
         */
        action: null
    });

    // Main

    permalinkTextField = new Ext.form.TextField({
        hideLabel: true,
        autoHeight: true,
        listeners: {
            'focus': function() {
                this.selectText();
            }
        }
    });

    permalinkWindow = new Ext.Window({
        layout: 'fit',
        renderTo: Ext.getBody(),
        width: 400,
        closeAction: 'hide',
        plain: true,
        title: OpenLayers.i18n('Permalink.title'),
        items: permalinkTextField,
        buttons: [{
            text: OpenLayers.i18n('Permalink.openlink'),
            handler: function() {
                window.open(permalinkTextField.getValue());
                permalinkWindow.hide();
            }
        }, {
            text: OpenLayers.i18n('close'),
            handler: function() {
                permalinkWindow.hide();
            }
        }]
    });
    // Registers a statechange listener to update the value
    // of the permalink text field.
    Ext.state.Manager.getProvider().on({
        statechange: function(provider) {
            permalinkTextField.setValue(provider.getLink());
        }
    });

    options = Ext.apply({
        allowDepress: false,
        iconCls: 'permalink',
        handler: showPermalink
    }, options);
    
    this.action = new Ext.Action(options);
};

/**
 * Creates the permalink provider.
 */
Ext.state.Manager.setProvider(
    new GeoExt.state.PermalinkProvider({encodeType: false})
);
