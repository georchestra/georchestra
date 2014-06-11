The development branch is master. It can be used for testing and reporting errors.

For production systems, you are advised to use the stable branch (currently 14.01).
This branch receives bug fixes as they arrive, during 6 months at least.


Version 14.01 (current stable version)
======================================

This release was supported by the GeoPicardie, PIGMA, CIGALsace and GeoBretagne projects. 
The CRAIG (Centre Régional Auvergnat de l'Information Géographique) is also to be thanked for some nice patches.

New features:
 * [GeoFence](https://github.com/geosolutions-it/geofence/blob/master/README.markdown) integration (not activated by default), see [#534](https://github.com/georchestra/georchestra/issues/534) and the [documentation](https://github.com/georchestra/geofence/blob/georchestra/georchestra.md)
 * geoserver: new kml and tiff+tab datastores, see [#557](https://github.com/georchestra/georchestra/issues/557)
 * geoserver: now supports data security with GWC requests, see [GEOS-4217](http://jira.codehaus.org/browse/GEOS-4217) and [geoserver/geoserver#341](https://github.com/geoserver/geoserver/pull/341)
 * mapfishapp: multi-layer querier tool (thanks to the [CRAIG](http://craig.fr/)) - see [#435](https://github.com/georchestra/georchestra/pull/435)
 * mapfishapp: extractor addon - see the [README](mapfishapp/src/main/webapp/app/addons/extractor/README.md)
 * mapfishapp: OpenLS addon - see the [README](mapfishapp/src/main/webapp/app/addons/openls/README.md)
 * mapfishapp: editor revamped - read [this](mapfishapp/README.md#feature-editor)
 * mapfishapp: document persistence in database - see [#443](https://github.com/georchestra/georchestra/issues/443)

Enhancements:
 * analytics: translated to ES, thanks to [GeoBolivia](http://geo.gob.bo/) !
 * analytics: tabs displayed or not based on ```shared.download_form.activated``` and ```shared.ogc.statistics.activated``` values.
 * doc: improved [installation instructions for gdal native libs](INSTALL.md#gdal-for-geoserver-extractorapp--mapfishapp)
 * doc: installation instructions updated with [GeoServer fine tuning instructions](INSTALL.md#geoserver)
 * doc: added a [README](mapfishapp/src/main/webapp/app/addons/README.md) for mapfishapp addons
 * js minification: test that jsbuild is working, rather than only testing if venv exists
 * ldapadmin: in the mail sent to the moderator, the "from" field is set to the user email - see [#380](https://github.com/georchestra/georchestra/pull/380)
 * ldapadmin: improved description fields - see [#400](https://github.com/georchestra/georchestra/pull/400)
 * ldapadmin: added ability to configure fields in users list - read the [how-to](ldapadmin/README.md#configure-the-look-of-the-users-list)
 * ldapadmin: in case of duplicated email error, the message is more explicit
 * ldapadmin: the "lost password" has become a "password recovery" page, which is more neutral, and allows one to define a password even if none pre-exists.
 * mapfishapp: WMS DescribeLayer on each WMS layer - see [#401](https://github.com/georchestra/georchestra/pull/401)
 * mapfishapp: new layer menu item to set layer as baselayer/overlay - see [#445](https://github.com/georchestra/georchestra/pull/445)
 * mapfishapp: preserve the full attribution information on context save/restore - see [#422](https://github.com/georchestra/georchestra/pull/422)
 * mapfishapp: cswquerier: better results count
 * mapfishapp: wms layer tab: red crosses removed, icons centered
 * mapfishapp: backbuffer hidden behind baselayer for non-opaque baselayers - see [#411](https://github.com/georchestra/georchestra/pull/411)
 * mapfishapp: allow to POST the url of a WFS service or layer - see [#392](https://github.com/georchestra/georchestra/pull/392)
 * mapfishapp: baselayers have a specific color in the layer manager - customizable, see [0a56ed](https://github.com/georchestra/georchestra/commit/0a56edc8e0ea6361e056ce30047d8eddaa7c4c75)
 * mapfishapp: contextual help bubbles (eg: on layer edit activated), see [#466](https://github.com/georchestra/georchestra/issues/466)
 * mapfishapp: print now supports WMS 1.3.0-only capable servers, see [#511](https://github.com/georchestra/georchestra/issues/511)
 * mapfishapp: annotation addon: added an icon & made the window closable
 * mapfishapp: OGC Exception Report handling deactivated during context restore - see [#532](https://github.com/georchestra/georchestra/issues/532)
 * mapfishapp: allow to show login/logout button in toolbar even if header is shown - see [#43](https://github.com/georchestra/georchestra/issues/43)
 * mapfishapp: file upload now reports more accurately errors - see [#402](https://github.com/georchestra/georchestra/issues/402)
 * mapfishapp: file upload: a spinner is shown while a file is uploaded
 * mapfishapp: contexts can now store title + abstract fields - see [#443](https://github.com/georchestra/georchestra/issues/443)
 * mapfishapp: added ability to send contexts or maps to any external application - see [#443](https://github.com/georchestra/georchestra/issues/443)
 * ogc-server-statistics: now logging WMTS GetTile, WMS GetStyles + WFS2 operations, see [#527](https://github.com/georchestra/georchestra/issues/527)
 * proxy: new filter to make basic auth challenge if https and matches user-agent, useful for ArcGIS clients - read the [notes](https://github.com/georchestra/georchestra/commit/8828a11ffb0cb716ad0a6bb1f847ce24328ea450)
 * proxy: overridable HTTP 40x error pages, see for instance [config/defaults/security-proxy/403.jsp](config/defaults/security-proxy/403.jsp)
 * proxy: does not send sec-username & sec-roles headers when the user is anonymous, see [#223](https://github.com/georchestra/georchestra/pull/223)
 * static: module is now called "header"
 * downloadform, ogcstatistics and ldapadmin modules now share the same postgresql database by default, each in their own schema. Please refer to [#516](https://github.com/georchestra/georchestra/issues/516) and the migration guide below.

Bug fixes:
 * analytics: fixed wrong password for jdbc postgresql
 * extractorapp: fixed the ```checkPermission``` method for local layers
 * extractorapp: fixed impossibility to switch to french when default lang is english or spanish
 * extractorapp: fixed invalid buffer combo text
 * extractorapp: removed useless classes - see [#551](https://github.com/georchestra/georchestra/issues/551)
 * extractorapp: bbox writer always uses geotools' ShpFeatureWriter, which allows extractorapp to not rely mandatorily on gdal/ogr native libs - see [#409](https://github.com/georchestra/georchestra/issues/409)
 * extractorapp: fixed parameter order on CheckFormAcceptance bean instantiation - see [b299ec](https://github.com/georchestra/georchestra/commit/b299ec9f55777ef9f3610c14f01e0449e0067f3c)
 * extractorapp: fixed wrong jsessionid used to check ability to download resource - see [#558](https://github.com/georchestra/georchestra/issues/558)
 * extractorapp: fixed CheckFormAcceptance SQL test
 * geonetwork: download form now opens also in metadata view if activated, see [#416](https://github.com/georchestra/georchestra/issues/416)
 * geonetwork: fixed missing thumbnail in CSW query requesting DC in full mode for profil France records
 * geonetwork: thumbnails: add protocol for JPG (csw)
 * geonetwork: widgets / keyword selection / support 2 concepts with same label
 * geonetwork: editor / XML view / do not escape &#10;
 * geonetwork: ISO19110 / fixed missing label. Add the capability to set contact logo.
 * geonetwork: spatial index / fixed corrupted shapefile when empty polygon.
 * geonetwork: ISO19110 / relation now displays title.
 * geonetwork: properly unzip file
 * geonetwork: widgets / properly propagate sortby options.
 * geonetwork: fixed map coords position when page scrolled
 * geonetwork: fixed facet layout issue
 * geonetwork: widgets / add privileges panel to batch operation.
 * geonetwork: ISO19139 / improve labels
 * geonetwork: fixed tooltip display error on IE
 * geonetwork: hide user menu if hideSignOut option is enable IE
 * geonetwork: add option to hide sign out action from user menu.
 * geonetwork: editor / suggestion / save changes before processing
 * geonetwork: RSS / add URL parameter to only return one link for each metadata
 * geonetwork: widgets / action menu is now in a custom element in the template
 * geonetwork: put default list width of some other search criterias to auto
 * geonetwork: search suggestion / properly returned field value with line break
 * geoserver: fixed "inspire extension not deployed"
 * header: fixed IE8 compatibility + header frameborder size set to 0
 * header: the platform-wide language set by ```shared.language``` is now enforced in the header module, see [#540](https://github.com/georchestra/georchestra/issues/540)
 * ldapadmin: fixed incorrect generation of login - see [#344](https://github.com/georchestra/georchestra/pull/344)
 * ldapadmin: remove user from groups after deleting the user - see [#406](https://github.com/georchestra/georchestra/pull/406)
 * ldapadmin: prevent race condition when opening ```/#groups/:group``` directly.
 * ldapadmin: fixed erroneous admin count - see [#405](https://github.com/georchestra/georchestra/pull/405)
 * ldapadmin: send HTTP 403 status code when access is forbidden, not 200 - see [#244](https://github.com/georchestra/georchestra/issues/244)
 * ldapadmin: normalizes the user input so that we consider the uid is always lowercased - see [#565](https://github.com/georchestra/georchestra/issues/565)
 * ldapadmin: fixed missing header on the account/newPassword page
 * ldapadmin: fixed group ordering in privateui
 * mapfishapp: fixed incorrect styles ordering
 * mapfishapp: annotation addon: fixed label size
 * mapfishapp: fix for vector features incompletely displayed - see [#367](https://github.com/georchestra/georchestra/pull/367)
 * mapfishapp: buggy legend url does not make the print fail anymore- see [#362](https://github.com/georchestra/georchestra/pull/362)
 * mapfishapp: ```window.onbeforeunload``` should not return null (fixed annoying IE popup)
 * mapfishapp: fixed "too many features" message in referentials search
 * mapfishapp: fixed WMS GetFeatureInfo feature reprojection in IE
 * mapfishapp: always compute data model on getfeatureinfo
 * mapfishapp: always prefer ```text/html``` metadataURL entries
 * mapfishapp: fixed WFS2 capabilities - see [#373](https://github.com/georchestra/georchestra/pull/373)
 * mapfishapp: fixed JPEG layers in WMC loaded as PNG - see [#370](https://github.com/georchestra/georchestra/pull/370)
 * mapfishapp: connection link in toolbar when header height is set to 0
 * mapfishapp: referential recenter : fix for old fashioned namespace usage
 * mapfishapp: fixed missing dependency to WFSCapabilitiesReader
 * mapfishapp: annotation addon: fixed calling ```createUrlObject``` with an object parameter - see [#437](https://github.com/georchestra/georchestra/pull/437)
 * mapfishapp: in layerfinder, fixed incorrect reference to ```OpenLayers.i18n``` method
 * mapfishapp: file upload - the limit is 8MB, not 8GB
 * mapfishapp: file upload - better handling of server-side errors
 * mapfishapp: fixed extra comma (IE) in GEOR_print
 * mapfishapp: fixed wrong popup anchor position on edit - see [#456](https://github.com/georchestra/georchestra/pull/456)
 * mapfishapp: annotations: fixed popup anchor - see [#366](https://github.com/georchestra/georchestra/pull/366)
 * mapfishapp: fixed potentially incorrect metadata url - see [#454](https://github.com/georchestra/georchestra/pull/454)
 * mapfishapp: less strict filtering of suitable images for thumbnail display in CSW querier
 * mapfishapp: fixed missing dependency to WKT format - see [#482](https://github.com/georchestra/georchestra/pull/482)
 * mapfishapp: fixed incorrect ```maxScaleDenominator``` on WMC restored - see [#431](https://github.com/georchestra/georchestra/pull/431)
 * mapfishapp: attribution logo correctly sized - see [#490](https://github.com/georchestra/georchestra/pull/490)
 * mapfishapp: fixed wrong proxy selected when the webapp name does not contain private - see [#509](https://github.com/georchestra/georchestra/pull/509)
 * mapfishapp: fixed various problems in legend printing
 * mapfishapp: fixed missing ```GEOR.config.USEREMAIL``` (used by the extractor addon)
 * mapfishapp: set a white background to the overview map in the printed PDF - see [#372](https://github.com/georchestra/georchestra/pull/372)
 * mapfishapp: overview map now working at most small scales - see [#513](https://github.com/georchestra/georchestra/issues/513)
 * mapfishapp: fixed magnifier tool - see [#500](https://github.com/georchestra/georchestra/issues/500)
 * mapfishapp: fixed too much space between elements in FireFox - see [#539](https://github.com/georchestra/georchestra/issues/539)
 * mapfishapp: print: white background for the overview map, see [b09cc9](https://github.com/georchestra/template/commit/b09cc94dcb66186b2ca48d5d0df5b2b7b95e1ed8)
 * mapfishapp: print: scaled down legend icons to match map icons size, see [436913](https://github.com/georchestra/template/commit/43691352bc81d024dff01245ba33c47605c7a607)
 * mapfishapp: print: limit legend texts width, and wrap them, see [78c05d](https://github.com/georchestra/template/commit/78c05d9d01699411df282ae6fca1965a9825b21b)
 * mapfishapp: print: left align the legend to its column container, see [d707a8](https://github.com/georchestra/template/commit/d707a8f7371bf56059758802e7afbb891f34bfce)
 * mapfishapp: fixed incorrect metadata URL in csw browser ("add a layer from thesaurus") - see [#542](https://github.com/georchestra/georchestra/issues/542)
 * ogcservstatistics - fixed missing postgresql driver loading
 * proxy: fixed charset detection in ArcGIS server responses - see [#498](https://github.com/georchestra/georchestra/pull/498)
 * proxy: removed ```sec-*``` headers from client request - see [#154](https://github.com/georchestra/georchestra/pull/154)
 * proxy: fixed incorrect referer value - see [#533](https://github.com/georchestra/georchestra/issues/533)
 * header: maintains existing URI parameters when adding the "login" param - see [#175](https://github.com/georchestra/georchestra/issues/175)
 * build now passes on windows.

UPGRADING:
 * As a result of [#569](https://github.com/georchestra/georchestra/issues/569), LDAP groups are now ```groupOfNames``` instances rather than ```posixGroup``` instances. You have to migrate your LDAP tree, according to the following procedure (please change the ```dc=georchestra,dc=org``` string for your own base DN):
   * dump your ldap **groups** with:

   ```
   ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -w your_ldap_password -b "ou=groups,dc=georchestra,dc=org" > /tmp/groups.ldif
   ```

   * migration:
   
   ```
   sed -i 's/\(memberUid: \)\(.*\)/member: uid=\2,ou=users,dc=georchestra,dc=org/' /tmp/groups.ldif
   sed -i 's/posixGroup/groupOfNames/' /tmp/groups.ldif
   sed -i '/gidNumber/d' /tmp/groups.ldif OR sed -i 's/gidNumber/ou/' /tmp/groups.ldif if geofence is deployed
   sed -i 's/objectClass: groupOfNames/objectClass: groupOfNames\nmember: uid=fakeuser/' /tmp/groups.ldif
   ```
   
   * drop your groups organizationalUnit (```ou```)
   * optionally, have a look at the provided [georchestra-memberof.ldif](https://github.com/georchestra/LDAP/blob/master/georchestra-memberof.ldif) file, which creates & configures the [memberOf overlay](http://www.openldap.org/doc/admin24/overlays.html). As root, and after checking that the file targets the correct database (```olcDatabase={1}hdb``` by default): ```ldapadd -Y EXTERNAL -H ldapi:// < georchestra-memberof.ldif```
   * import the updated groups.ldif file.
 * analytics: the ExtJS submodule path has changed, be sure to run ```git submodule update --init``` when you switch branches.
 * databases: the downloadform, ogcstatistics and ldapadmin databases are now merged into a single one named "georchestra". Each webapp expects to find its tables in a dedicated schema ("downloadform" for the downloadform module, "ogcstatistics" for ogc-server-statistics, and "ldapadmin" for ldapadmin). See [#535](https://github.com/georchestra/georchestra/pull/535) for the complete patch. If you currently have one dedicated database for each module, you can keep your setup, provided you customize the ```shared.psql.ogc.statistics.db```, ```shared.psql.download_form.db``` & ```shared.ldapadmin.db``` maven filters in your own config. In any case, you'll have to rename the ```download``` schema (of the previous ```downloadform``` database) into ```downloadform```, and migrate the tables which were in the public schema of the databases ```ogcstatistics``` and ```ldapadmin``` into the newly created schemas. 
 
Example migration script:
 
```
psql -d downloadform -c 'alter schema download rename to downloadform;'

wget https://raw.github.com/georchestra/georchestra/14.01/ldapadmin/database.sql -O /tmp/ldapadmin.sql
psql -d ldapadmin -f /tmp/ldapadmin.sql
psql -d ldapadmin -c 'GRANT ALL PRIVILEGES ON SCHEMA ldapadmin TO "www-data";'
psql -d ldapadmin -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ldapadmin TO "www-data";'
psql -d ldapadmin -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ldapadmin TO "www-data";'
psql -d ldapadmin -c 'insert into ldapadmin.user_token (uid, token, creation_date) select uid, token, creation_date from public.user_token;'
psql -d ldapadmin -c 'drop table public.user_token;'

wget https://raw.github.com/georchestra/georchestra/14.01/ogc-server-statistics/database.sql -O /tmp/ogcstatistics.sql
psql -d ogcstatistics -f /tmp/ogcstatistics.sql
psql -d ogcstatistics -c 'GRANT ALL PRIVILEGES ON SCHEMA ogcstatistics TO "www-data";'
psql -d ogcstatistics -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ogcstatistics TO "www-data";'
psql -d ogcstatistics -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ogcstatistics TO "www-data";'
psql -d ogcstatistics -c 'insert into ogcstatistics.ogc_services_log (id, user_name, date, service, layer, request, org) select id, user_name, date, service, layer, request, org from public.ogc_services_log;'
psql -d ogcstatistics -c 'drop table public.ogc_services_log;'
```

 * download form: the module is disabled by default (```shared.download_form.activated=false```). Be sure to set the value you want in your shared.maven.filters file.
 * extractorapp:
   * ```BUFFER_VALUES``` has changed. If you had a custom value in your GEOR_custom.js file, you have to modify it according to the new syntax.
   * the ```geobretagne_production``` env variable has been removed - see [#97](https://github.com/georchestra/georchestra/pull/97)
 * geoserver: be sure to set the ```file.encoding``` tomcat option for geoserver to interpret correctly UTF-8 SLDs (read [how](INSTALL.md#geoserver)).
 * ldapadmin:
   * accessing ```/ldapadmin/privateui/``` is now restricted to members of the ```MOD_LDAPADMIN``` group. It is recommended that only members of the ```ADMINISTRATOR``` or ```SV_ADMIN``` administrative groups belong to ```MOD_LDAPADMIN```, since this group allows privileges escalation.
   * new ```shared.ldapadmin.db``` parameter to specify the ldapadmin database name (defaults to "georchestra").
   * the ldapadmin private app is now accessed via /ldapadmin/privateui/ rather than /ldapadmin/privateui/index.html
 * mapfishapp:
   * geonames now require you to create an account in order to enable queries on their free web services (see [#563](https://github.com/georchestra/georchestra/issues/563)). Please change the default account in your profile's GEOR_custom.js ```GEONAMES_FILTERS``` variable.
   * addons: custom addons relying on local web services should no longer assume that the application path is ```/mapfishapp```. Instead, they should use the new ```GEOR.config.PATHNAME``` constant, eg [here](https://github.com/georchestra/georchestra/blob/04017309f3880a0c558537235c92f70a269722d1/mapfishapp/src/main/webapp/app/addons/annotation/js/Annotation.js#L486).
   * the app now requires a dedicated database schema, please refer to the [INSTALL.md](INSTALL.md#postgresql) documentation.
   * new config option: ```SEND_MAP_TO``` for [#443](https://github.com/georchestra/georchestra/issues/443), please read the [doc](https://github.com/georchestra/template/blob/34496d62701e809c80235275a9e2a0b4b46f1123/mapfishapp/app/js/GEOR_custom.js#L583).
   * new config option: ```FORCE_LOGIN_IN_TOOLBAR```
   * the ```NS_EDIT``` config option has been removed, and mapfishapp/edit is no longer routed. By default, all layers served by the platform geoserver are editable (see ```GEOR.custom.EDITABLE_LAYERS```), provided the user has the rights to (defaults to members of ```ROLE_ADMINISTRATOR```, see ```GEOR.custom.ROLES_FOR_EDIT```).
   * the contexts referenced in your ```GEOR.custom.CONTEXTS``` array are now able to reference layers with their full attribution information (text, logo & link). Have a look at the provided [default.wmc](https://github.com/georchestra/template/blob/55f24c8625e737d0b4567db92966c98502578766/mapfishapp/default.wmc#L39).
   * print: some parameters have changed when the print module was updated: ```maxIconWidth``` -> ```iconMaxWidth```, ```maxIconHeight``` -> ```iconMaxHeight``` (see [e6231c](https://github.com/georchestra/template/commit/e6231c8cbf325dfa2bf96fcaa14096fc0c64ab89)).
 * ogcservstatistics - disabled by default: ```shared.ogc.statistics.activated=false```. Be sure to set the value you want in your shared.maven.filters file.
 * static: the "static" module has been renamed into "header": your deployment scripts *must* be adapted, as well as your apache2 configuration (or any other reverse proxy).


Version 13.09 
==============

This major release was supported by the GeoPicardie, PIGMA, CIGALsace and GeoBretagne projects. 
The CRAIG (Centre Régional Auvergnat de l'Information Géographique) is also to be thanked for some nice patches.

New features:
 * mapfishapp: [annotation addon](https://github.com/georchestra/georchestra/tree/master/mapfishapp/src/main/webapp/app/addons/annotation) with KML export,
 * mapfishapp: geofile upload: support of SHP, MIF/MID, GML, KML by default (uses a geotools implementation). Support of TAB and GPX when OGR is available (read this [how to](https://github.com/georchestra/georchestra/tree/master/mapfishapp#optional-install-gdal-native-library)),
 * mapfishapp: WMS + WFS version autodetection,
 * mapfishapp: WMTS 1.0.0 support,
 * mapfishapp: WFS 2.0.0 support,
 * mapfishapp: WMS 1.3.0 support,
 * mapfishapp: OWSContext 0.3.x READ support (WMS layers),
 * mapfishapp: French IGN's GeoPortail webservices support,
 * mapfishapp: Russian translation (!),
 * geoserver: Opaque WMS Layer property support (see [#158](https://github.com/georchestra/georchestra/issues/158)),
 * ldapadmin: a brand new module is available which allows at the same time to: admin your users in your browser, let your users recover their lost password, and let them register too ! More information in the module [README](ldapadmin/README.md),
 * extractorapp: ship metadata in ZIP (read this [how to](extractorapp/README.md#metadata-extraction)),
 * extractorapp: now supports KML & TIF+(TFW,TAB) output.

Enhancements:
 * mapfishapp: external libs such as ExtJS updated to 3.4.1.1 (for IE 10 support), GeoExt and OpenLayers updated to master (yeah !). This brings greater navigation ease and support for other cool stuff.
 * mapfishapp: added ability to restore contexts with a projection different from the map's (assuming layers will be able to reproject),
 * mapfishapp: print improved (updated to MapFish Print 2.0 for WMTS and GeoPortail support, brand new templates, natural 91 dpi resolution and new "comment" field),
 * mapfishapp: referentials search is no more case sensitive with WFS2,
 * mapfishapp: improved syntax for metadata search (via CSW), see [#325](https://github.com/georchestra/georchestra/pull/325),
 * mapfishapp: true print extent displayed,
 * mapfishapp: querier radius is now dynamically displayed in meters/km,
 * mapfishapp: WFS layers feature selection & attributes viewing, 
 * mapfishapp: layer name and source smartly ellipsed with CSS rather than JS,
 * mapfishapp: do not close window on style applied,
 * mapfishapp: layer style list is now alphabetically sorted,
 * mapfishapp: permalink validity displayed in months,
 * mapfishapp: link to layer metadata which is declared as text/html is now prefered over the other links,
 * mapfishapp: addons can now be loaded by default (read [how](https://github.com/georchestra/template/blob/45eddec545418b4de55952795c66940729d3b547/mapfishapp/app/js/GEOR_custom.js#L64)),
 * mapfishapp: added a [note on data](mapfishapp/README.md#your-data-in-mapfishapp) in the doc,
 * extractorapp: several small fixes for extractorapp reprojection,
 * extractorapp: more visible extract button,
 * extractorapp: auto-deactivate the "Modify the bbox" button,
 * geonetwork: validation report: translated all reported XSD errors in editor,
 * geonetwork: user menu: improved links according to role,
 * geonetwork: RSS: added URL parameter to only return one link for each metadata,
 * geonetwork: widgets: added privileges panel to batch operation,
 * geonetwork: improved icons plus a custom icon for XLS files,
 * SDI Instance name in page titles across the SDI (```shared.instance.name```, defaulting to "geOrchestra"),
 * everywhere: plain text emails, with the ability to switch back to HTML via ```shared.email.html```,
 * everywhere: all outgoing emails are now prefixed with the platform name,
 * everywhere: better translations.
 * documentation: improved [installation](INSTALL.md) instructions

Bug fixes:
 * security-proxy: now only sends one referer headers - fixes consuming arcgis web services - [read more](https://github.com/georchestra/georchestra/issues/266),
 * geoserver: fixed incorrect WMS 1.3.0 scaledenominator values, see [#264](https://github.com/georchestra/georchestra/issues/264),
 * geonetwork: editor: suggestion: changes saved before processing,
 * geonetwork: user menu: IE compatibility issues fixed,
 * static: fixed incorrect login link on CAS pages,
 * cas: fixed IE8 JS error on login page,
 * extractorapp: fixed app loading on IE8,
 * extractorapp: now reports broken or uninstalled GDAL libraries,
 * mapfishapp: WM[T]S GetFeatureInfo geometries on the fly reprojection (at last !) - as a result, it is advised to fill the most widely used SRSes in your country in your [GEOR_custom.js](https://github.com/georchestra/template/blob/45eddec545418b4de55952795c66940729d3b547/mapfishapp/app/js/GEOR_custom.js#L365) config file,
 * mapfishapp: fixed fontFamily not taken into account by styler - also requires ```apt-get install ttf-mscorefonts-installer```,
 * mapfishapp: fixed querier setup issue when WFS service is not available,
 * mapfishapp: more robust layer hydrating from namespaced capabilities,
 * mapfishapp: fixed zooming occuring while drawing features,
 * mapfishapp: mouse position rounding is now correct for ETRS89, RGF93 and other long-lat based projections,
 * mapfishapp: fixed scrolling on a map without any visible layer,
 * mapfishapp: fixed unresolved images for point symbolizer combo,
 * mapfishapp: fixed legend label not appearing when only one class is available,
 * mapfishapp: fixed incorrect describeFeatureType URLs,
 * mapfishapp: fixed undefined addons title and description when lang dict does not exist yet,
 * mapfishapp: fixed broken referentials search in IE8,
 * mapfishapp: fixed broken help url


UPGRADING:
 * mapfishapp:
   * default projection changes from EPSG:2154 to EPSG:3857 (aka Spherical Web Mercator). Your users might need to clear their localStorage, or force loading of the new default context.
   * default MAP_SCALES changes to match the OGC WMTS spec,
 * LDAP: see [georchestra/LDAP#2](https://github.com/georchestra/LDAP/pull/2)
   * one group was renamed: ```STAT_USER``` becomes ```MOD_ANALYTICS``` - grants access to the analytics app,
   * an other one was created: ```MOD_LDAPADMIN``` - grants access to the LDAPadmin private UI (/ldapadmin/privateui/).
 * The default application language is now **English**:
   * ```shared.language``` = en
   * ```geonetwork.language``` = eng
   * default email templates [here](https://github.com/georchestra/georchestra/tree/master/config/defaults/ldapadmin/WEB-INF/templates) and [there](https://github.com/georchestra/georchestra/tree/master/config/defaults/extractorapp/WEB-INF/templates): be sure to override them in your own config !
 * Remember also to fill these new global maven filters: 
   * ```shared.homepage.url``` - for your SDI home page (might be something like http://my.sdi.org/portal/),
   * ```shared.instance.name``` - will be displayed in page titles (eg: GeoMyCompany),
   * ```shared.email.html``` - whether to send emails in plain text (default) or HTML,
   * ```shared.administrator.email``` - this email receives new account requests (eg: me@mycompany.com)
 * shared maven filters renamed:
   * ```shared.smtp.replyTo``` -> ```shared.email.replyTo```
   * ```shared.smtp.from``` -> ```shared.email.from```
 * frontend webserver:
   * add a proxy rule for `/_static/` subdirectory (see https://github.com/georchestra/georchestra/tree/master/INSTALL.md)


Version 13.06
==============

This release was supported by the French GeoPicardie, GeoBretagne and CIGALsace projects, the GIP ATGeRi and individual contributors.

New features:
 * geoserver: updated to 2.3.2,
 * geoserver: patched to include ScaleHint (WMS 1.1.x) and Min/MaxScaleDenominator (WMS 1.3.0) in the capabilities documents, according to the default layer SLD. The patched was accepted in GeoServer master, see http://jira.codehaus.org/browse/GEOS-572

Enhancements:
 * the header is now part of the static module, and all webapps make use of it. As a result, requests to static pass through the security-proxy,
 * the header height can be set across all applications by the way of a unique shared maven filter,
 * mapfishapp: the CSW querier filters results on type = dataset OR series,
 * extractorapp: supported output formats combos made configurable, ECW output format expunged by default,
 * the template config was slimmed down: files where default values are suitable for most deployments were moved to config/defaults,
 * the documentation was improved ([mapfishapp](mapfishapp/README.md#feature-editor), [install](INSTALL.md), [manage configs](README.md#how-to-customize-)),
 * we now have CI on JS files syntax with Travis !

Bug fixes:
 * fixed the ability to turn off the downloadform with shared.downloadform.activated,
 * mapfishapp: referentials search now sends the map srs in the WFS query to allow feature reprojection,
 * mapfishapp: fixed incorrect thumbnail URLs in CSW querier,
 * mapfishapp: fixed misaligned thumbnail in CSW querier,
 * mapfishapp: fixed window + grid problems on service opening, see [issue 109](https://github.com/georchestra/georchestra/issues/109),
 * mapfishapp: fixed error in GEOR.ows.hydrateLayerRecord when falling back to main service,
 * mapfishapp: fixed links to MD sheet, see [issue 110](https://github.com/georchestra/georchestra/issues/110),
 * mapfishapp: fixed broken legend after styling, see [issue 107](https://github.com/georchestra/georchestra/issues/107),
 * mapfishapp: more robust handling of incoming WMS server URLs (eg: those with a mapfile GET parameter),
 * geonetwork: fixed ldap attribute mapping.
 
UPGRADING:
 * mapfishapp's default thesaurus has been set to local.theme.test, which is the only one exported by GeoNetwork by default. Feel free to customize to suit your needs,
 * geonetwork upgrade instructions are available [here](https://github.com/georchestra/geonetwork/blob/georchestra-29/README.md).


Version 13.02
=============

This release was made possible thanks to support from the French GIP ATGeRi (http://cartogip.fr/) and contributors.

New features:
 * geoserver: updated to 2.3.0, see http://blog.geoserver.org/2013/03/18/geoserver-2-3-0-released-first-official-osgeo-release/
 * geoserver: useful extensions added in template profile, see http://applis-bretagne.fr/redmine/issues/4217
 * geonetwork: upgraded geonetwork to geonetwork master (2.9.0-pre)
 * extractorapp: extraction bbox is now part of the data bundle, see https://github.com/georchestra/georchestra/pull/35
 * mapfishapp: lon, lat and radius GET parameters for startup recentering, see https://github.com/georchestra/georchestra/pull/20
 * mapfishapp: switchable pointer coordinates SRS, see https://github.com/georchestra/georchestra/pull/25
 * mapfishapp: layers drag'n drop in layer manager, see http://applis-bretagne.fr/redmine/issues/1959
 * mapfishapp: OGC context switcher, see https://github.com/georchestra/georchestra/pull/26
 * mapfishapp: print layouts ACL, see https://github.com/georchestra/georchestra/pull/30
 * mapfishapp: spatial query based on a circle, see http://applis-bretagne.fr/redmine/issues/1957
 * mapfishapp: support for addons & magnifier addon, see https://github.com/georchestra/georchestra/pull/36
 * mapfishapp: cadastre addon, see https://github.com/georchestra/georchestra/pull/48
 * mapfishapp: support transitionEffect resize (aka "back buffers") on layers coming from a WMC, see https://github.com/georchestra/georchestra/pull/42

Enhancements:
 * mapfishapp: results panel displays URLs as html links, see https://github.com/georchestra/georchestra/pull/21
 * mapfishapp: add layer from thesaurus: metadata title first, see https://github.com/georchestra/georchestra/pull/23
 * mapfishapp: more visible layer names, see https://github.com/georchestra/georchestra/pull/22
 * mapfishapp: add zoomout button in the toolbar, see https://github.com/georchestra/georchestra/pull/24
 * mapfishapp: added ability to print protected geoserver layers, see https://github.com/georchestra/template/commit/bb424bd74f7504af93b5e5c708f807ce0b6fdca4
 * mapfishapp: more robust detection of WMS layers in CSW getRecords responses, see https://github.com/georchestra/georchestra/pull/4
 * mapfishapp: window buttons consistency and default actions, see https://github.com/georchestra/georchestra/pull/33
 * mapfishapp: by default, the map is now restored with its latest known state (context), see https://github.com/georchestra/georchestra/pull/50
 * mapfishapp: missing translations
 * mapfishapp, downloadform, extractorapp, security-proxy, ogc-server-statistics: the java packages now belong to org.georchestra
 * mapfishapp: DocController's maxDocAgeInMinutes was change to manage long integer value, see https://github.com/georchestra/georchestra/pull/81

Bug fixes:
 * security-proxy: Location header was erroneously removed in some cases, see https://github.com/georchestra/georchestra/commit/fef3d77ab4fe0e6045c47add1f84dbd7de3a8c4e
 * mapfishapp: fixed erroneous WMSC2WMS mapping, which prevented printing of the GeoBretagne OSM baselayer, see https://github.com/georchestra/georchestra/commit/159bd4f24ecb21b9c76f76d27c1736ec1040f0ab
 * mapfishapp: use toponymName instead of name in GeoNames results, see https://github.com/georchestra/georchestra/issues/45
 * mapfishapp: WFS layer source server now correctly displayed, see https://github.com/georchestra/georchestra/commit/945349a1935286af2e02bfd21f9d7d9eeb6481e7
 * mapfishapp: Styler 2nd load timing out fixed, see https://github.com/georchestra/georchestra/commit/7b28656a2a81d01c00ebe0ff5a55e571f43aa63c
 * mapfishapp: download style styler link did not always provide the current layer style, see https://github.com/georchestra/georchestra/commit/5c47caa38b8c975982776f2a35c0574217bc2a17
 * mapfishapp: fixed XML documents missing the prolog, see http://applis-bretagne.fr/redmine/issues/4536
 * mapfishapp: WFS layer redraw was throwing an error, see http://applis-bretagne.fr/redmine/issues/4544
 * LDAP: group membership is now declared with memberUid = user uid rather than full dn, see https://github.com/georchestra/georchestra/pull/91

UPGRADING:
 * LDAP tree needs to be migrated as a result of https://github.com/georchestra/georchestra/pull/91 :
    * ldif export: ldapsearch -xLLL -D "cn=admin,dc=georchestra,dc=org" -W > dump.ldif
    * migration: sed 's/\(memberUid: uid=\)\(.*\)\(,ou=users,dc=georchestra,dc=org\)/memberUid: \2/' dump.ldif > new.ldif
    * ldif import
 * mapfishapp config changes:
    * the print config folder should be moved from YOUR_CONFIG/mapfishapp/print to YOUR_CONFIG/mapfishapp/WEB-INF/print for security reasons, see https://github.com/georchestra/georchestra/issues/82
    * client side (see GEOR_config.js or GEOR_custom.js for more information):
        * MAP_POS_SRS1 and MAP_POS_SRS2 options have been replaced with POINTER_POSITION_SRS_LIST
        * DEFAULT_WMC option has been replaced with CONTEXTS
        * PRINT_LAYOUTS_ACL allows to fine-tune available printing layouts based on user roles
        * DEFAULT_PRINT_FORMAT is now replaced by DEFAULT_PRINT_LAYOUT
        * DEACCENTUATE_REFERENTIALS_QUERYSTRING option added (controls whether to deaccentuate the referentials widget query string or not)
    * server side:
        * There is a new maven filter for mapfishapp temporary documents: shared.mapfishapp.docTempDir (defaults to /tmp/mapfishapp)
    * don't forget to edit your WMCs to activate back buffers on base layers, see https://github.com/georchestra/georchestra/pull/42
 * In GeoNetwork, it is now recommended to use OGC:WMS protocol rather than OGC:WMS-1.1.1-http-get-map (or any other WMS tagged with a version) to declare WMS layers, see https://github.com/georchestra/georchestra/pull/4
