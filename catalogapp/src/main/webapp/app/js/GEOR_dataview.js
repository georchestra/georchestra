/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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
 * @include GeoExt.data.CSW.js
 * @include GEOR_config.js
 * @include GEOR_waiter.js
 * @include OpenLayers/Format/JSON.js
 */

Ext.namespace("GEOR");

GEOR.dataview = (function() {

    var tr = OpenLayers.i18n;

    var store = null;

    var dataView = null;

    var OWSdb = {};

    var form = {}, jsonFormat;

    var selectedRecordsId = [];

    var createButtons = function(URIs) {
        if (!URIs || !URIs[0]) {
            return '';
        }
        var id, dl = [], view = [], URI;
        for (var i=0,l=URIs.length;i<l;i++) {
            id = OpenLayers.Util.createUniqueID('OWS_');
            URI = URIs[i];
            switch (URI.protocol) {
            case 'OGC:WMS':
            case 'OGC:WMS-1.0.0-http-get-map':
            case 'OGC:WMS-1.1.0-http-get-map':
            case 'OGC:WMS-1.1.1-http-get-map':
            case 'OGC:WMS-1.3.0-http-get-map':
                if (URI.value) {
                    OWSdb[id] = URI;
                    if (URI.name) {
                        // we have a layer
                        view.push('<button class="x-list-btn-view" id="'+id+'">'
                                  +tr('View WMS layer', {NAME: (URI.description || URI.name)})
                                  +'</button>');
                        dl.push('<button class="x-list-btn-dl" id="'+id+'">'
                                +tr('Download WMS layer', {NAME: (URI.description || URI.name)})
                                +'</button>');
                    } else  {
                        // we have a service
                        view.push('<button class="x-list-btn-view" id="'+id+'">'
                                  +tr('View WMS service', {NAME: (URI.description || URI.value)})
                                  +'</button>');
                        dl.push('<button class="x-list-btn-dl" id="'+id+'">'
                                +tr('Download WMS service', {NAME: (URI.description || URI.value)})
                                +'</button>');
                    }
                }
                break;
            /*
            case '':
            case 'image/png':
            case 'WWW:DOWNLOAD-1.0-http--download':
                if (URI[i].value) {
                    OWSdb[id] = URI[i];
                    dl.push('<button class="x-list-btn" id="'+id+'">'
                            +tr('Download data', {NAME: URI.name})
                            +'</button>');
                }
                break;
            */
            }
        }
        return dl.join(' ')+view.join(' ');
    };

    var getZoomText = function(values) {
        var bbox = values.BoundingBox;
        var uuid = values.identifier;
        return (bbox instanceof OpenLayers.Bounds ? ' - <a href="#'+uuid+'" class="zoom">' + tr('zoom') + '</a>' : '');
    };


    var getTemplate = function() {
        return [
            '<tpl for=".">',
                '<div class="x-view-item">',
                    '<p><b>{title}</b> - <a href="/geonetwork/?uuid={identifier}" class="fullmd" target="_blank">' + tr('record') + '</a>',
                        '{[this.zoom(values)]}</p>',
                    '<p>{abstract}</p>',
                    '{[this.buttons(values.URI)]}',
                    /*tr('Keywords: '),
                    '<tpl for="subject">',
                        '&nbsp;{value} ',
                    '</tpl>',*/
                '</div>',
            '</tpl>'
        ].join('');
    };

    var submitData = function(url_key, o) {
        form[url_key] = form[url_key] || Ext.DomHelper.append(Ext.getBody(), {
            tag: "form",
            action: GEOR.config[url_key], //url_key can be one of VIEWER_URL or EXTRACTOR_URL
            target: "_blank",
            method: "post"
        });
        var input = form[url_key][0] || Ext.DomHelper.append(form[url_key], {
            tag: "input",
            type: "hidden",
            name: "data"
        });
        jsonFormat = jsonFormat || new OpenLayers.Format.JSON();
        input.value = jsonFormat.write(o);
        form[url_key].submit();
    };


    var onButtonClick = function(evt, elt) {
        elt = Ext.get(elt);
        if (!elt.is('button')) {
            elt = elt.parent('button');
        }
        if (!OWSdb[elt.id]) {
            return;
        }
        var url_key = (elt.hasClass('x-list-btn-view')) ?
            'VIEWER_URL' : 'EXTRACTOR_URL';
        var services = [], layers = [];
        if (OWSdb[elt.id].name) {
            layers.push({
                layername: OWSdb[elt.id].name,
                metadataURL:"", // FIXME
                owstype:"WMS", // What about WFS ?
                owsurl: OWSdb[elt.id].value
            });
        } else {
            services.push({
                text: tr("Server NAME", {'NAME': OWSdb[elt.id].value}),
                metadataURL:"", // FIXME
                owstype:"WMS",
                owsurl: OWSdb[elt.id].value
            });
        }
        submitData(url_key, {services: services, layers: layers});
    };

    var getRecordFromHref = function(href) {
        var uuid = href.slice(href.indexOf('#')+1);
        return store.getById(uuid);
    };

    var onZoomClick = function(e, t) {
        // TODO: change event name to zoom
        var r = getRecordFromHref(t.href);
        if (r) {
            GEOR.observable.fireEvent("itemzoom", {
                record: r
            });
        }
    };

    var onStoreLoad = function(s) {
        Ext.select('button.x-list-btn-view').on('click', onButtonClick);
        Ext.select('button.x-list-btn-dl').on('click', onButtonClick);
        Ext.select('.x-view-item a.zoom').on('click', onZoomClick);
        GEOR.waiter.hide();
        // we need to restore selection of items referenced in selectedRecords
        if (selectedRecordsId.length) {
            dataView.select(store.queryBy(function(r, id) {
                return (selectedRecordsId.indexOf(id) > -1);
            }).getRange(), true, true);
        }
        GEOR.observable.fireEvent("storeloaded", {store: s});
    };


    var onStoreBeforeload = function() {
        // local db reset
        OWSdb = {};
    };

    var onStoreException = function() {
        GEOR.waiter.hide();
        alert(tr("Oops, a problem occured."));
    };


    return {

        init: function(s) {
            if (!store) {
                store = new GeoExt.data.CSWRecordsStore({
                    url: GEOR.config.GEONETWORK_URL + '/csw', //'content.xml', //
                    listeners: {
                        "beforeload": onStoreBeforeload,
                        "load": onStoreLoad,
                        "exception": onStoreException
                    }
                });
            }
            return store;
        },

        bind: function(store) {
            if (!dataView.store) {
                dataView.bindStore(store, true);
            }
        },


        getCmp: function() {
            if (!dataView) {
                dataView = new Ext.DataView({
                    //store: store, // do not specify store right now, or contentEl will be overwritten.
                    singleSelect: null,
                    //multiSelect: true,
                    selectedClass: 'x-view-selected',
                    //simpleSelect: true,
                    cls: 'x-list',
                    overClass:'x-view-over',
                    itemSelector: 'div.x-view-item',
                    autoScroll: true,
                    autoWidth: true,
                    contentEl: "dataview-contentel",
                    //trackOver: true,
                    autoHeight: true,
                    tpl: new Ext.XTemplate(getTemplate(), {
                        buttons: createButtons,
                        zoom: getZoomText
                    }),
                    listeners: {
                        "click": function(dv, idx, node) {
                            var selectedRecords = dv.getSelectedRecords();
                            var length = selectedRecords.length;
                            selectedRecordsId = new Array(length);
                            for (var i=0,l=length;i<l;i++) {
                                selectedRecordsId[i] = selectedRecords[i].get('identifier');
                                // TODO: keep a local cache of selectedRecords not selectedRecordsId
                            }
                            GEOR.observable.fireEvent("itemselectionchanged", {
                                records: selectedRecords,
                                total: selectedRecordsId.length
                            });
                        }
                    }
                });
            }
            return dataView;
        },

        scrollToTop: function() {
            var el = dataView.getEl();
            var f = el && el.first();
            f && f.scrollIntoView(dataView.container);
            // FIXME: there are still 10 or 20 pixels left at the top
        }


    };
})();
