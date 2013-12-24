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

Deploying addons
=================

Deploying addons is just a matter of inserting a few lines of code in your configuration files.
An example is provided in the template configuration, here: [georchestra/template/mapfishapp/app/js/GEOR_custom.js](https://github.com/georchestra/template/blob/master/mapfishapp/app/js/GEOR_custom.js#L47).

Each addon comes with a ```manifest.json``` file which:
 * describes the files to load,
 * lists the addon options (see for instance the ```default_options``` [key](extractor/manifest.json#L8) in the extractor addon) and their most common values,
 * ships the translated strings.

The ```default_options``` from the manifest are overriden by the addon-config-specific ```options``` set in your own GEOR_custom.js file.
Again, an example is worth a hundred words, please refer to the typical [extractor addon config](extractor/README.md).

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