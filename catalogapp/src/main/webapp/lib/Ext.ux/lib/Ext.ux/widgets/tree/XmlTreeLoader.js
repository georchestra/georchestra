Ext.namespace('Ext.ux.tree');

Ext.ux.tree.XmlTreeLoader = Ext.extend(Ext.tree.TreeLoader, {

    requestData : function(node, callback){
        if(this.fireEvent("beforeload", this, node, callback) !== false){
            this.transId = Ext.Ajax.request({
                method:this.requestMethod,
                url: this.dataUrl||this.url,
                success: this.handleResponse,
                failure: this.handleFailure,
                scope: this,
                argument: {callback: callback, node: node},
                xmlData: this.parseInput(this, node)
            });
        }else{
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if(typeof callback == "function"){
                callback();
            }
        }
    },

    processResponse : function(response, node, callback){
        var xml = response.responseText;
        try {
            var o = this.parseOutput(this, xml);
            node.beginUpdate();
            if(node.store && node.store.loadData) {
                node.store.loadData(o);
            }
            else {
                for(var i = 0, len = o.length; i < len; i++){
                    var n = this.createNode(o[i]);
                    if(n){
                        node.appendChild(n);
                    }
                }
            }
            node.endUpdate();
            if(typeof callback == "function"){
                callback(this, node);
            }
        }catch(e){
            this.handleFailure(response);
        }
    }

});

