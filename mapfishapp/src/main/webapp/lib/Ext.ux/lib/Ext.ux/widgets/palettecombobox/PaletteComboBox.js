Ext.namespace('Ext.ux');

/**
 *
 * Ext.ux.PaletteComboBox Class
 *
 * Example usage :
 *
 * <br>
 * <pre><code>
var palettes = [
    [0, ["#FF0000", "#FF4400", "#FF8800", "#FFCC00"]],
    [1, ["#00FF00", "#00FF44", "#00FF88", "#00FFCC"]]
];

var combo = new Ext.ux.PaletteComboBox({
    fieldLabel:'IconCombo',
    store: new Ext.data.SimpleStore({
            fields: ['value', 'colors'],
            data: palettes
    }),
    valueField: 'value',
    triggerAction: 'all',
    mode: 'local'
});
</pre></code>

<pre><code>
<style type="text/css">

.x-combo-palette-item {
    padding: 2px !important;
}
.x-combo-palette-input-value {
    position: absolute;
    top: 0;
    left: 0;
    overflow: hidden;
    height:18px;
    padding: 3px;
}

.x-combo-palette {
    position: relative;
}

.x-combo-palette div {
    float: left;
    -moz-outline: 0 none;
    outline: 0 none;
    padding: 1px;
}

.x-combo-palette div span {
    display: block;
    height: 14px;
    line-height: 14px;
    width: 14px;
}
</style>
</pre></code>
 */

Ext.ux.PaletteComboBox = Ext.extend(Ext.form.ComboBox, {

    cls: 'x-combo-palette',

    initComponent:function() {

        var paletteTpl = '<div class="x-combo-palette">'
                +      '<tpl for="colors">'
                +        '<div><span style="background-color:{.}" unselectable="on">&#160;</span></div>'
                +       '</tpl>'
                +   '</div>';

        Ext.apply(this, {
            paletteTpl: paletteTpl,

            tpl:  '<tpl for=".">'
                + '<div class="x-combo-list-item x-combo-palette-item">'
                +    paletteTpl
                + '</div></tpl>'
        });

        // call parent initComponent
        Ext.ux.PaletteComboBox.superclass.initComponent.call(this);

    },

    onRender:function(ct, position) {
        // call parent onRender
        Ext.ux.PaletteComboBox.superclass.onRender.call(this, ct, position);

        // adjust styles
        this.wrap.applyStyles({position:'relative'});

        this.el.addClass('ux-icon-combo-input');

        // add div for icon
        var el = Ext.DomHelper.append(this.el.up('div.x-form-field-wrap'), {
            tag: 'div', cls: 'x-combo-palette-input-value',
            style: 'width:' + this.el.getWidth(true) + 'px'
        });
        this.icon = Ext.DomHelper.append(el, {
            tag: 'div', style:'width:500px'
        });
    },

    setIconCls:function() {
        var rec = this.store.query(this.valueField, this.getValue()).itemAt(0);
        if(rec) {
            var t = new Ext.XTemplate(this.paletteTpl);
            var palette = {colors: rec.get('colors')};
            t.overwrite(this.icon, palette);
        }
    },
    
    assertValue: function() {
        // does nothing in override
        // this is to prevent a call to clearValue() - because there's no displayField
    },

    setValue: function(value) {
        Ext.ux.PaletteComboBox.superclass.setValue.call(this, value);
        
        this.setIconCls();

        // don't show the color value in the input
        this.el.dom.value = "";
    },

    onResize: function(w, h) {
        Ext.ux.PaletteComboBox.superclass.onResize.apply(this, arguments);

        // resize the icon div's parent based on the input field's
        // new size
        Ext.fly(this.icon).up("div.x-combo-palette-input-value").setWidth(
            this.el.getWidth(true)
        );
    }
});

Ext.reg('ext.ux.palettecombo', Ext.ux.PaletteComboBox);

