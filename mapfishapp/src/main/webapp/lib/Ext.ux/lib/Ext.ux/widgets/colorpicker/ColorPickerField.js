/**
 * @class Ext.ux.form.ColorPickerField
 * @extends Ext.form.TwinTriggerField
 * This class makes Ext.ux.ColorPicker available as a form field.
 * @license: BSD
 * @author: Robert B. Williams (extjs id: vtswingkid)
 * @author: Tobias Uhlig (extjs id: tobiu)
 * @author: Jerome Carbou (extjs id: jcarbou)
 * @constructor
 * Creates a new ColorPickerField
 * @param {Object} config Configuration options
 * @version 1.1.3
 */
Ext.ux.form.ColorPickerField = Ext.extend(Ext.form.TwinTriggerField,  {
	
	editMode : 'picker',
	
	// private
    onResize : function(w, h){
        Ext.ux.form.ColorPickerField.superclass.onResize.apply(this, arguments);
		if (this.hideHtmlCode && this.colorMask) {
			this.colorMask.setBox(this.createColorMaskBox());
		}
    },
	
	// private
    onRender : function(ct, position){
		Ext.ux.form.ColorPickerField.superclass.onRender.apply(this, arguments);
		
		if (this.hideHtmlCode) {
			this.colorMask = this.el.createProxy('x-form-colorfield-colorMask',null,true);			
			this.colorMask.setBox(this.createColorMaskBox());			
		}
		this.updateElColor();
    },
	
	// private
	updateElColor : function(){
		if (this.el) {
			var v = this.getValue();
			var bg = 'ffffff';
			var fg = 'ffffff';
	        if (v) {
				alphaRgb = this.splitAphaRgbHex(v.replace('#', ''));
				bg = alphaRgb.rgbHex;
	            fg = (this.hideHtmlCode) ? bg : this.rgbToHex(this.invert(this.hexToRgb(bg)));
			}
			this.el.applyStyles('background: #' + bg + ';color:#' + fg);
			if (this.colorMask) {
				this.colorMask.applyStyles('background: #' + bg + ';');
			}
		}
	},
	
	// private
	createColorMaskBox : function(){
		var b = this.el.getBox();
		b.x+=2;
		b[0]+=2;
		b.y+=2;
		b[1]+=2;
		b.width-=3;
		b.height-=4;
		b.right -= 1;
    	b.bottom -= 2;
		return b;
	},
	
    initComponent : function(){
		this.editable = !this.hideHtmlCode;
		this.selectOnFocus = !(this.hideHtmlCode || !this.selectOnFocus);
        Ext.ux.form.ColorPickerField.superclass.initComponent.apply(this, arguments);
		this.picker=-1;
		switch (this.editMode){
			case 'picker' :
				this.trigger1Class='x-form-colorfield-picker';
				this.triggerConfig = {
		            tag:'span', cls:'x-form-twin-triggers', cn:[
		            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger1Class}
		        ]};		
				this.picker=0;		
				break;
			case 'palette' :
				this.trigger1Class='x-form-colorfield-palette';
				this.triggerConfig = {
		            tag:'span', cls:'x-form-twin-triggers', cn:[
		            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger1Class}
		        ]};
				this.palette=0;
				break;
			default :
				this.trigger1Class='x-form-colorfield-palette';
				this.trigger2Class='x-form-colorfield-picker';
				this.triggerConfig = {
		            tag:'span', cls:'x-form-twin-triggers', cn:[
		            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger1Class},
					{tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger2Class}
		        ]};
				this.palette=0;
				this.picker=1;	
		}
		
		this.menus =[];
		if (this.palette>=0) {
			this.menus[this.palette] = new Ext.menu.ColorMenu({
            listeners : {
                select: this.onSelect
                ,scope : this
            }
        	});
		}
		
		if (this.picker>=0) {			
			this.menus[this.picker] = new Ext.ux.menu.ColorMenu({
				opacity:this.opacity,
	            listeners : {
	                select: this.onSelect
	                ,scope : this
	            }
	        });
		}
    }
	
	// private
	,onSelect : function(m, c){
		this.setValue(c);
        this.focus.defer(10, this);
    }
	
	,focus : function(selectText, delay){
		Ext.ux.form.ColorPickerField.superclass.focus.call(this, selectText && !this.hideHtmlCode, delay);
	}
	
	,setValue : function(v){		
		if (v) {
			v = v.replace('#', '').toUpperCase();
			var alphaRgb = this.splitAphaRgbHex(v);
			if (this.opacity && alphaRgb) {
				v=alphaRgb.alphaRgbHex;
			}
			v='#'+v;
		}
        Ext.ux.form.ColorPickerField.superclass.setValue.call(this, v);        
		this.updateElColor();			
    }
	
    ,onDestroy : function(){
		Ext.destroy(this.menus,this.wrap,this.colorMask);
        Ext.ux.form.ColorPickerField.superclass.onDestroy.apply(this, arguments);
    }
    ,onBlur : function(){
        Ext.ux.form.ColorPickerField.superclass.onBlur.apply(this, arguments);
        this.setValue(this.getValue());
    }
    ,onTrigger1Click : function(){
        if(this.disabled){
            return;
        }
        this.menus[0].show(this.el, "tl-bl?");
		if (this.picker==0) {
        	this.menus[0].picker.setColor(this.getValue());
		}
    }
	,onTrigger2Click : function(){
        if(this.disabled){
            return;
        }
        this.menus[1].show(this.el, "tl-bl?");
		if (this.picker==1) {
        	this.menus[1].picker.setColor(this.getValue());
		}
    }
	,hexToRgb: Ext.ux.ColorPicker.prototype.hexToRgb
	,rgbToHex: Ext.ux.ColorPicker.prototype.rgbToHex
	,decToHex: Ext.ux.ColorPicker.prototype.decToHex
	,hexToDec: Ext.ux.ColorPicker.prototype.hexToDec
	,getHCharPos: Ext.ux.ColorPicker.prototype.getHCharPos 
	,invert: Ext.ux.ColorPicker.prototype.invert
	,splitAphaRgbHex: Ext.ux.ColorPicker.prototype.splitAphaRgbHex
});
Ext.reg("colorpickerfield", Ext.ux.form.ColorPickerField);