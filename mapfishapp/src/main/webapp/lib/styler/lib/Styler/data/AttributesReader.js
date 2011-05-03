/**
 * Copyright (c) 2008 The Open Planning Project
 */

Ext.namespace("Styler.data");
/**
 * Class: Styler.data.AttributesReader
 * Data reader class to provide an array of {Ext.data.Record} objects given
 *     a WFS DescribeFeatureType response for use by an {Ext.data.Store}
 *     object.
 *
 * Extends: Ext.data.DataReader
 */

/**
 * Constructor: Styler.data.AttributesReader
 * Create a new attributes reader object.
 *
 * Parameters:
 * meta - {Object} Reader configuration.
 * recordType - {Array | Ext.data.Record} An array of field configuration
 *     objects or a record object.
 *
 * Configuration options (meta properties):
 * format - {OpenLayers.Format} A parser for transforming the XHR response
 *     into an array of objects representing attributes.  Defaults to
 *     an {OpenLayers.Format.WFSDescribeFeatureType} parser.
 * ignore - {Object} Properties of the ignore object should be field names.
 *     Values are either arrays or regular expressions.
 */
Styler.data.AttributesReader = function(meta, recordType) {
    meta = meta || {};
    if(!meta.format) {
        meta.format = new OpenLayers.Format.WFSDescribeFeatureType();
    }
    Styler.data.AttributesReader.superclass.constructor.call(
        this, meta, recordType || meta.fields
    );
};

Ext.extend(Styler.data.AttributesReader, Ext.data.DataReader, {

    /**
     * Method: read
     * This method is only used by a DataProxy which has retrieved data from a
     *     remote server.
     *
     * Parameters:
     * request - {Object} The XHR object which contains the parsed XML
     *     document.
     * 
     * Returns:
     * {Object} A data block which is used by an {Ext.data.Store} as a cache
     *     of Ext.data.Records.
     */
    read: function(request) {
        var data = request.responseXML;
        if(!data || !data.documentElement) {
            data = request.responseText;
        }
        return this.readRecords(data);
    },
    
    /**
     * Method: readRecords
     * Create a data block containing Ext.data.Records from an XML document.
     *
     * Parameters:
     * data - {DOMElement | String | Array} A document element or XHR response
     *     string.  As an alternative to fetching attributes data from a remote
     *     source, an array of attribute objects can be provided given that
     *     the properties of each attribute object map to a provided field name.
     *
     * Returns:
     * {Object} A data block which is used by an {Ext.data.Store} as a cache of
     *     Ext.data.Records.
     */
    readRecords: function(data) {
        var attributes;
        if(data instanceof Array) {
            attributes = data;
        } else {
            // only works with one featureType in the doc
            attributes = this.meta.format.read(data).featureTypes[0].properties;
        }
    	var recordType = this.recordType;
        var fields = recordType.prototype.fields;
        var numFields = fields.length;
        var attr, values, name, record, ignore, matches, value, records = [];
        for(var i=0, len=attributes.length; i<len; ++i) {
            ignore = false;
            attr = attributes[i];
            values = {};
            for(var j=0; j<numFields; ++j) {
                name = fields.items[j].name;
                value = attr[name];
                if(this.meta.ignore && this.meta.ignore[name]) {
                    matches = this.meta.ignore[name];
                    if(typeof matches == "string") {
                        ignore = (matches === value);
                    } else if(matches instanceof Array) {
                        ignore = (matches.indexOf(value) > -1);
                    } else if (matches instanceof RegExp) {
                        ignore = (matches.test(value));
                    }
                    if(ignore) {
                        break;
                    }
                }
                values[name] = attr[name];
            }
            if(!ignore) {
                records[records.length] = new recordType(values);
            }
        }
        
        return {
            success: true,
            records: records,
            totalRecords: records.length
        };
        
    }

});