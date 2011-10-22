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
 * @include GeoExt.data.CSW.js
 * @include GEOR.config.js
 * @include GEOR.waiter.js
 * @include OpenLayers/Format/JSON.js
 */

Ext.namespace("GEOR");

GEOR.dataview = (function() {

    var store = null;
    
    var dataView = null;
    
    var OWSdb = {};
    
    var form, jsonFormat;
        
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
            case 'OGC:WMS-1.1.1-http-get-map':
                if (URI.value) {
                    OWSdb[id] = URI;
                    var html = "<b>Visualiser</b> ";
                    if (URI.name) {
                        // we have a layer
                        html += "la couche WMS <b>"+(URI.description || URI.name)+"</b>";
                    } else  {
                        // we have a service
                        html += "le service WMS <b>"+(URI.description || URI.value)+"</b>";
                    }
                    view.push('<button class="x-list-btn" id="'+id+'">'+html+'</button>');
                }
                break;
            /*
            case '':
            case 'image/png':
            case 'WWW:DOWNLOAD-1.0-http--download':
                if (URI[i].value) {
                    OWSdb[id] = URI[i];
                    dl.push('<button class="x-list-btn" id="'+id+'">Télécharger la donnée '+URI.name+'</button>');
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
        return (bbox instanceof OpenLayers.Bounds ? ' - <a href="#'+uuid+'" class="zoom">zoom</a>' : '');
    };
    
    
    var getTemplate = function() {
        return [
            '<tpl for=".">',
                '<div class="x-view-item">',
                    '<p><b>{title}</b>{[this.zoom(values)]}</p>',
                    '<p>{abstract}</p>',
                    '{[this.buttons(values.URI)]}',
                    /*'Mots clés : ',
                    '<tpl for="subject">',
                        '&nbsp;{value} ',
                    '</tpl>',*/
                '</div>',
            '</tpl>'
        ].join('');
    };
    
    var submitData = function(o) {
        form = form || Ext.DomHelper.append(Ext.getBody(), {
            tag: "form",
            action: GEOR.config.VIEWER_URL,
            target: "_blank",
            method: "post"
        });
        var input = form[0] || Ext.DomHelper.append(form, {
            tag: "input",
            type: "hidden",
            name: "data"
        });
        jsonFormat = jsonFormat || new OpenLayers.Format.JSON();
        input.value = jsonFormat.write(o);
        form.submit();
    };
    
    
    var onButtonClick = function(evt, elt) {
        if (!OWSdb[elt.id]) {
            return;
        }
        var services = [], layers = [];
        if (OWSdb[elt.id].name) {
            layers.push({
                layername: OWSdb[elt.id].name,
                metadataURL:"",
                owstype:"WMS",
                owsurl: OWSdb[elt.id].value
            });
        } else {
            services.push({
                text: "test serveur",
                metadataURL:"",
                owstype:"WMS",
                owsurl: OWSdb[elt.id].value
            });
        }
        submitData({services: services, layers: layers});
    };
    
    var onZoomClick = function(e, t) {
        // TODO: change event name to zoom
        var uuid = t.href.slice(t.href.indexOf('#')+1);
        var r = store.getById(uuid);
        if (r) {
            GEOR.observable.fireEvent("itemzoom", {
                record: r
            });
        }
    };
    
    var onStoreLoad = function(s) {
        Ext.select('.x-list-btn').on('click', onButtonClick);
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
        alert("Oops, il y a eu un problème.");
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
                    //store: store, // do not specify store right now, or contentEL will be overwritten.
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
