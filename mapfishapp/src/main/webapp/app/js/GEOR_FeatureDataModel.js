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

Ext.namespace('GEOR');

/*
 * Class: GEOR.FeatureDataModel is meant to represent
 * features' data model (or attributes) and interact with it
 * (mainly export it to different formats)
 */

GEOR.FeatureDataModel = function(options) {
    this.dataModel = {};
    if (options.features) {
        this.fromFeatures(options.features);
    } else if (options.attributeStore) {
        this.fromAttributeStore(options.attributeStore);
    }
};

GEOR.FeatureDataModel.prototype = {

    /**
     * Property: dummyAttributeName
     * {String} Fake attribute name, used to reference objects in the grid panel
     */
    dummyAttributeName: OpenLayers.i18n('objects'),

    /**
     * Method: fromFeatures
     *
     * Parameters:
     * features - {Array(OpenLayers.Feature.Vector)} an array of features
     */
    fromFeatures: function(features) {
        if (!(features instanceof Array)) {
            features = [features];
        }
        var attributes, type;
        for (var i=0, len=features.length; i<len; i++) {
            attributes = features[i].attributes;
            for (var key in attributes) {
                if (!attributes.hasOwnProperty(key)) {
                    continue;
                }
                type = this.guessType(attributes[key]);
                if (!this.dataModel[key]) {
                    this.dataModel[key] = {
                        name: key,
                        type: type,
                        header: key, // sanitize column name (replace _ by space, etc.)?
                        width: 150   // guess it from longer attributes received?
                    };
                } else if (this.dataModel[key].type != type) {
                    this.dataModel[key].type = 'string';
                }
            }
        }
        if (this.isEmpty()) {
            this.dataModel[this.dummyAttributeName] = {
                name: this.dummyAttributeName,
                type: 'string',
                header: this.dummyAttributeName
            };
        }
    },

    /**
     * Method: fromAttributeStore
     *
     * Parameters:
     * store - {GeoExt.data.AttributeStore}
     */
    fromAttributeStore: function(store) {
        var name, type;
        store.each(function(record) {
            name = record.get('name');
            switch (record.get('type').replace(/xsd:/,'')) {
                case 'int':
                case 'short':
                case 'byte':
                case 'integer':
                case 'long':
                    type = 'int';
                    break;
                case 'double':
                case 'float':
                case 'decimal':
                    type = 'float';
                    break;
                case 'date':
                case 'dateTime':
                    type = 'date';
                    break;
                default:
                    type = 'string';
            }
            this.dataModel[name] = {
                name: name,
                type: type,
                header: name, // sanitize column name (replace _ by space, etc.) ?
                width: 150
            };
        }, this);
    },

    /**
     * Method: guessType
     *
     * Parameters:
     * model - {Mixed} input data to test for type
     *
     * Returns:
     * {String} the data type: one of 'float', 'int', 'string', 'date'.
     *          Defaults to 'string' if no match.
     */
    guessType: function(input) {
        if (/^\d+$/.test(input) && ! /^0/.test(input)) {
            // second test is required to prevent postal codes 
            // like 02100 to be interpreted as integers 
            // (thus truncated by ExtJS)
            // see https://github.com/camptocamp/georchestra-geopicardie-configuration/issues/215
            return 'int';
        }
        if (Ext.isDate(input)) {
            return 'date';
        }
        if (input) {
            var str = input.toString();
            var pointPos = str.lastIndexOf(".");
            if ((pointPos != -1) && (this.guessType(str.substring(0,pointPos)) == 'int') && (this.guessType(str.substring(pointPos+1)) == 'int')) {
                return 'float';
            }
        }
        return 'string';
    },

    /**
     * Method: getFields
     *
     * Returns:
     * {Array} the data model column names
     */
    getFields: function() {
        var cols = [], dataModel = this.dataModel;
        for (var key in dataModel) {
            if (!dataModel.hasOwnProperty(key)) {
                continue;
            }
            cols.push(key);
        }
        return cols;
    },

    /**
     * Method: toStoreFields
     *
     * Returns:
     * {Array} the data store fields config
     */
    toStoreFields: function() {
        var storeFields = [], dataModel = this.dataModel, field;
        for (var key in dataModel) {
            if (!dataModel.hasOwnProperty(key)) {
                continue;
            }
            field = dataModel[key];
            storeFields.push({
                name: field.name,
                type: field.type
            });
        }
        return storeFields;
    },

    /**
     * Method: toColumnModel
     *
     * Returns:
     * {Array} the column model config
     */
    toColumnModel: function(options) {
        options = options || {};
        var columnModel = [], dataModel = this.dataModel, field;
        for (var key in dataModel) {
            if (!dataModel.hasOwnProperty(key)) {
                continue;
            }
            field = dataModel[key];
            columnModel.push(
                Ext.apply({}, {
                    header: field.header,
                    dataIndex: field.name,
                    width: field.width
                }, options)
            );
        }
        return columnModel;
    },

    /**
     * Method: isEmpty
     *
     * Returns:
     * {Boolean} Is the data model empty ?
     */
    isEmpty: function() {
        for (var key in this.dataModel) {
            if (!this.dataModel.hasOwnProperty(key) || key == this.dummyAttributeName) {
                continue;
            }
            return false;
        }
        return true;
    }
};
