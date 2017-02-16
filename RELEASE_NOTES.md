The development branch is master. It can be used for testing and reporting errors.

For production systems, you are advised to use the stable branch (currently 16.12).
This branch receives bug fixes as they arrive, during 12 months at least.

Version 16.12
=============

This release represents one year of contributions. It's a HUGE one !  
Major highlights include:
 * a brand new console to manage efficiently users, orgs and roles
 * an atlas webapp, which allows users to have features from a layer printed as multipage PDF
 * many new features in our power viewer: map contexts manager, buffer, styling of vector features, new addons...
 * Docker as prefered, streamlined deployment procedure
 * [sdi consistence check](https://github.com/georchestra/sdi-consistence-check), a new project whose aim is to check links between data and metadata in a SDI.

Many thanks to those who made it possible, either through funding, or patches !


New features:
 * console webapp allows to manage not only users & roles but also organisations with their geographic area of competence - read more on [#1018](https://github.com/georchestra/georchestra/issues/1018).
 * atlas webapp and addon - see [#1017](https://github.com/georchestra/georchestra/issues/1017)
 * email proxy in ldapadmin - see [#1572](https://github.com/georchestra/georchestra/issues/1572)
 * extractions are now logged in a new db schema - [#1566](https://github.com/georchestra/georchestra/issues/1566) & [#1570](https://github.com/georchestra/georchestra/issues/1570)
 * mapfishapp - styling vector features which result from a query - [#1604](https://github.com/georchestra/georchestra/issues/1604)
 * mapfishapp - own contexts manager - [#1601](https://github.com/georchestra/georchestra/issues/1601)
 * mapfishapp - querier - supports buffers around drawn features - [#1347](https://github.com/georchestra/georchestra/issues/1347)
 * mapfishapp - querier - filters can be saved and loaded to/from XML files - [#1351](https://github.com/georchestra/georchestra/issues/1351)
 * mapfishapp addons:
   * GOTO - [#1550](https://github.com/georchestra/georchestra/issues/1550)
   * Coordinates - [#1549](https://github.com/georchestra/georchestra/issues/1549)
   * BAN - [#1518](https://github.com/georchestra/georchestra/issues/1518)
   * Notes - [#1341](https://github.com/georchestra/georchestra/issues/1341)
   * Measurements - [#1011](https://github.com/georchestra/georchestra/issues/1011)

Enhancements:
 * proxy - enable jetty gzip module to compress static files - [#1478](https://github.com/georchestra/georchestra/issues/1478)
 * geoserver - limit jetty threads in a docker context - [#1666](https://github.com/georchestra/georchestra/issues/1666)
 * mapfishapp - sorting contexts alphabetically based on their filename - [#1514](https://github.com/georchestra/georchestra/issues/1514)
 * mapfishapp - contexts are now uniformly refered to as "maps" - [#1640](https://github.com/georchestra/georchestra/issues/1640)
 * mapfishapp - introducing GEOR.config.METADATA_VIEW_BASE_URL - [#1481](https://github.com/georchestra/georchestra/issues/1481)
 * mapfishapp - load layers via the querystring - [#1469](https://github.com/georchestra/georchestra/issues/1469)
 * mapfishapp - permalink + export to external app now give the user the ability to fill in context title, abstract, keywords
 * mapfishapp - keywords are now serialized in contexts - [#1447](https://github.com/georchestra/georchestra/issues/1447)
 * mapfishapp - csw keyword browser is now optional - [#1445](https://github.com/georchestra/georchestra/issues/1445)
 * mapfishapp - allow to provide a debug version of an addon alongside a minified js  - [#1430](https://github.com/georchestra/georchestra/issues/1430)
 * mapfishapp - osm editors as addon instead of core feature - [#1389](https://github.com/georchestra/georchestra/issues/1389)
 * mapfishapp - querier in a floating window - [#1346](https://github.com/georchestra/georchestra/issues/1346)
 * mapfishapp - annotation addon - save and load features - [#1547](https://github.com/georchestra/georchestra/issues/1547)
 * mapfishapp - ACL for contexts - [#1298](https://github.com/georchestra/georchestra/issues/1298)
 * docker:
    * many improvements in dockerfiles and composition (syntax v2 now requires compose >= 1.6 and docker >= 1.10),
    * setup streamlined: static datadir is shipped in image, but it is overridable with a volume or a host folder, read the [how-to](https://github.com/georchestra/georchestra/blob/master/docs/docker.md),
    * docker volumes in `/mnt`, see [#1597](https://github.com/georchestra/georchestra/issues/1597) and [#1602](https://github.com/georchestra/georchestra/issues/1602).
 * a [Makefile](Makefile) is now available for those who'd like to build geOrchestra (but it's not recommended anymore, since it is **now fully configurable** through it's datadir and UIs)
 * ... and [many more](https://github.com/georchestra/georchestra/issues?q=milestone%3A16.12+is%3Aclosed+label%3Aenhancement) !


Bug fixes:
 * analytics - truncated layername when using QGIS as client - [#877](https://github.com/georchestra/georchestra/issues/877)
 * geoserver - restored ability to build extensions - [#1231](https://github.com/georchestra/georchestra/issues/1231)
 * mapfishapp - graphic scalebar is ~ 30% wrong - [#1535](https://github.com/georchestra/georchestra/issues/1535)
 * mapfishapp - printing now takes into account geodetic distances - [#1084](https://github.com/georchestra/georchestra/issues/1084)
 * mapfishapp - location comboboxes now resize with right panel - [#1459](https://github.com/georchestra/georchestra/issues/1459)
 * mapfishapp - fix thesaurus query with GeoNetwork 3 - [#1333](https://github.com/georchestra/georchestra/issues/1333)
 * extractorapp - failure to retrieve linked metadata does not make the extraction fail anymore - [#357](https://github.com/georchestra/georchestra/issues/357)
 * ... and [many more](https://github.com/georchestra/georchestra/issues?q=milestone%3A16.12%20label%3Abug%20is%3Aclosed) !

Read [how to migrate from 15.12 to 16.12](migrations/16.12/README.md).


Version 15.12
=============

This release received important contributions from the community.
The major one being that the project delivers generic artifacts (WARs, Debian & RPM packages, Docker images), which allow for a wider distribution by removing the need for compilation.
A strong focus was also laid on infrastructure, with the integration of the [docker image generation process](https://github.com/georchestra/georchestra/issues/1178) in the main repository and also with the [automation of the install process allowed by ansible](https://github.com/georchestra/ansible).


New features:
 * generic wars [#94](https://github.com/georchestra/georchestra/issues/94) and [build.georchestra.org](https://build.georchestra.org/)
 * docker images [#1178](https://github.com/georchestra/georchestra/issues/1178)
 * geonetwork 3.0.4 integration [#1120](https://github.com/georchestra/georchestra/issues/1120)
 * geoserver 2.8.2 and geofence 3 integration [#1135](https://github.com/georchestra/georchestra/issues/1135)
 * geowebcache standalone 1.8.0 integration
 * osm2geor addon [#1031](https://github.com/georchestra/georchestra/pull/1031) & [#1074](https://github.com/georchestra/georchestra/issues/1074) & [README](mapfishapp/src/main/webapp/app/addons/osm2geor/README.md)
 * fullscreen addon [#981](https://github.com/georchestra/georchestra/issues/981) & [README](mapfishapp/src/main/webapp/app/addons/fullscreen/README.md)
 * locateme addon [#1079](https://github.com/georchestra/georchestra/pull/1079) & [README](mapfishapp/src/main/webapp/app/addons/locateme/README.md)
 * mapfishapp: simplified metadata view [#1016](https://github.com/georchestra/georchestra/issues/1016)
 * mapfishapp: dynamic contexts and addons discovery [#40](https://github.com/georchestra/georchestra/issues/40) & [#1255](https://github.com/georchestra/georchestra/pull/1255)
 * ldapadmin: export to CSV or Vcard [#1104](https://github.com/georchestra/georchestra/pull/1104)

Enhancements:
 * extractorapp and mapfishapp now use the `wfs-ng` geotools module: [#1267](https://github.com/georchestra/georchestra/pull/1267)
 * analytics: improved performance on huge databases [#556](https://github.com/georchestra/georchestra/issues/556)
 * ogc-server-statistics: logs user roles too [#1108](https://github.com/georchestra/georchestra/issues/1108)
 * ldapadmin: administrator can modify a user's `uid` and the user is informed by email [#1109](https://github.com/georchestra/georchestra/pull/1109)
 * ldapadmin: new `TEMPORARY` virtual group, holding users from the `shadowAccount` objectClass with a `shadowExpire` attribute.
 * header: new sub-menu dedicated to administrators [#1010](https://github.com/georchestra/georchestra/issues/1010)
 * mapfishapp: improved addons init [#1006](https://github.com/georchestra/georchestra/issues/1006)
 * mapfishapp: measurement tools are now an addon, see the [README](mapfishapp/src/main/webapp/app/addons/measure/README.md)
 * mapfishapp: layergroup detection and reduced functionalities [#1091](https://github.com/georchestra/georchestra/pull/1091)
 * extractorapp: text file for failure instead of html file [#1272](https://github.com/georchestra/georchestra/pull/1272)
 * security-proxy: does not require clients presenting the `jakarta` or `apache-httpclient` user-agent to provide a valid basic auth [#960](https://github.com/georchestra/georchestra/issues/960)
 * security-proxy: fix user-agent matching/authorization checking flow [#1188](https://github.com/georchestra/georchestra/pull/1188)
 * ldap: search fields indexed [#881](https://github.com/georchestra/georchestra/issues/881)
 * dev: add configuration to run geOrchestra with multiple jetty instances [#1112](https://github.com/georchestra/georchestra/pull/1112)

Bug fixes:
 * security-proxy: fixed a long-standing issue with sessions [#1069](https://github.com/georchestra/georchestra/issues/1069). They now last 24 hours by default [#1177](https://github.com/georchestra/georchestra/issues/1177)
 * ogc-server-statistics: does not log OGC requests to external servers anymore [#1058](https://github.com/georchestra/georchestra/issues/1058)
 * ldapadmin: blanking a field is now allowed [#1086](https://github.com/georchestra/georchestra/issues/1086)
 * ldapadmin: now allows using the plus char in email address [#1128](https://github.com/georchestra/georchestra/issues/1128)
 * ldapadmin: fixed SMTP servers refusing ICMP protocols makes the deploy fail [#991](https://github.com/georchestra/georchestra/issues/991)
 * ldapadmin: fixed group filter not applying correctly on selections [56e350](https://github.com/georchestra/georchestra/commit/56e35057f7a347faee4fd4f117ca5138b4897d4a)
 * ldapadmin: members of the PENDING group are not able to reset their password anymore [#1002](https://github.com/georchestra/georchestra/issues/1002)
 * ldapadmin: API returns json content-type [#1115](https://github.com/georchestra/georchestra/issues/1115)
 * ldapadmin: fixed encoding of email in URL [#1129](https://github.com/georchestra/georchestra/pull/1129)
 * ldapadmin: set restangular version in CDN [#1270](https://github.com/georchestra/georchestra/issues/1270)

According to our release policy, geOrchestra 14.06 is not supported anymore.

Read [how to migrate from 15.06 to 15.12](migrations/15.12/README.md).


Version 15.06
=============

This release brings several key enhancements:
 * support for the newer Debian 8 "Jessie", including tomcat 7, 8 & apache 2.4,
 * standard geoserver artifacts can now be used instead of the "geOrchestra flavoured geoserver",
 * [travis-ci](https://travis-ci.org/georchestra/georchestra) service integration, with improved test coverage,
 * lighter artifacts.

Other enhancements include:
 * mapfishapp: redirect to iD as OSM editor - see [#992](https://github.com/georchestra/georchestra/issues/992),
 * mapfishapp: gml & kml export of results - see [#995](https://github.com/georchestra/georchestra/pull/995),
 * mapfishapp: the admin may restrict all metadata searches to a specific extent, while the user may now restrict his search to the current map extent - see [#964](https://github.com/georchestra/georchestra/pull/964),
 * mapfishapp: added possibility to filter visible features in POSTed layers with CQL - see [#921](https://github.com/georchestra/georchestra/pull/921),
 * proxy: intercepts "connection refused" errors - see [#963](https://github.com/georchestra/georchestra/pull/963),
 * doc: added SASL configuration for remote AD - see [#958](https://github.com/georchestra/georchestra/pull/958).

Full list of issues on GitHub issue tracker, tagged with milestone [15.06](https://github.com/georchestra/georchestra/issues?q=milestone%3A15.06).

Please note that:
 * the protocol between the security proxy and the apps was slightly modified as a result of [#920](https://github.com/georchestra/georchestra/issues/920). This means that the proxy and webapps from this release are not backward-compatible: you cannot use mapfishapp from 15.06 with a 14.12 security-proxy.
 * geOrchestra 14.01 is not supported anymore.

Many thanks to all contributors for this great release !


Version 14.12
=============

This release received contributions from the GeoBretagne, GéoPicardie, CIGALsace, CRAIG, Rennes Métropole, Le Puy en Velay and Université de Franche-Comté projects.
It comes with an improved [install documentation](README.md).

According to our release policy, geOrchestra 13.09 is not supported anymore.

New features:
 * extractorapp: native raster resolution extracted by default - see [#726](https://github.com/georchestra/georchestra/issues/726),
 * geofence: ability to configure the map SRS - see [#732](https://github.com/georchestra/georchestra/issues/732),
 * geoserver: updated to version 2.5.4 with geofence - see [#677](https://github.com/georchestra/georchestra/issues/677),
 * ldapadmin: identify users not members of a SV_* group - see [#834](https://github.com/georchestra/georchestra/issues/834),
 * mapfishapp: context restoring - option to reset map or not - see [#302](https://github.com/georchestra/georchestra/issues/302),
 * mapfishapp: spatial query using previous results' geometries - see [#752](https://github.com/georchestra/georchestra/issues/752),
 * mapfishapp: streetview addon - see [#774](https://github.com/georchestra/georchestra/issues/774),
 * mapfishapp: direct geodata file loading via URL - see [#754](https://github.com/georchestra/georchestra/issues/754),
 * mapfishapp: added a button to remove all layers - see [#753](https://github.com/georchestra/georchestra/issues/753),
 * mapfishapp: new syntax for CSW querier - see [#760](https://github.com/georchestra/georchestra/issues/760) and the [README](https://github.com/georchestra/georchestra/blob/master/mapfishapp/README.md#cswquerier),
 * mapfishapp: png printing - see [#814](https://github.com/georchestra/georchestra/issues/814),
 * mapfishapp: print extent handling improvement - see [#813](https://github.com/georchestra/georchestra/issues/813),
 * mapfishapp: give easy access to WMS/WFS layer details - see [#346](https://github.com/georchestra/georchestra/issues/346),
 * mapfishapp: export current map context as a "map metadata" - see [#751](https://github.com/georchestra/georchestra/issues/751),
 * mapfishapp: revamp of the "add layers" window - see [#718](https://github.com/georchestra/georchestra/issues/718),
 * mapfishapp: help message popping down when tools are loaded and available,
 * mapfishapp: if filled, wmc title and abstract are now displayed on context restored - see [#816](https://github.com/georchestra/georchestra/issues/816),
 * mapfishapp: filter contexts by keywords - see [#866](https://github.com/georchestra/georchestra/issues/866).

Enhancements:
 * extractorapp: editable combobox for resolution selection - see [#726](https://github.com/georchestra/georchestra/issues/726),
 * extractorapp: warn user when the extraction area is too large before the extraction is fired - see [#726](https://github.com/georchestra/georchestra/issues/726),
 * extractorapp: better emails - see [#750](https://github.com/georchestra/georchestra/issues/750),
 * ldapadmin: test suite improved - see [#843](https://github.com/georchestra/georchestra/pull/843),
 * mapfishapp: print extent fully visible,
 * mapfishapp: print templates improved - see [#17](https://github.com/georchestra/template/pull/17),
 * mapfishapp: geometry from query stored forever in browser/LocalStorage (rather than cookie),
 * mapfishapp: warn users about chrome 36/37 issue,
 * mapfishapp: filter out WFS services not supporting POST,
 * mapfishapp: i18n neutral nopreview image for csw querier - see [#775](https://github.com/georchestra/georchestra/issues/775),
 * mapfishapp: load data from catalog improvement - see [#756](https://github.com/georchestra/georchestra/issues/756),
 * mapfishapp: advanced csw querier syntax help - see [#478](https://github.com/georchestra/georchestra/issues/478),
 * mapfishapp: addons can be placed in toolbars now - see [#132](https://github.com/georchestra/georchestra/issues/132),
 * proxy: handle a list of servers for which the x-forwarded headers should not be sent (typically geo.admin.ch) - see [#782](https://github.com/georchestra/georchestra/issues/782),
 * georchestra: german translations generalized - see [#777](https://github.com/georchestra/georchestra/issues/777),
 * georchestra: the maven build is now aware of the javascript build outcome - see [#809](https://github.com/georchestra/georchestra/issues/809).

Bug fixes:
 * analytics: fixed global csv export - see [#835](https://github.com/georchestra/georchestra/issues/835),
 * analytics: fixed log4j config issue - see [#785](https://github.com/georchestra/georchestra/issues/785),
 * analytics:  fixed broken double click to see details - see [#883](https://github.com/georchestra/georchestra/issues/883),
 * build: fixed by migrating from OpenGeo to Boundless repository and also by setting up our own repository,
 * cas: fixed "after logging in from /cas/login the header does not show the connected user" - see [#837](https://github.com/georchestra/georchestra/issues/837),
 * catalogapp: fixed missing log4j configuration file - see [#861](https://github.com/georchestra/georchestra/issues/861),
 * config: fixed proxy targets wrong connector for geoserver - see [c2dca7](https://github.com/georchestra/template/commit/c2dca72c647e9e33662655232212601fb5f5ac45),
 * epsg-extension: fixed the EPSG:27572 SRS for use in mapfishapp and extractorapp - see [#379](https://github.com/georchestra/georchestra/issues/379),
 * extractorapp: workaround a geoserver issue for EPSG:4326 bboxes,
 * extractorapp: fixed WCS specific regexp in checkPermission method,
 * geonetwork: fixed LDAP sync - see [6a7e69](https://github.com/georchestra/geonetwork/commit/6a7e692daaabe3b27793b4c75de2bc91ffe43840),
 * geonetwork: in associated resource panel from editor, fixed failure to add link to service metadata record,
 * geonetwork: schema iso / fra - fixed missing description for "funder",
 * ldapadmin: fixed renaming group name leads to corrupted LDAP entry - see [#810](https://github.com/georchestra/georchestra/issues/810),
 * ldapadmin: fixed unescaped quotes in translations - see [#890](https://github.com/georchestra/georchestra/issues/890),
 * mapfishapp: fixed js error on vector layer selection - see [#785](https://github.com/georchestra/georchestra/issues/785),
 * mapfishapp: fixed missing SLD_VERSION WMS parameter, which is mandatory from version 1.3.0 on - see [#636](https://github.com/georchestra/georchestra/issues/636),
 * mapfishapp: modified MAP_DOTS_PER_INCH value in order to fix WMS/WMTS layer overlay - see [#736](https://github.com/georchestra/georchestra/issues/736),
 * mapfishapp: fixed WMTS style switch for GeoWebCache versions >= 1.5,
 * mapfishapp: fixed WMTS legend printing issue,
 * mapfishapp: workaround for IE11 bug, see [#773](https://github.com/georchestra/georchestra/issues/773),
 * mapfishapp: fixed several programming errors in the cadastre addon,
 * mapfishapp: restore record opaque status from layer transitionEffect,
 * mapfishapp: fixed the csw queryable "Type" case,
 * mapfishapp: fixed erroneous area measurements - see [#838](https://github.com/georchestra/georchestra/issues/838),
 * mapfishapp: fixed spanish lang file formatting - see [043ef1](https://github.com/georchestra/georchestra/commit/043ef1eeee0865a3e4028cc1509ae5d9f4526bd9),
 * proxy: fixed a vulnerability where client could access localhost resources via proxy url, see [5c9b4d](https://github.com/georchestra/georchestra/commit/5c9b4db1a8c004a582d2be4f2a909c68843cad59),
 * proxy: prevented the use of the ogcproxy in production (it is required for development purposes only),
 * proxy: restored access to public layers through qgis, udig and arcgis clients - see [b077f8](https://github.com/georchestra/georchestra/commit/b077f8e6695ea807d18342b35c63c1843f457ee0),
 * proxy: checkhealth now targets the main geOrchestra database by default, see [a1d825](https://github.com/georchestra/template/commit/a1d825ecdda2c1f89ec78039d95ab17ba2f47310),
 * proxy: force preemptive basic auth for GeoFence client to GeoServer REST - see [2c2b74](https://github.com/georchestra/georchestra/commit/2c2b74b5bf813e184a87e032c07cc4276a3f66a8),
 * server-deploy: fixed wrong webapp names (geoserver-webapp, geofence-webapp, geowebcache-webapp),
 * georchestra: many small fixes related to HTTPS support, eg [#745](https://github.com/georchestra/georchestra/issues/745), [#840](https://github.com/georchestra/georchestra/issues/840) and [#780](https://github.com/georchestra/georchestra/issues/780),
 * georchestra: css3 border-radius property replaces browser (moz and webkit) implementations.

Read [how to migrate from 14.06 to 14.12](migrations/14.12/README.md).


Version 14.06
==============

Contributions from Rennes, CIGAL, GeoBretagne, GeoPicardie, PIGMA, GeoBolivia, ViennAgglo & developers on their free time.  
Note on the 13.06 release: end-of-life was in april, earlier this year. As a result, it will not receive bug fixes anymore.

New features:
 * GeoWebCache standalone is now part of the geOrchestra suite

Enhancements:
 * analytics: access is now restricted to members of the ```MOD_ANALYTICS``` group **only**,
 * cas: upgraded to latest CAS Server 4.0.0 release - see [#503](https://github.com/georchestra/georchestra/issues/503),
 * cas: members of the ```PENDING_USERS``` group are not allowed to login anymore - see [#581](https://github.com/georchestra/georchestra/issues/581),
 * cas: unmodified files where removed from template config,
 * cas: username field autocompleted on browsers that support it,
 * catalogapp: german translation,
 * deploy: a default [deployment script](https://github.com/georchestra/template/blob/master/DeployScript.groovy) is now provided by the template config,
 * downloadform: complete revamp, including more testing, see [#657](https://github.com/georchestra/georchestra/issues/657),
 * extractorapp: now displays layers using the configured ```GLOBAL_EPSG``` srs, if they support it,
 * extractorapp: much improved test coverage,
 * extractorapp: access is now restricted to members of the ```MOD_EXTRACTORAPP``` group **only**,
 * extractorapp admin: access is now restricted to members of the ```ADMINISTRATOR``` group **only**,
 * extractorapp: german translation,
 * extractorapp: the privileged user is allowed to use imp-username/imp-roles headers to impersonate an arbitrary user for security check,
 * extractorapp: improved GDAL/OGR handling,
 * extractorapp: proper reporting in case the OGRDataStore cannot be created, with an invitation to check the GDAL libraries setup,
 * extractorapp: dropped support for geoserver 1.x, which results in better startup performance, see [#640](https://github.com/georchestra/georchestra/issues/640),
 * extractorapp: improved db connection pooling wrt download form,
 * extractorapp admin: includes the standard geOrchestra header - see [#729](https://github.com/georchestra/georchestra/issues/729),
 * geonetwork: rolling logs + gzipping - see [#200](https://github.com/georchestra/georchestra/issues/200),
 * geonetwork: improved IE10 compatibility,
 * geonetwork: disable user management for non-administrators,
 * geonetwork: improved links to WMS and WMC got from ISO19139 records, "distribution" section - see [5e7671](https://github.com/georchestra/geonetwork/commit/5e7671e5c498cc18f0150dd33e5b123553161453) and [c53e77](https://github.com/georchestra/geonetwork/commit/c53e7748938fb82afc2786614b228175172ad744),
 * geonetwork: added the possibility to search on a field and suggest on another one - see [4a3a34](https://github.com/georchestra/geonetwork/commit/4a3a34b031f2e1ceeec860e7cb5bb162da76fafa),
 * geonetwork: improved iso19139.fra schema - see [62057e](https://github.com/georchestra/geonetwork/commit/62057e5ce6db4cfaafd67c288df20c1b7511ceeb), [fd086f](https://github.com/georchestra/geonetwork/commit/fd086f69161032f5fdfa34d2f29e90dfec0b2097) and [0eba33](https://github.com/georchestra/geonetwork/commit/0eba333269bce331e46f79c8a461f822544f0ec0),
 * geoserver: the project now provides a [basic data_dir](https://github.com/georchestra/geoserver_minimal_datadir) with sensible defaults for geOrchestra - see [INSTALL.md#geoserver](INSTALL.md#geoserver),
 * geofence: performance improvement for GetCapabilities requests,
 * geofence: allows dynamic styles and remote layers by default,
 * geofence: conforms to the global logging strategy as set by shared maven filters,
 * geofence: allow [dynamic geofencing](https://github.com/georchestra/geofence/blob/georchestra/georchestra.md#static--dynamic-geofencing) based on inetOrgPerson's "l" field storing the limiting EWKT geometry,
 * header: only members of the ```MOD_EXTRACTORAP``` group see the link to the extractor - see [#717](https://github.com/georchestra/georchestra/issues/717),
 * ldapadmin: improved compatibility with geofence, by automatic management of unique numeric identifiers for users and groups,
 * ldapadmin: SSHA password encryption,
 * ldapadmin: much improved test coverage,
 * ldapadmin: now takes into account shared.ldap.* values,
 * ldapadmin: now uses configured LDAP users and groups DN instead of hardcoded values,
 * ldapadmin: public context path now set via a shared maven filter - see [#721](https://github.com/georchestra/georchestra/issues/721)
 * ldapadmin: hyphens are now allowed in user identifiers - see [#725](https://github.com/georchestra/georchestra/pull/725),
 * mapfishapp: improved testsuite coverage,
 * mapfishapp: annotation addon improved (outline and fill colors added),
 * mapfishapp: using adequate XML exception format string, depending on WMS version,
 * mapfishapp: max_features and max_length set to higher numbers,
 * mapfishapp: german translation, including for the supported addons (thanks to CIGALsace),
 * mapfishapp: display SLD title before its name - see [#597](https://github.com/georchestra/georchestra/issues/597),
 * mapfishapp: file upload is now using a pure JSON implementation,
 * mapfishapp: file upload checks that a SRS can be guessed by OGRFeatureReader, or else falls back onto the pure GeoTools implementation,
 * mapfishapp: file upload supports OSM files to some extent,
 * mapfishapp: proj4js definitions are now local to prevent mixed active content warning in case of HTTPS access, see [#602]( https://github.com/georchestra/georchestra/issues/602),
 * mapfishapp: only members of the ```MOD_EXTRACTORAPP``` group see the "download data" link - see [#717](https://github.com/georchestra/georchestra/issues/717)
 * mapfishapp: the "remove layer" icon was changed - see [#728](https://github.com/georchestra/georchestra/issues/728)
 * server-deploy: now supports deploying geofence-webapp,
 * all modules now use the same "georchestra" database by default, including GeoNetwork - see [#601](https://github.com/georchestra/georchestra/issues/601),
 * system wide favicon.

Bug fixes:
 * extractorapp: fixed several client-side issues regarding handling of namespaced layer names and virtual services - see [#517](https://github.com/georchestra/georchestra/issues/517#issuecomment-40697504) and [#634](https://github.com/georchestra/georchestra/issues/634),
 * extractorapp: fixed too early expiration of archive download link,
 * extractorapp: fixed protected layer cannot be extracted - see [#633](https://github.com/georchestra/georchestra/issues/633),
 * extractorapp: fixed KML export. Note: MIF export via OGR can only work with a patched version of GDAL, or GDAL >= 1.11.1, see [gdal#5477](http://trac.osgeo.org/gdal/ticket/5477),
 * geofence: avoid NPE with dynamic geofencing - [#629](https://github.com/georchestra/georchestra/issues/629),
 * geonetwork: shared.geonetwork.language enforced - see [#595](https://github.com/georchestra/georchestra/issues/595),
 * geonetwork: keywords-based extent suggestion does not fail anymore with aceentuated chars - see [652e87](https://github.com/georchestra/geonetwork/commit/652e87c073e438d9b75150fc786c823f6c346467),
 * geonetwork: fixed extent map cursor position after panel has been scrolled - see [95aa65](https://github.com/georchestra/geonetwork/commit/95aa659dc0def87dab33642b9d761d08c746700a),
 * geonetwork: fixed classification of spatial data service for better INSPIRE compatibility - see [c1017d](https://github.com/georchestra/geonetwork/commit/c1017d41971219369ed2671c5d1132e6167c4e90),
 * geonetwork: avoid JS error when the WMS layer title contains a quote - see [7bbc34](https://github.com/georchestra/geonetwork/commit/7bbc34c902b1390935799f93aa6e3f7d90edd092),
 * ldapadmin: fixed wrong users count in groups, in case several of them share the same ```sn``` field - see [0d7ab2](https://github.com/georchestra/georchestra/commit/0d7ab2ae1767e13af17d7f7d5f87ad22b728162d),
 * ldapadmin: group description editing does not detach users from group anymore - see [#650](https://github.com/georchestra/georchestra/issues/650),
 * mapfishapp: restored GetFeatureiInfo queries on aggregated layers - see [#658](https://github.com/georchestra/georchestra/issues/658),
 * mapfishapp: truly independant tabs in resultspanel - see [#671](https://github.com/georchestra/georchestra/issues/671),
 * mapfishapp: fixed missing drillDown option for getFeatureInfo control - see [#674](https://github.com/georchestra/georchestra/issues/674),
 * mapfishapp: multilayer querier tool - several issues fixed,
 * mapfishapp: fixed file upload limits values (8Mb limit really enforced) - see [#592](https://github.com/georchestra/georchestra/issues/592),
 * mapfishapp: fixed thesaurus keywords request lang - see [#624](https://github.com/georchestra/georchestra/issues/624),
 * mapfishapp: compatibility with WMS 1.1.0 servers - see [#663](https://github.com/georchestra/georchestra/issues/663),
 * mapfishapp: fixed IE10-specific issue with Ext.data.XmlReader,
 * proxy: ignores non HTTPS request check, to allow OGC web service usage, see [a24c74](https://github.com/georchestra/georchestra/commit/a24c7427a484028a5be211c3d6cbe516dbf2c04b),
 * proxy: returns earlier in case of 403|404 error code - see [#506](https://github.com/georchestra/georchestra/issues/506),
 * proxy: really customizable error pages,
 * proxy: always remove incoming sec-* headers,
 * proxy: members of the ```ADMINISTRATOR``` group do not need to belong to another ```SV_*``` group anymore - see [ad6784](https://github.com/georchestra/georchestra/commit/ad67845c41af94ee9f2636cc54da7fe88d29c22b),
 * proxy: added QuantumGIS to the list of clients triggering basic auth, thus allowing access to protected layers (along with uDig and ArcGIS clients).


## INSTALLING:

Please refer to [this guide](INSTALL.md).


Read [how to migrate from 14.01 to 14.06](migrations/14.06/README.md).



Version 14.01
==============

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


Read [how to migrate from 13.09 to 14.01](migrations/14.01/README.md).


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


Read [how to migrate from 13.06 to 13.09](migrations/13.09/README.md).


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

Read [how to migrate from 13.02 to 13.06](migrations/13.06/README.md).



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
 
Read [how to migrate to 13.02](migrations/13.02/README.md).
