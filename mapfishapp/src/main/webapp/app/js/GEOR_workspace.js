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
 * @include OpenLayers/Request/XMLHttpRequest.js
 * @include GEOR_wmc.js
 * @include GEOR_waiter.js
 * @include GEOR_util.js
 */

Ext.namespace("GEOR");

GEOR.workspace = (function() {
    /*
     * Private
     */

    /**
     * Method: saveBtnHandler
     * Handler for the button triggering the WMC save dialog
     */
    var saveBtnHandler = function() {
        var formPanel = this.ownerCt;
        GEOR.waiter.show();
        OpenLayers.Request.POST({
            url: "ws/wmc/",
            data: GEOR.wmc.write({
                id: formPanel.getForm().findField('filename').getValue()
            }),
            success: function(response) {
                formPanel.ownerCt.close();
                var o = Ext.decode(response.responseText);
                window.location.href = o.filepath;
            },
            scope: this
        });
    };

    /**
     * Method: loadBtnHandler
     * Handler for the button triggering the WMC loading
     */
    var loadBtnHandler = function() {
        var formPanel = this.ownerCt;
        formPanel.getForm().submit({
            url: "ws/wmc/", 
            // Beware: form submission requires a *success* parameter in json response
            // As said in http://extjs.com/learn/Manual:RESTful_Web_Services
            // "Ext.form.BasicForm hopefully becomes HTTP Status Code aware!"
            success: function(form, action) {
                formPanel.ownerCt.close();
                var o = Ext.decode(action.response.responseText);
                // GET WMC content
                GEOR.waiter.show();
                OpenLayers.Request.GET({
                    url: o.filepath, 
                    success: function(response) {
                        try {
                            GEOR.wmc.read(response.responseText);
                        } catch(err) {
                            GEOR.util.errorDialog({
                                msg: "Le contexte n'est pas valide."
                            });
                        }
                    }
                });
            },
            failure: function(form,action) {
                formPanel.ownerCt.close();
            },
            scope: this
        });
    };
    
    /**
     * Method: cancelBtnHandler
     * Handler for the cancel button
     */
    var cancelBtnHandler = function() {
        this.ownerCt.ownerCt.close();
    };
    
    /**
     * Method: saveWMC
     * Triggers the save dialog.
     */
    var saveWMC = function() {
        var popup = new Ext.Window({
            title: 'Sauvegarde du contexte',
            layout: 'fit',
            modal: false,
            width: 400,
            height: 120,
            closeAction: 'close',
            plain: true,
            items: [{
                xtype: 'form',
                bodyStyle: 'padding:5px',
                labelWidth: 80,
                monitorValid: true,
                buttonAlign: 'right',
                items: [{
                    xtype: 'textfield',
                    name: 'filename',
                    width: 200,
                    fieldLabel: "Nom",
                    allowBlank: false,
                    blankText: "Un nom du fichier est nécessaire."
                }],
                buttons: [{
                    text: "Annuler",
                    handler: cancelBtnHandler
                },{
                    text: "Sauvegarder",
                    handler: saveBtnHandler,
                    formBind: true
                }]
            }]
        });
        popup.show();
    };
    
    /**
     * Method: loadWMC
     * Triggers the upload dialog and restores the context.
     */
    var loadWMC = function() {
        var popup = new Ext.Window({
            title: "Restauration d'un contexte",
            layout: 'fit',
            modal: false,
            width: 400,
            height: 120,
            closeAction: 'close',
            plain: true,
            items: [{
                xtype: 'form',
                fileUpload: true,
                bodyStyle: 'padding:5px',
                labelWidth: 80,
                monitorValid: true,
                buttonAlign: 'right',
                html: ["<p>Notez que le fichier de contexte",
                        " doit être encodé en UTF-8.</p>"].join(""),
                items: [{
                    xtype: 'textfield',
                    inputType: 'file',
                    name: 'wmc',
                    fieldLabel: "Fichier",
                    allowBlank: false,
                    blankText: "Un fichier est nécessaire."
                }],
                buttons: [{
                    text: "Annuler",
                    handler: cancelBtnHandler
                },{
                    text: "Charger",
                    handler: loadBtnHandler,
                    formBind: true
                }]
            }]
        });
        popup.show();
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: create
         * Returns the workspace menu config.
         *
         * Returns:
         * {Object} The toolbar config item corresponding to the "workspace" menu.
         */
        create: function() {
            return {
                text: "Espace de travail",
                menu: new Ext.menu.Menu({
                    defaultAlign: "tr-br",
                    // does not work as expected, at least with FF3 ... (ExtJS bug ?)
                    // top right corner of menu should be aligned with bottom right corner of its parent
                    items: [{
                        text: "Sauvegarder la carte",
                        iconCls: "geor-save-map",
                        handler: saveWMC
                    },{
                        text: "Charger une carte",
                        iconCls: "geor-load-map",
                        handler: loadWMC
                    }]
                })
            };
        }
    };
})();
