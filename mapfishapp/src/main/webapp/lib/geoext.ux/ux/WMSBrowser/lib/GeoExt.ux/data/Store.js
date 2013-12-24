Ext.namespace("Ext.data.Store");

Ext.data.Store.prototype.getValueArray = function(value) {
    var aszValues = [];
    var nValues = this.getCount();

    for(var i=0; i<nValues; i++) {
        aszValues.push(this.getAt(i).get(value));
    }

    return aszValues;
};
