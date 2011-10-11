/////////////////// GeoExt.data.CSWRecordsReader
Ext.namespace("GeoExt.data");
GeoExt.data.CSWRecordsReader = function(meta, recordType) {
    meta = meta || {};
    if(!meta.format) {
        meta.format = new OpenLayers.Format.CSWGetRecords();
    }
    if(typeof recordType !== "function") {
        recordType = GeoExt.data.LayerRecord.create(
            recordType || meta.fields || [
                {name: "audience"},
                {name: "contributor"},
                {name: "coverage"},
                {name: "creator"},
                {name: "date"},
                {name: "description"},
                {name: "format"},
                {name: "identifier"},
                {name: "language"},
                {name: "provenance"},
                {name: "publisher"},
                {name: "relation"},
                {name: "rights"},
                {name: "rightsHolder"},
                {name: "source"},
                {name: "subject"},
                {name: "title"},
                {name: "type"},
                {name: "URI"},
                {name: "abstract"},
                {name: "modified"},
                {name: "spatial"},
                {name: "BoundingBox"}
            ]
        );
    }
    GeoExt.data.CSWRecordsReader.superclass.constructor.call(
        this, meta, recordType
    );
};

Ext.extend(GeoExt.data.CSWRecordsReader, Ext.data.DataReader, {
    /** private: method[read]
     *  :param request: ``Object`` The XHR object which contains the parsed XML
     *      document.
     *  :return: ``Object`` A data block which is used by an ``Ext.data.Store``
     *      as a cache of ``Ext.data.Record`` objects.
     */
    read: function(request) {
        var data = request.responseXML;
        if(!data || !data.documentElement) {
            data = request.responseText;
        }
        return this.readRecords(data);
    },
    
    readers: {
        "title": function(r) {
            var o = [];
            for (var i=0, l=r.length; i<l; i++) {
                o.push(r[i].value);
            }
            return o.join(' / ');
        },
        "abstract": function(r) {
            return (r ? r.join(' / ') : "pas d'abstract");
        },
        "bounds": function(b) {
            if (!b || !(b instanceof OpenLayers.Bounds)) {
                return;
            }
            b.transform(new OpenLayers.Projection("EPSG:4326"),
                new OpenLayers.Projection("EPSG:900913"));
            return b.toGeometry();
        }
    },
    
    parseField: function(fieldname, value) {
        if (this.readers[fieldname]) {
            return this.readers[fieldname](value);
        } else {
            return value;
        }
    },

    /** private: method[readRecords]
     *  :param data: ``DOMElement | String | Object`` A document element or XHR
     *      response string.  As an alternative to fetching capabilities data
     *      from a remote source, an object representing the capabilities can
     *      be provided given that the structure mirrors that returned from the
     *      capabilities parser.
     *  :return: ``Object`` A data block which is used by an ``Ext.data.Store``
     *      as a cache of ``Ext.data.Record`` objects.
     *  
     *  Create a data block containing Ext.data.Records from an XML document.
     */
    readRecords: function(data) {
        if(typeof data === "string" || data.nodeType) {
            data = this.meta.format.read(data);
        }
        if (!!data.error) {
            throw new Ext.data.DataReader.Error("invalid-response", data.error);
        }
        
        var records = [], rs, r;
        
        if (data.records) {
            rs = data.records;
            var fields = this.recordType.prototype.fields; 
            for(var i=0, l=rs.length; i<l; i++){
                r = rs[i];
                //console.log(r);
                if(r.title && r.title instanceof Array && r.title[0]) {
                    values = {};
                    for(var j=0, lj=fields.length; j<lj; j++) {
                        field = fields.items[j];
                        v = this.parseField(field.name, (r[field.mapping || field.name])) ||
                            field.defaultValue;
                        v = field.convert(v);
                        values[field.name] = v;
                    }

                    records.push(new this.recordType(values));
                }
                
                
            }
        }
        
        return {
            totalRecords: data.SearchResults.numberOfRecordsMatched,
            success: true,
            records: records
        };
    }
});


/////////////////// GeoExt.data.CSWRecordsStore
Ext.namespace("GeoExt.data");
GeoExt.data.CSWRecordsStore = function(c) {
    c = c || {};
    GeoExt.data.CSWRecordsStore.superclass.constructor.call(
        this,
        Ext.apply(c, {
            proxy: c.proxy || (!c.data ?
                new Ext.data.HttpProxy({url: c.url, method: "POST"}) : 
                undefined
            ),
            reader: new GeoExt.data.CSWRecordsReader(
                c, c.fields
            )
        })
    );
};
Ext.extend(GeoExt.data.CSWRecordsStore, Ext.data.Store);

