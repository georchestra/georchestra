Mapfishapp Addons
=================

Addons are small pieces of client-side code which can be dynamically loaded and unloaded to/from geOrchestra's advanced viewer.
They provide additional functionality, which might be of interest for all or a fraction of your users.

Addons can be contributed to geOrchestra by their authors.
They can be hosted on https://github.com/georchestra/addons

If generic enough, they are pushed to the main repository, precisely in the [current directory](./).
We currently have the following addons available:
 * [annotation](annotation/README.md) tools.
 * [cadastre](cadastre/README.md) which allows users to locate a feature (typically a parcel) based on cascading drop downs (eg: state, then city, then borough).
 * [extractor](extractor/README.md) which relies on the services offered by the [extractorapp](/extractorapp/README.md) geOrchestra module.
 * [magnifier](magnifier/README.md) which allows one to explore a map area with configured imagery.
 * [openls](openls/README.md) which allows one to locate an address on the map.
 * [quicksearch](quicksearch/README.md) is an all-in-one search tool.
 * [streetview](streetview/README.md) ... obviously based on the Google Street View Image API.
 * [osm2geor](osm2geor/README.md) display vector data from OSM (got from the Overpass API) into a vector layer.
 * [measure](measure/README.md) to perform simple distance and area measurements (that cannot be printed).
 * [measurements](measurements/README.md) to perform advanced distance and area measurements (which can be printed & exported to KML).
 * [locateme](locateme/README.md) allows users to track their location on the map.
 * [fullscreen](fullscreen/README.md) to (obviously) make the map fullscreen.
 * [notes](notes/README.md) to report map issues.


Finding more addons
====================

There are other places where one can find contributed addons:
 * https://github.com/georchestra/addons
 * https://github.com/geobolivia/addons
 * https://github.com/geobretagne/addons
 * https://github.com/geosas/
 * https://github.com/jusabatier/nominatim


Deploying addons
=================

## Without georchestra.datadir

Deploying addons is just a matter of inserting a few lines of code in your configuration files.

Each addon comes with two files: ```manifest.json``` and ```config.json```.

```manifest.json``` addresses the following needs:
 * describes the files to load,
 * lists the addon options (see for instance the ```default_options``` [key](extractor/manifest.json#L8) in the extractor addon) and their most common values,
 * ships the translated strings.

The ```default_options``` from the manifest are overriden by the addon-config-specific ```options``` set in your own GEOR_custom.js file.
Again, an example is worth a hundred words, please refer to the typical [extractor addon config](extractor/README.md).

```config.json``` contains the following informations:

 * the unique identifier for the addon (an id)
 * the addon name,
 * a boolean indicating whether the addon is activated or not (i.e. available as the addon selection tool in the interface),
 * some extra infos (title, description) translated in several languages

Typically, the config.json block was previously contained in the GEOR_custom.js file, and is now dynamically sourced from a controller server-side.

## Using georchestra.datadir

If you are using the georchestra.datadir environment variable, the previous
behaviour applies, but if other addons are available in the
"georchestra datadir", they override the default ones provided by the webapp.

Loading addons
==============

The platform administrator decides which addons will be available to the end users.  
Users may choose the ones they want among these addons, through the dedicated UI.

Note that it is also possible to force the viewer to use a given list of addons, eg with:
http://my.sdi.org/mapfishapp/?addons=magnifier_0,annotation_0 (in which magnifier_0 and annotation_0 are the addon ids)


Addon placement
===============

Starting from geOrchestra 14.12, addons are able to escape the "tools" menu to which they were confined before.
This is achieved through the use of the optional ```target``` property in their configuration options.

Example ```init``` method taking into account the target property:
```js
    init: function(record) {
        if (this.target) {
            // addon placed in toolbar
            this.components = this.target.insertButton(this.position, {
                xtype: 'button',
                tooltip: this.getTooltip(record), // method provided by GEOR.Addons.Base
                iconCls: 'addon-xxx',
                handler: ...,
                scope: this
            });
            this.target.doLayout();
        } else {
            // addon placed in "tools menu"
            this.item = new Ext.menu.Item({
                text: this.getText(record), // method provided by GEOR.Addons.Base
                qtip: this.getQtip(record), // method provided by GEOR.Addons.Base
                iconCls: 'addon-xxx',
                handler: ...,
                scope: this
            });
        }
    }
```

Example configuration:
```js
    {
        "id": "test_0",
        "name": "Test",
        "options": {
            "target": "tbar_11"
        },
        "title": {
            "en": "My addon title"
        },
        "description": {
            "en": "My addon description"
        }
    }
```
With the above configuration (```"target": "tbar_11"```), the addon button will be inserted in the top toolbar, at position 11.
At the moment, the target can be one of ```tbar```, ```bbar``` for the bottom toolbar, ```tabs``` for the tabpanel in the lower right corner of the screen.

If no target is specified, the addon will have the default behavior (as before) and it's component will be placed inside the "tools" menu.

In order to achieve this, addons are supposed to inherit from the ```GEOR.Addons.Base``` class. 
Older addons still work, but they will not take advantage of the newer capability.


Developers' corner
===================

The code responsible for loading/unloading addons is the ```fetchAndLoadTools``` method located inside [GEOR_tools.js](mapfishapp/src/main/webapp/app/js/GEOR_tools.js).

Let's go through this code quickly...

First we're computing which addons have to be loaded, and which other addons have to be unloaded.

For each addon to unload (```outgoing```), we're calling ```destroy()``` on the addon instance.

For each addon to load (```incoming```), we're trying to fetch it's ```manifest.json``` file via an XHR.
On success, the manifest is parsed and its i18n dictionaries are merged into the current application lang dictionaries.
The CSS and JS files are also dynamically loaded (via ```Ext.Loader.load``` for the JS). Once the JS files are loaded, the addon is instanciated. 
The constructor takes 2 arguments: the current OpenLayers.Map object and the options object (```default_options``` superseded with the ```options``` provided by the platform administrator).
Finally, the addon's ```init``` method is called with an ```Ext.data.Record``` object representing the addon config. The record fields are: "id", "name", "title", "thumbnail", "description", "group", "options", "_loaded", "preloaded".
If the addon instance exposes a public property named ```item```, the referenced object is inserted in the "tools" menu.


If developing a new addon, you might want to start from a simple example, eg the [magnifier](magnifier/README.md) addon.
