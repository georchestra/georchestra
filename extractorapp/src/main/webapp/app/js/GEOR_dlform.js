/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include GEOR_util.js
 * @include GEOR_layerstree.js
 */

Ext.namespace("GEOR");

GEOR.dlform = (function() {
    /*
     * Private
     */

    /**
     * Internationalization
     */
    var tr = OpenLayers.i18n;

    // Ext.Window
    var win;

    var createForm = function(options) {
        var ls = localStorage;

        var storeOptions = {
            autoLoad: true,
            reader: new Ext.data.JsonReader({
                root: 'rows',
                fields: ['id', 'name'],
                idProperty: 'id'
            })
        };
        if (GEOR.data.jettyrun) {
            // we are debugging the app with "mvn jetty:run"
            // we do not want to deploy dlform webapp to get this list
            // i18n: we let that strings hardcoded, as they are for debugging
            // purposes
            storeOptions.data = {
                "rows": [
                    {"id": 1, "name": "Administratif et budgétaire"},
                    {"id": 2, "name": "Aménagement du Territoire et Gestion de l'Espace"},
                    {"id": 3, "name": "Communication"},
                    {"id": 4, "name": "Environnement"},
                    {"id": 5, "name": "Fond de Plan"},
                    {"id": 6, "name": "Foncier et Urbanisme"},
                    {"id": 7, "name": "Formation"},
                    {"id": 8, "name": "Gestion du Domaine Public"},
                    {"id": 9, "name": "Mise en valeur du Territoire (Tourisme)"},
                    {"id": 10, "name": "Risques Naturels et Technologiques"}
                ]
            };
        } else {
            // use the dedicated dlform webservice.
            storeOptions.url = '/downloadform/data_usage';
        }

        var formPanelItems = [
        // hidden fields:
        {
            xtype: 'hidden',
            name: 'json_spec'
        },{
            xtype: 'hidden',
            name: 'sessionid',
            value: GEOR.util.getCookie('JSESSIONID')
        // regular fields:
        },{
            fieldLabel: tr("Firstname"),
            labelStyle: 'font-weight:bold;',
            name: 'first_name',
            value: GEOR.data.first_name || (ls && ls.getItem('first_name')) || '',
            allowBlank: false
        },{
            fieldLabel: tr("Lastname"),
            labelStyle: 'font-weight:bold;',
            name: 'last_name',
            value: GEOR.data.last_name || (ls && ls.getItem('last_name')) || '',
            allowBlank: false
        },{
            fieldLabel: tr("Company"),
            labelStyle: 'font-weight:bold;',
            value: GEOR.data.company || (ls && ls.getItem('company')) || '',
            name: 'company',
            allowBlank: false
        }, {
            fieldLabel: tr("Email"),
            labelStyle: 'font-weight:bold;',
            name: 'email',
            vtype: 'email',
            value: GEOR.data.email || (ls && ls.getItem('email')) || '',
            allowBlank: false
        }, {
            fieldLabel: tr("Phone"),
            value: GEOR.data.tel || (ls && ls.getItem('tel')) || '',
            name: 'tel'
        },
        // data use
        {
            xtype: 'multiselect',
            fieldLabel: tr("Applications"),
            labelStyle: 'font-weight:bold;',
            name: 'datause',
            height: 120,
            allowBlank: false,
            displayField: 'name',
            valueField: 'id',
            minSelections: 1,
            store: new Ext.data.Store(storeOptions)
        },
        // comment
        {
            xtype:'htmleditor',
            fieldLabel: tr("Comments"),
            name: 'comment',
            height: 150
        }, 
        // check box
        {
            xtype:'checkboxgroup',
            allowBlank: false,
            blankText: tr("dlform.blanktext"),
            columns: 1,
            items: [{
                boxLabel: tr("dlform.checkbox",
                        {"URL": GEOR.config.PDF_URL}),
                name: 'ok'
            }]
        }];

        return new Ext.FormPanel({
            region: 'center',
            labelWidth: 100,
            standardSubmit: false,
            monitorValid: true,
            bodyStyle:'padding:5px 5px 0',
            defaults: {
                width: 550
            },
            defaultType: 'textfield',
            labelSeparator: ' : ',
            items: formPanelItems,
            buttons: [{
                text: tr("OK"),
                formBind: true,
                handler: function() {
                    var fp = this.ownerCt.ownerCt,
                        form = fp.getForm();
                    if (form.isValid()) {
                        var v = form.getValues();

                        // save form fields in local storage if not connected.
                        if (ls) {
                            var fields = ['first_name', 'last_name', 'company', 'email', 'tel'];
                            if (GEOR.data.anonymous) {
                                GEOR.data.email = v['email']; // offer a last chance to change email
                                for (var i=0,l=fields.length;i<l;i++) {
                                    ls.setItem(fields[i], v[fields[i]]);
                                }
                            } else {
                                // clear values
                                for (var i=0,l=fields.length;i<l;i++) {
                                    ls.removeItem(fields[i]);
                                }
                            }
                        }

                        // set json_spec hidden field with given email
                        form.findField('json_spec').setRawValue(
                            Ext.encode(GEOR.layerstree.getSpec(v['email']))
                        );

                        var submitOptions = {
                            // requires dlform webapp to be deployed:
                            url: '/downloadform/extractorapp',
                            success: function() {
                                win.close();
                                options.callback.call();
                            }
                        };
                        // We do not want to block the app when running with jetty
                        // in this particular case, the success callback always gets executed
                        // Note: this is no security breach, since the extractorapp "initiate" controler
                        // always checks that the form has been validated before the job is done.
                        submitOptions.failure = (GEOR.data.jettyrun) ?
                            submitOptions.success : function(form, action) {
                            switch (action.failureType) {
                                case Ext.form.Action.CLIENT_INVALID:
                                    // should not happen, since we have formBind
                                    GEOR.util.errorDialog({
                                        msg: tr("Invalid form")
                                    });
                                    break;
                                case Ext.form.Action.CONNECT_FAILURE:
                                    GEOR.util.errorDialog({
                                        msg: tr("dlform.save.error")
                                    });
                                    break;
                                case Ext.form.Action.SERVER_INVALID:
                                    GEOR.util.errorDialog({
                                        msg: action.result.msg
                                    });
                            }
                        };
                        form.submit(submitOptions);
                    }
                }
            }]
        });
    };

    /*
     * Public
     */
    return {

        /**
         * APIMethod: show
         * displays the popup with form inside
         *
         * Parameters:
         * map - {Object} config options
         *
         */
        show: function(options) {
            Ext.QuickTips.init();

            win = new Ext.Window({
                title: tr("Take one minute to indicate how you use the data"),
                constrainHeader: true,
                layout: 'fit',
                border: false,
                width: 700,
                height: 540,
                closeAction: 'close',
                modal: true,
                items: [{
                    frame: true,
                    layout: 'border',
                    defaults: {
                        border: false,
                        frame: false
                    },
                    items: [{
                        region: "north",
                        bodyStyle: "padding:5px;",
                        html: tr("dlform.mandatory.fields")
                    }, createForm(options)]
                }]
            });
            win.show();
        }
    };
})();
