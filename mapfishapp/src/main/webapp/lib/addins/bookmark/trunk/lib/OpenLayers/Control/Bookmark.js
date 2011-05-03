/* Copyright (c) 2006-2008 MetaCarta, Inc., published under the Clear BSD
 * license.  See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license. */

/**
 * @requires OpenLayers/Control.js
 */

/**
 * Class: OpenLayers.Control.Bookmark
 *     A bookmark view control.  Store an array of locations
 *     refrenced by a label.
 *
 * Inherits from:
 *  - <OpenLayers.Control>
 */
OpenLayers.Control.Bookmark = OpenLayers.Class(OpenLayers.Control, {

    /**
     * Property: type
     * {String}
     */
    type: OpenLayers.Control.TYPE_TOOL,

    /**
     * Property: stackState
     * {Boolean} Flag to indicate if the control needs to be redrawn. We have
     *     this in order to avoid unnecessarily redrawing the control.
     */
    stackState: true,

    /**
     * APIProperty: limit
     * {Integer} Optional limit on the number of bookmark items to retain.
     *           Default is 50. 0 is no limit.
     */
    limit: 50,

    /**
     * APIProperty: stack
     * {Array} Array of items in the list.
     */
    stack: null,

    /**
     * APIProperty: allowRemove
     * {Boolean}  Allow the bookmark to be removed.
     *      Default to true.
     */
    allowRemove: true,

    // DOM Elements

    /**
     * Property: contentDiv
     * {DOMElement}
     */
    contentDiv: null,

    /**
     * Property: dataLblDiv
     * {DOMElement}
     */
    dataLblDiv: null,

    /**
     * Property: dataElementDiv
     * {DOMElement}
     */
    dataElementDiv: null,

    /**
     * Property: minimizeDiv
     * {DOMElement}
     */
    minimizeDiv: null,

    /**
     * Property: maximizeDiv
     * {DOMElement}
     */
    maximizeDiv: null,

    /**
     * Constructor: OpenLayers.Control.Bookmark
     *
     * Parameters:
     * options - {func} An optional object whose properties will be used
     *     to extend the control.
     */
    initialize: function(options) {
        OpenLayers.Control.prototype.initialize.apply(this, [options]);
        this.initStack();
    },

    /**
     * APIMethod: destroy
     * Destroy the control.
     */
    /* FIXME  - not sure i'm stopping observation correctly*/
    destroy: function() {
        this.stack=null;
        OpenLayers.Event.stopObservingElement(this.div);
        OpenLayers.Event.stopObservingElement(this.minimizeDiv);
        OpenLayers.Event.stopObservingElement(this.maximizeDiv);
        OpenLayers.Control.prototype.destroy.apply(this);
    },

    /**
     * Method: initStack
     * Initialize the stack and get ready for adds.
     */
    initStack: function() {
      this.stack = [];
      this.stackState = true;
    },

    /**
     * APIMethod: add
     * Add an item to the stack as label, bounds.  If no label is specified
     *      "NONAME" is used.
     *
     * Parameters:
     * label - {String} Label reference to this bookmark
     * bounds - {Object or String}
     *      Object is a OpenLayers.bounds object or
     *      String representation of bounds in format of
     *     "-180,-180,180,180"
     *
     * Returns:
     * {Boolean} False if limit reached. True if added successfully.
     */
    add: function(label, bounds, func, param) {
      if(this.stack.length == this.limit && this.limit != 0) {
         return false;  // limit reached
      }
      func = (typeof func == "undefined") ? function() {} : func;
      param = (typeof param == "undefined" || param.length == 0) ? "" : param;
      label = (label == "undefined" || label.length == 0) ? "NONAME" : label;
      this.stack.push({ "label":label.toUpperCase(),
                        "bounds":bounds,
                        "func": func,
                        "param": param,
                        "id": this.stack.length });
      this.stackState = true;
      this.redraw();
      return true;
    },

    /**
     * APIMethod: clear
     * Clear and reset the stack then redraw the div.
     */
    clear: function() {
      this.initStack();
      this.redraw();
    },

    /**
     * APIMethod: remove
     * Remove item from the stack.
     *
     * Parameters:
     * label - {String} Remove from the stack referenced by label.
     *
     * Returns:
     * {Boolean} False if not found or not allowed to remove.
     */
    /* FIXME  - not sure i'm stopping observation correctly*/
    remove: function(label) {
      if(!this.allowRemove) {
         return false;
      }
      label = label.toUpperCase();
      for(var i=0; i<this.stack.length; i++) {
         if( this.stack[i].label.toUpperCase() == label) {
            for( var ii=0; ii<this.dataElementDiv.childNodes.length; ii++) {
               var currentNode = this.dataElementDiv.childNodes[ii].id;
               var currentLabel = label.replace(/ /g,'_');
               if(currentNode.replace(/ /g,'_') == "Remove_"+currentLabel) {
                  OpenLayers.Event.stopObservingElement(
                     this.dataElementDiv.childNodes[ii]);
               }
               if(currentNode.replace(/ /g,'_') == "Link_"+currentLabel) {
                  OpenLayers.Event.stopObservingElement(
                     this.dataElementDiv.childNodes[ii]);
               }
            }
            this.stack.splice( i, 1 );
            this.stackState = true;
            this.redraw();
            return true;
         }
      }
      return false;
    },

    /**
     * APIMethod: find
     * Find the first regex matched label and return it.
     *
     * Parameters:
     * label - {String}  Case insensitive string to find.
     *
     * Returns:
     * {Boolean} Return array of label(s) if found. False if not found
     */
    find: function(label) {
      var rt = [];
      var regEx = new RegExp (label, "i");
      for(var i=0; i<this.stack.length; i++) {
         if(regEx.test(this.stack[i].label)) {
            rt.push([this.stack[i].id,this.stack[i].label]);
            //return this.stack[i].label; // return first found
         }
      }
      return (rt.length == 0 ? false : rt);
    },

    /**
     * APIMethod: zoomToId
     * Zoom to extents referenced by id
     *
     * Parameters:
     * id - {Integer} stack id reference
     *
     * Returns:
     * {Boolean} False if not found
     */
    zoomToId: function(id) {
       var found=false;
       if( this.stack[id] != this.stack[id].id ) {
         if( typeof this.stack[id].bounds == "object" ) {
            this.map.zoomToExtent( this.stack[id].bounds );
         } else {
            this.map.zoomToExtent( new OpenLayers.Bounds.fromString(
                                       this.stack[id].bounds) );
         }
         this.stack[id].func(this.stack[id].param);
         found=true;
       } else {
         // index is misaligned, search the stack for the correct id.
         for( var i=0; i<this.stack.length; i++ ) {
            if( this.stack[i].id == id ) {
               if( typeof this.stack[i].bounds == "object" ) {
                  this.map.zoomToExtent( this.stack[i].bounds );
               } else {
                  this.map.zoomToExtent( new OpenLayers.Bounds.fromString(
                                             this.stack[i].bounds) );
               }
               this.stack[i].func(this.stack[i].param);
               found=true;
               break;  // break out of for when found
            }
         }
       }
       return found;
    },

    /**
     * APIMethod: zoomTo (deprecated, use zoomToLabel)
     * Zoom to extents referenced by label
     *
     * Parameters:
     * label - {String} Label to zoom to.
     *
     * Returns:
     * {Boolean} False if not found
     */
    zoomTo: function(label) {
      OpenLayers.Console.log("zoomTo() deprecated. Use zoomToLabel");
      return this.zoomToLabel(label);
    },

    /**
     * APIMethod: zoomToLabel
     * Zoom to extents referenced by label
     *
     * Parameters:
     * label - {String} Label to zoom to.
     *
     * Returns:
     * {Boolean} False if not found
     */
    zoomToLabel: function(label) {
      if( label != false ) {
         for(var i=0; i<this.stack.length; i++) {
            if( this.stack[i].label.toUpperCase() == label.toUpperCase() ) {
               if( typeof this.stack[i].bounds == "object" ) {
                  this.map.zoomToExtent( this.stack[i].bounds );
               } else {
                  this.map.zoomToExtent( new OpenLayers.Bounds.fromString(
                                          this.stack[i].bounds) );
               }
               this.stack[i].func(this.stack[i].param);
               return true;
            }
         }
      }
      return false;
    },

    /**
     * APIMethod: sortStack
     * Sort stack by sortMethod
     *
     * Parameters:
     * sortMethod - {String} Field to sort on.
     */
    sortStack: function(sortMethod) {
      switch(sortMethod.toLowerCase()) {
         case "id":
            this.stack.sort(this.sortById);
            break;
         case "label":
         default:
            this.stack.sort(this.sortByLabel);
            break;
      }
      this.stackState = true;
    },

    /**
     * Method: sortByLabel
     * Sort stack by label values. Called by sort().
     */
    sortByLabel:  function(a, b) {
         var x = a.label.toLowerCase();
         var y = b.label.toLowerCase();
         return ((x < y) ? -1 : ((x > y) ? 1 : 0));
      },

    /**
     * Method: sortById
     * Sort stack by id values. Called by sort().
     */
    sortById:  function(a, b) {
         var x = a.id;
         var y = b.id;
         return ((x < y) ? -1 : ((x > y) ? 1 : 0));
      },

    /**
     * APIMethod: reindex
     * Reindex stack; realign id with stack index. Usually called
     * after sortStack, add or remove.
     */
    reindex: function() {
       for( var i=0; i<this.stack.length; i++ ) {
          this.stack[i].id = i;
       }
       this.stackState = true;
    },

    /**
     * APIMethod: maximizeControl
     * Set up the labels and divs for the control
     *
     * Parameters:
     * e - {Event}
     */
    maximizeControl: function(e) {
        //HACK HACK HACK - find a way to auto-size this
        this.div.style.width = "13em";
        this.div.style.height = "";

        this.showControls(false);

        if (e != null) {
            OpenLayers.Event.stop(e);
        }
    },

    /**
     * APIMethod: minimizeControl
     * Hide all the contents of the control, shrink the size,
     *     add the maximize icon
     *
     * Parameters:
     * e - {Event}
     */
    minimizeControl: function(e) {
        this.div.style.width = "0px";
        this.div.style.height = "0px";

        this.showControls(true);

        if (e != null) {
            OpenLayers.Event.stop(e);
        }
    },

    /**
     * Method: draw
     * Create a div with stack values
     *
     * Returns:
     * {DOMElement} A reference to the DIV DOMElement containing the control
     */
    draw: function() {

        OpenLayers.Control.prototype.draw.apply(this);

        // create layout divs
        this.loadContents();

        // set mode to minimize
        if(!this.outsideViewport) {
            this.minimizeControl();
        }

        // populate div with current info
        this.redraw();

        return this.div;
    },

    /**
     * Method: loadContents
     * Set up the labels and divs for the control
     */
    loadContents: function() {
         //configure main div
         this.div.id = ( this.div.id == ""
                       ? "olControlBookmark"
                       : this.div.id );
         this.div.className = "olControlBookmark";
         OpenLayers.Event.observe(this.div, "mouseup",
            OpenLayers.Function.bindAsEventListener(this.mouseUp, this));
         OpenLayers.Event.observe(this.div, "click", this.ignoreEvent);
         OpenLayers.Event.observe(this.div, "mousedown",
            OpenLayers.Function.bindAsEventListener(this.mouseDown, this));
         OpenLayers.Event.observe(this.div, "dblclick", this.ignoreEvent);

         // layers list div
         this.contentDiv = document.createElement("div");
         this.contentDiv.id = this.displayClass + 'Content';
         this.contentDiv.className = this.displayClass + 'Content';

         this.dataLblDiv = document.createElement("div");
         this.dataLblDiv.id = this.displayClass + 'Title';
         this.dataLblDiv.className = this.displayClass + 'Title';
         this.dataLblDiv.innerHTML = ( this.title == ""
                                     ? "Bookmarks"
                                     : this.title );
        this.contentDiv.appendChild(this.dataLblDiv);

        this.dataElementDiv = document.createElement("div");
        this.dataElementDiv.id = this.displayClass + 'Elements';
        this.dataElementDiv.className = this.displayClass + 'Elements';

        this.contentDiv.appendChild(this.dataElementDiv);
        this.div.appendChild(this.contentDiv);

        // maximimze button
        this.maximizeDiv = document.createElement("div");
        this.maximizeDiv.id = this.div.id+'MaximizeDiv';
        this.maximizeDiv.className = this.displayClass + 'MaximizeButton';
        OpenLayers.Event.observe(this.maximizeDiv, "click",
           OpenLayers.Function.bindAsEventListener(this.maximizeControl, this)
        );
        this.div.appendChild(this.maximizeDiv);

        // minimimze button
        this.minimizeDiv = document.createElement("div");
        this.minimizeDiv.id = this.div.id+'MinimizeDiv';
        this.minimizeDiv.className = this.displayClass + 'MinimizeButton';
        OpenLayers.Event.observe(this.minimizeDiv, "click",
           OpenLayers.Function.bindAsEventListener(this.minimizeControl, this)
        );
        this.div.appendChild(this.minimizeDiv);

    },

    /**
     * Method: clearStackDiv
     * Clear the div containing the stack elements.
     */
    /* FIXME  - not sure i'm stopping observation correctly*/
    clearStackDiv: function() {
      // if no dataEmentDiv drop out gracfully
      if( this.dataElementDiv == null ) {
         return;
      }

      if( this.dataElementDiv != null ) {
         for(var i=0; i<this.dataElementDiv.childNodes; i++) {
            OpenLayers.Event.stopObservingElement(
                              this.dataElementDiv.childNodes[i]);
         }
         this.dataElementDiv.innerHTML = "";
      }
    },

    /**
     * Method: redraw
     * Takes the current state of the control and rebuilds if needed.
     *
     * Returns:
     * {DOMElement} A reference to the DIV DOMElement containing the control
     */
    redraw: function() {

        // only redraw the div if the state has changed, otherwise return
        // same div.
        if (!this.stackState) {
           return this.div;
        }

        this.clearStackDiv();

        // if no dataEmentDiv drop out gracfully
        if( this.dataElementDiv == null ) {
            return;
        }

        for(var i=0; i<this.stack.length; i++) {

            var bookmarkRow = document.createElement("div");
            bookmarkRow.className = this.displayClass + 'Row';

            if(this.allowRemove) {

               var removeSpan = document.createElement("span");
               removeSpan.id = this.displayClass + 'Remove_' +
                                       this.stack[i].label.replace(/ /g,'_');
               removeSpan.className = this.displayClass + 'Remove';

               var context = { 'bookmark': this,
                               'label': this.stack[i].label,
                               'action': "remove"
                             };

               OpenLayers.Event.observe(removeSpan, "click",
                 OpenLayers.Function.bindAsEventListener(this.onBookmarkClick,
                                                         context)
               );

               bookmarkRow.appendChild(removeSpan);

            }

            var labelSpan = document.createElement("span");
            labelSpan.id = this.displayClass + 'Link_' +
                                this.stack[i].label.replace(/ /g,'_');
            labelSpan.className = this.displayClass + 'Link';
            labelSpan.innerHTML = this.stack[i].label;

            var context = { 'bookmark': this,
                            'label': this.stack[i].label,
                            'action': 'zoom'
                          };

            OpenLayers.Event.observe(labelSpan, "click",
               OpenLayers.Function.bindAsEventListener(this.onBookmarkClick,
                                                       context)
            );

            bookmarkRow.appendChild(labelSpan);
            this.dataElementDiv.appendChild(bookmarkRow);
        }
        this.stackState = false;
        return this.dataElementDiv;
    },

    /**
     * Method:
     * A label has been clicked. Do somehting with/to it.
     *
     * Parameters:
     * e - {Event}
     *
     * Context:
     *  - {<OpenLayers.Control.Bookmark>} bookmark
     *  - {String} label
     *  - {String} action
     */
    onBookmarkClick: function(e) {
         switch(this.action) {
            case "remove":
               this.bookmark.remove(this.label);
               break;
            case "zoom":
            default:
               this.bookmark.zoomToLabel(this.label);
               break
         }
        OpenLayers.Event.stop(e);
    },

    /**
     * Method: showControls
     * Hide/Show all controls depending on whether we are
     *     minimized or not
     *
     * Parameters:
     * minimize - {Boolean}
     */
    showControls: function(minimize) {
        this.maximizeDiv.style.display = minimize ? "block" : "none";
        this.minimizeDiv.style.display = minimize ? "none" : "block";
        this.contentDiv.style.display = minimize ? "none" : "block";
    },

    /**
     * Method: showToggle
     * Hide/Show the toggle depending on whether the control is minimized
     *
     * Parameters:
     * minimize - {Boolean}
     */
    showToggle: function(minimize) {
        this.maximizeDiv.style.display = minimize ? "block" : "none";
        this.minimizeDiv.style.display = minimize ? "none" : "block";
    },

    /**
      * Method: ignoreEvent
      *
      * Parameters:
      * evt - {Event}
      */
    ignoreEvent: function(evt) {
       OpenLayers.Event.stop(evt);
    },

    /**
      * Method: mouseDown
      * Register a local 'mouseDown' flag so that we'll know whether or not
      *     to ignore a mouseUp event
      *
      * Parameters:
      * evt - {Event}
      */
    mouseDown: function(evt) {
        this.isMouseDown = true;
        this.ignoreEvent(evt);
    },

    /**
      * Method: mouseUp
      * If the 'isMouseDown' flag has been set, that means that the drag was
      *     started from within the control, and thus we can
      *     ignore the mouseup. Otherwise, let the Event continue.
      *
      * Parameters:
      * evt - {Event}
      */
    mouseUp: function(evt) {
        if (this.isMouseDown) {
            this.isMouseDown = false;
            this.ignoreEvent(evt);
        }
    },

    CLASS_NAME: "OpenLayers.Control.Bookmark"
});

OpenLayers.Control.Bookmark.VERSION_NUMBER = '2.1';