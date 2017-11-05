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
 * @include OpenLayers/Map.js
 * @include OpenLayers/Control/Navigation.js
 * @include OpenLayers/Control/Attribution.js
 * @include OpenLayers/Filter/Spatial.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Projection.js
 * @include OpenLayers/Layer/SphericalMercator.js
 * @include OpenLayers/Layer/XYZ.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/StyleMap.js
 * @include OpenLayers/Style.js
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Handler/RegularPolygon.js
 * @include OpenLayers/Renderer/SVG.js
 * @include OpenLayers/Renderer/VML.js
 * @include GeoExt/widgets/MapPanel.js
 */

Ext.namespace("GEOR");

GEOR.where = (function() {

    var map;
    var extentLayer, vectorLayer;
    var epsg4326, epsg900913;
    var searchExtent;


    var createVectorLayer = function() {
        vectorLayer = new OpenLayers.Layer.Vector('bounds', {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillOpacity: 0,
                    strokeColor: "#ee9900",
                    strokeWidth: 2,
                    strokeOpacity: 0.4
                }),
                "select": new OpenLayers.Style({
                    fillOpacity: 0,
                    strokeColor: "blue",
                    strokeWidth: 2,
                    strokeOpacity: 1,
                    graphicZIndex: 1000
                })
            }),
            rendererOptions: {
                zIndexing: true
            },
            getFeatureByAttributeId: function(value) {
                var feature = null;
                for(var i=0, len=this.features.length; i<len; ++i) {
                    if(this.features[i].attributes['id'] == value) {
                        feature = this.features[i];
                        break;
                    }
                }
                return feature;
            }
        });
        map && map.addLayer(vectorLayer);
    };


    var onMapMoveend = function() {
        if (!extentLayer) {
            return;
        }
        var b = map.getExtent().clone();
        if (searchExtent) {
            // A search extent is currently defined.
            // We have to keep it if current searchExtent matches the feature attribute,
            // else destroy it and create a new one with current search extent.
            var f = extentLayer.features[0];
            if (f.attributes.bbox !== searchExtent.toString()) {
                extentLayer.destroyFeatures();
                extentLayer.addFeatures([
                    new OpenLayers.Feature.Vector(
                        searchExtent.toGeometry(), {
                            "bbox": searchExtent.toString()
                        }
                    )
                ]);
            }
        } else {
            // drop existing feature, create a new one with current map extent
            extentLayer.destroyFeatures();
            extentLayer.addFeatures([
                new OpenLayers.Feature.Vector(
                    b.toGeometry(), {
                        "bbox": b.toString()
                    }
                )
            ]);
        }
    };


    var createMap = function() {
        if (!epsg900913) {
            epsg900913 = new OpenLayers.Projection("EPSG:900913");
        }
        var map = new OpenLayers.Map({
            controls: [
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.Attribution()
            ],
            theme: null,
            projection: epsg900913,
            layers: [
                new OpenLayers.Layer.OSM('OSM', 'https://a.tile.openstreetmap.org/${z}/${x}/${y}.png', {
                    buffer: 0,
                    attribution: "Carte CC-By-SA <a href='http://openstreetmap.org/' target='_blank'>OpenStreetMap</a>"
                })
            ],
            eventListeners: {
                "moveend": onMapMoveend
            }
        });
        map.zoomToMaxExtent();
        // the layer which will display the search extent:
        extentLayer = new OpenLayers.Layer.Vector('searchextent', {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "#000000",
                    fillOpacity: 0,
                    strokeColor: "#ff0000",
                    strokeDashstyle: "dash",
                    strokeWidth: 2,
                    strokeOpacity: 1
                })
            })
        });
        map.addLayer(extentLayer);
        return map;
    };

    return {

        getCmp: function() {
            if (map) {
                return Ext.getCmp('geor-map');
            }
            map = createMap();
            return {
                xtype: "gx_mappanel",
                id: 'geor-map',
                stateful: false,
                map: map
            };
        },

        // TODO: inclusion stricte ?
        getFilter: function() {
            var filter = null;
            if (!epsg4326) {
                epsg4326 = new OpenLayers.Projection("EPSG:4326");
            }
            var bounds = map.getExtent();
            // when getFilter is called, we want to keep a state with the bbox being searched:
            searchExtent = bounds.clone();
            filter = new OpenLayers.Filter.Spatial({
                type: OpenLayers.Filter.Spatial.BBOX,
                property: "ows:BoundingBox",
                value: bounds.clone().transform(epsg900913, epsg4326)
            });
            return filter;
        },

        display: function(records) {
            if (!vectorLayer) {
                createVectorLayer();
            } else {
                vectorLayer.destroyFeatures();
            }

            var record, bounds, features = [], geom;
            for (var i=0, l=records.length; i<l; i++) {
                record = records[i];
                bounds = record.get('BoundingBox');
                if (bounds) {
                    geom = bounds.transform(epsg4326, epsg900913).toGeometry();
                    features.push(new OpenLayers.Feature.Vector(geom, {
                        "id": record.get('identifier')
                    }));
                }
            }
            vectorLayer.addFeatures(features);
            var dataExtent = vectorLayer.getDataExtent();
            if (map && dataExtent && dataExtent.getWidth() && dataExtent.getHeight()) {
                map.zoomToExtent(vectorLayer.getDataExtent());
            }
        },

        highlight: function(records) {
            if (!vectorLayer) {
                return;
            }
            var featureId, feature;
            for (var i=0, l=vectorLayer.features.length; i<l; i++) {
                vectorLayer.drawFeature(vectorLayer.features[i], "default");
            }
            for (var i=0, l=records.length; i<l; i++) {
                featureId = records[i].get('identifier');
                feature = vectorLayer.getFeatureByAttributeId(featureId);
                feature && vectorLayer.drawFeature(feature, "select");
            }
        },

        zoomTo: function(record) {
            if (!vectorLayer) {
                return;
            }
            var featureId = record.get('identifier');
            if (featureId) {
                var feature = vectorLayer.getFeatureByAttributeId(featureId);
                feature && map && map.zoomToExtent(feature.geometry.getBounds().scale(1.1));
                //feature && vectorLayer.drawFeature(feature, "select");
            }
        },

        reset: function() {
            searchExtent = null;
            extentLayer.destroyFeatures();
            map && map.zoomToMaxExtent();
        }
    };
})();
