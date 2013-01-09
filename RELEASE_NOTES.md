Version 13.02
=============

New features:
 * mapfishapp: lon, lat and radius GET parameters for startup recentering, see https://github.com/georchestra/georchestra/pull/20
 * mapfishapp: results panel displays URLs as links, see https://github.com/georchestra/georchestra/pull/21
 * mapfishapp: add layer from thesaurus: metadata title first, see https://github.com/georchestra/georchestra/pull/23
 * mapfishapp: switchable pointer coordinates SRS, see https://github.com/georchestra/georchestra/pull/25
 * mapfishapp: layers drag'n drop in layer manager, see http://applis-bretagne.fr/redmine/issues/1959
 * mapfishapp: OGC context switcher, see https://github.com/georchestra/georchestra/pull/26

Enhancements:
 * mapfishapp: more visible layer names, see https://github.com/georchestra/georchestra/pull/22
 * mapfishapp: add zoomout button in the toolbar, see https://github.com/georchestra/georchestra/pull/24
 * mapfishapp: missing translations

Bug fixes:
 * -


UPGRADING:
 * MAP_POS_SRS1 and MAP_POS_SRS2 options have been removed from GEOR.config / GEOR.custom, and replaced with POINTER_POSITION_SRS_LIST - see GEOR_config.js for more information
 * DEFAULT_WMC option has been removed from GEOR.config / GEOR.custom, and replaced with CONTEXTS - see GEOR_config.js for more information