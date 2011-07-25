/*
 * Copyright (C) 2009  Camptocamp
 *
 * This file is part of GeoBretagne
 *
 * MapFish Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoBretagne is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBretagne.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @include lang/fr.js
 * @include GEOB_proj4jsdefs.js
 * @include GEOB_data.js
 * @include GEOB_map.js
 * @include GEOB_ajaxglobal.js
 * @include GEOB_waiter.js
 * @include GEOB_toolbar.js
 * @include GEOB_layerstree.js
 * @include GEOB_layeroptions.js
 * @include GEOB_referentials.js
 * @include GeoExt/widgets/MapPanel.js
 */

Ext.namespace("GEOB");

(function() {

    var email = null;

    /**
     * Handler for extract all checked layers button.
     */
    var extractHandler = function(b) {
        email = email || GEOB.data.email;
        var emailRegexp = /^([\w\-\'\-]+)(\.[\w-\'\-]+)*@([\w\-]+\.){1,5}([A-Za-z]){2,4}$/;

        if (emailRegexp.test(email)) {
            GEOB.layerstree.extract(email, b);
        } else {
            // prompt for valid email and process the extraction using a callback
            Ext.Msg.prompt('Email', 'Saisissez une adresse email valide : ', function(btn, text){
                if (btn == 'ok'){
                    if (emailRegexp.test(text)) {
                        email = text;
                        GEOB.layerstree.extract(email, b);
                    }
                    else {
                        GEOB.util.errorDialog({
                            msg: "L'email n'est pas valide. Abandon de l'extraction."
                        });
                    }
                }
            });
        }
    };

    Ext.onReady(function() {

        /*
         * Setting of OpenLayers global vars.
         */

        OpenLayers.Lang.setCode('fr');
        OpenLayers.Number.thousandsSeparator = " ";
        OpenLayers.ImgPath = 'resources/app/img/openlayers/';
        OpenLayers.DOTS_PER_INCH = 25.4 / 0.28;
        OpenLayers.IMAGE_RELOAD_ATTEMPTS = 5;

        /*
         * Setting of Ext global vars.
         */

        Ext.BLANK_IMAGE_URL = "resources/lib/externals/ext/resources/images/default/s.gif";
        Ext.apply(Ext.MessageBox.buttonText, {
            yes: "Oui",
            no: "Non",
            ok: "OK",
            cancel: "Annuler"
        });
        
        /*
         * Initialize the application.
         */

        GEOB.ajaxglobal.init();
        GEOB.waiter.init();
        var map = GEOB.map.create();
        var vectorLayer = GEOB.map.createVectorLayer();

        /*
         * Create the page's layout.
         */

        // the viewport
        new Ext.Viewport({
            layout: "border",
            items: [{
                region: "center",
                layout: "border",
                title: "Paramètres de l'extraction",
                defaults: {
                    defaults: {
                        border: false
                    },
                    border: false
                },
                items: [
                    GEOB.layeroptions.create(map, {
                        region: "north",
                        vectorLayer: vectorLayer,
                        height: 105
                    }),
                    {
                        region: "center",
                        xtype: "gx_mappanel",
                        id: "mappanel",
                        map: map,
                        tbar: GEOB.toolbar.create(map)
                    }
                ]
            }, {
                region: "west",
                border: false,
                width: 300,
                minWidth: 200,
                maxWidth: 400,
                split: true,
                collapseMode: 'mini',
                layout: "border",
                items: [{
                    region: "north",
                    html: ["Configurez les paramètres généraux de votre extraction en utilisant le panneau ci-contre à droite (affiché en sélectionnant 'Paramètres par défaut').", "Vous pouvez ensuite lancer l'extraction en cliquant sur le bouton 'Extraire les couches cochées'.", "Si vous souhaitez préciser des paramètres d'extraction spécifiques pour une couche donnée, sélectionnez la dans l'arbre ci-dessous."].join('<br/><br/>'),
                    bodyCssClass: 'paneltext',
                    height: 170,
                    autoScroll: true,
                    title: 'Extracteur',
                    collapsible: true,
                    split: true
                }, {
                    xtype: 'tabpanel',
                    activeTab: 0,
                    region: 'center',
                    defaults: {
                        defaults: {
                            border: false
                        },
                        border: false
                    },                    
                    items: [{
                        region: "center",
                        layout: "fit",
                        title: "Configuration",
                        items: GEOB.layerstree.create(),
                        bbar: [ '->',
                            {
                                id: "geob-btn-extract-id",
                                text: "Extraire les couches cochées",
                                iconCls: "geob-btn-extract",
                                handler: function() {
                                    extractHandler(this);
                                }
                            }
                        ]
                    }, {
                        region: "south",
                        layout:"border",
                        title: "Recentrage",
                        border: false,
                        defaults: {
                            border: false
                        },
                        items: [
                            Ext.apply({
                                height: 150,
                                region: 'north'
                            }, GEOB.referentials.create(map, "geob_loc")),
                            {
                                xtype: 'container',
                                autoEl: 'div',
                                cls: 'x-panel-body x-panel-body-noborder',
                                html: ' ',
                                region: 'center'
                            }
                        ]
                    }]
                }]
            }]
        });
        
        var saveLayerOptions = function() {
            GEOB.layerstree.saveExportOptions(GEOB.layeroptions.getOptions());
        };
        
        GEOB.layerstree.events.on({
            "beforelayerchange": saveLayerOptions,
            "beforeextract": saveLayerOptions,
            "layerchange": GEOB.layeroptions.setOptions
        });
        GEOB.referentials.events.on({
            "recenter": GEOB.layeroptions.setBbox
        });

        GEOB.layerstree.init(map, vectorLayer);
    });
})();
