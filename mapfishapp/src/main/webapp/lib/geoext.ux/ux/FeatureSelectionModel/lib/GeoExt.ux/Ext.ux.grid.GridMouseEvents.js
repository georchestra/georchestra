Ext.ns("Ext.ux.grid");
Ext.ux.grid.GridMouseEvents = (function() {

    function onMouseOver(e) {
        processMouseOver.call(this, 'mouseenter', e);
    }

    function onMouseOut(e) {
        processMouseOver.call(this, 'mouseleave', e);
    }

    function processMouseOver(name, e){
        var t = e.getTarget(),
            r = e.getRelatedTarget(),
            v = this.view,
            header = v.findHeaderIndex(t),
            hdEl, row, rowEl, cell, fromCell, col;
        if(header !== false){
            hdEl = Ext.get(v.getHeaderCell(header));
            if (!((hdEl.dom === r) || hdEl.contains(r))) {
                this.fireEvent('header' + name, this, header, e, hdEl);
            }
        }else{
            row = v.findRowIndex(t);
            if(row !== false) {
                rowEl = Ext.get(v.getRow(row));
                if (!((rowEl.dom === r) || rowEl.contains(r))) {
                    this.fireEvent('row' + name, this, row, e, rowEl);
                }
                cell = v.findCellIndex(t);
                if(cell !== false){
                    cellEl = Ext.get(v.getCell(row, cell));
                    if (!((cellEl.dom === r) || cellEl.contains(r))){
                        this.fireEvent('cell' + name, this, row, cell, e, cellEl);
                    }
                    if (cell != v.findCellIndex(r)) {
                        col = this.colModel.getColumnAt(cell);
                        if (col.processEvent) {
                            col.processEvent(name, e, this, row, cell);
                        }
                        this.fireEvent('column' + name, this, cell, e);
                    }
                }
            }
        }
    }

    function onGridRender() {
        this.mon(this.getGridEl(), {
            scope: this,
            mouseover: onMouseOver,
            mouseout: onMouseOut
        });
    }

    return {
        init: function(g) {
            g.onRender = g.onRender.createSequence(onGridRender);
        }
    };
})();