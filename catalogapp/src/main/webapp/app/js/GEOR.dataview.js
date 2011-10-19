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
        
    var createButtons = function(URIs) {
        if (!URIs || !URIs[0]) {
            return '';
        }
        var id, dl = [], view = [], URI;
        for (var i=0,l=URIs.length;i<l;i++) {
            id = OpenLayers.Util.createUniqueID('OWS_');
            URI = URIs[i];
            //console.log(URI.protocol);
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
    
    
    var getTemplate = function() {
        return [
            '<tpl for=".">',
                '<div class="x-view-item">',
                    '<p><b>{title}</b></p>',
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
        console.log(elt.id);
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
    
    var onStoreLoad = function(s) {
        Ext.select('.x-list-btn').on('click', onButtonClick);
        GEOR.waiter.hide();

        GEOR.observable.fireEvent("storeloaded", {store: s});
    };
    
    
    var onStoreBeforeload = function() {
        // local db reset
        OWSdb = {};
    };
    
    var onStoreException = function() {
        GEOR.waiter.hide();
        //console.log(arguments);
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
        
        
        getCmp: function() {
            if (!dataView) {
                dataView = new Ext.DataView({
                    store: store,
                    singleSelect: true,
                    //emptyText: 'No records to display',
                    overClass:'x-view-over',
                    cls: 'x-list',
                    autoScroll: true,
                    autoWidth: true,
                    //contentEl: "dataview", // FIXME: I cannot make it appear
                    //trackOver: true,
                    autoHeight: true,
                    tpl: new Ext.XTemplate(getTemplate(), {
                        buttons: createButtons
                    })
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
