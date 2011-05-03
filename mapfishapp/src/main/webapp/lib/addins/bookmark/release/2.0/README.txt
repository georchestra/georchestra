Bookmark Control

   1. Bookmark Control
         1. Description
         2. Examples
         3. Installation and Use
         4. Advanced Usage
               1. Bookmarks with no containing DIV
               2. Bookmarks Outside the Map
               3. Passing A Function
         5. Styling
         6. Properties & Methods

Description

The Bookmark control provides a simple means to "bookmark" or "remember" a location. See OpenLayers Ticket #1583 for control history.

Examples

    * bookmark.html - A basic example of the Bookmark in action.
    * bookmark-custom.html - An example demonstrating advanced configurations of the Bookmark control.

Installation and Use

   1. Use subversion to check out the Bookmark addin.
   2. Place the Bookmark.js file in your Control directory (or alternatively, place it elsewhere and add a <script> tag to your page that points to it).
   3. Use the build tools to create a single file build (or link to your OpenLayers.js file to run in development mode).
   4. Add a stylesheet to your page to style the Bookmark, bookmark.css.
   5. Construct a map and add a Bookmark to it with the following syntax:

var map = new OpenLayers.Map("map");
var bookmark = new OpenLayers.Control.Bookmark();

/* when adding the control to the map, css elements are expected */
map.addControl(bookmark);

/* bookmark positions using a bounds object or string */
bookmark.add("BOSTON", new OpenLayers.Bounds(-71.401901,41.989908,-70.698776,42.693033));
bookmark.add("BOSTON TOO","-71.401901,41.989908,-70.698776,42.693033");

Advanced Usage

Bookmarks with no containing DIV

The developer can allow interaction with the Bookmark control without adding it as a map control. Simply
pass the map reference to the constructor. Using this method, the CSS declarations are not needed.

var map = new OpenLayers.Map("map");
var bookmark = new OpenLayers.Control.Bookmark({ 'map': map });
bookmark.add("BOSTON", new OpenLayers.Bounds(-71.401901,41.989908,-70.698776,42.693033));
bookmark.add("BOSTON TOO","-71.401901,41.989908,-70.698776,42.693033");

Bookmarks Outside the Map

<style>
#myBookmarks { height: 200px; width: 15em; float: right; }
#myBookmarks #olBookmarkElement { cursor : pointer; }
</style>

var map = new OpenLayers.Map("map");
bookmark = new OpenLayers.Control.Bookmark({ 'div':OpenLayers.Util.getElement('myBookmarks'), 'title': 'Bookmarks outside the map' });
map.addControl(bookmark);
bookmark.add("BOSTON", new OpenLayers.Bounds(-71.401901,41.989908,-70.698776,42.693033));
bookmark.add("BOSTON TOO","-71.401901,41.989908,-70.698776,42.693033");

<body>
<!-- Div to contain the bookmarks -->
<div id="myBookmarks"></div>
<div id="map"></div>
</body>

Passing A Function

Passing a developer defined function and value. This function will be called when bookmark.zoomToLabel() or bookmark.zoomToId()
methods are called. This can be triggered by the user clicking the bookmark in the DIV or, if no DIV is defined, using something similar to:

var map = new OpenLayers.Map("map");
var bookmark = new OpenLayers.Control.Bookmark();

/* add a bookmark with user defined function and value */
bookmark.add("BOSTON", new OpenLayers.Bounds(-71.401901,41.989908,-70.698776,42.693033),Msg,"Welcome to Boston");
bookmark.add("BOSTON TOO","-71.401901,41.989908,-70.698776,42.693033",Msg,"Welcome to Boston");

/* zoom to first found reference */
bookmark.zoomTo(bookmark.find("boston")[0]);.

function Msg(val) { alert(val); }

Styling

The look of the Bookmark contol is styled using CSS elements. If the Bookmark is used without a defining DIV, no CSS
elements are required. See CSS examples bookmark.css.

/* bookmark main div */
.olControlBookmark {}

/* bookmark content container */
.olControlBookmarkContent {}

/* bookmark title */
.olControlBookmarkTitle {}

/* bookmark data elements */
.olControlBookmarkElements {}

/* each row contains a remove element and link element */
.olControlBookmarkRow {}
.olControlBookmarkRemove {}
.olControlBookmarkLink {}

/* maximize and minimize buttons */
.olControlBookmarkMaximizeButton {}
.olControlBookmarkMinimizeButton {}

Note: Rounding of the bookmark div corners is done by Rico outside of the class.

OpenLayers.Rico.Corner.round( bookmark.div, {corners: "tl bl", bgColor: "transparent", color: "darkblue", blend: false});
OpenLayers.Rico.Corner.changeOpacity(bookmark.contentDiv, 0.75);

Properties & Methods

See Bookmark.js for full descriptions.
