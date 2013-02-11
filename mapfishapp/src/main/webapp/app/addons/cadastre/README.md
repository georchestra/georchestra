Cadastre ADDON
==============

This addon allows users to locate land parcels, either by reference (tab 1), or by owner name (tab 2).
author: @fvanderbiest, with ideas from @spelhate.

Tab availability can be restricted to specific roles (= LDAP groups prefixed with "ROLE_").
By default, tab2 is only available to admin users, while tab1 is allowed for everyone.

Typical configuration to include in your GEOR_custom.js file:

    {
        "id": "cadastre",
        "name": "Cadastre",
        "title": {
            "fr": "Cadastre",
            "en": "Cadastre",
            "es": "Catastro"
        },
        "description": {
            "fr": "Cet outil permet de chercher des parcelles soit par référence, soit par le nom du propriétaire",
            "en": "This tool allows one to search for land parcels, either by reference, or by owner name",
            "es": "Esta herramienta permite buscar parcelas ya sea por referencia o por el nombre del propietario"
        },
        "options": {
            "roles": {
                "tab1": [],
                "tab2": ["ROLE_SV_ADMIN"]
            },
            "tab1": {
                "field1": {
                    "file": "cities.json",
                    "valuefield": "code_insee",
                    "displayfield": "nom_com",
                    "template": "<b>{nom_com}</b> ({code_dep})"
                },
                "field2": {
                    "wfs": "http://ids.pigma.org/geoserver/ign/wfs",
                    "typename": "ign:ign_bdparcellaire_sections",
                    "matchingproperties": {
                        "field1": "code_insee"
                    },
                    "valuefield": "section",
                    "displayfield": "section",
                    "template": "<b>{section}</b>"
                },
                "field3": {
                    "wfs": "http://ids.pigma.org/geoserver/ign/wfs",
                    "typename": "ign:ign_bdparcellaire_localisants",
                    "matchingproperties": {
                        "field1": "code_insee",
                        "field2": "section"
                    },
                    "valuefield": "numero",
                    "displayfield": "numero",
                    "template": "<b>{numero}</b> {section}"
                }
            },
            "tab2": {
                "field2": {
                    "wfs": "http://ids.pigma.org/geoserver/cadastre/wfs",
                    "typename": "cadastre:localisants_bdparc_majic2012",
                    "matchingproperties": {
                        "field1": "code_insee"
                    },
                    "valuefield": "majic_ddenom",
                    "displayfield": "majic_ddenom"
                }
            }
        }
    }

This configuration is perfect for quick operations, when the WFS server is configured to send the feature bounding box, regardless of whether the geometry is fetched or not.

Few servers do not send the bbox. 
For those, it is required to trigger an additional XHR to fetch the geometry once a combo item is selected. 
Thus, we need an additional "geometry" property in the config for the geometry name. 
The config might then look like this:

    {
        "id": "cadastre",
        "name": "Cadastre",
        "title": {
            "fr": ...
        },
        "description": {
            "fr": ...
        },
        "options": {
            "tab1": {
                "field1": {
                    "file": "cities.json",
                    "geometry": "the_geom",
                    "wfs": "http://geobretagne.fr/geoserver/geob_loc/wfs",
                    "typename": "geob_loc:COMMUNE",
                    "valuefield": "INSE",
                    "displayfield": "COMMUNE",
                    "template": "<b>{COMMUNE}</b> ({INSE})"
                },
                "field2": {
                    "wfs": "http://geobretagne.fr/geoserver/ref/wfs",
                    "typename": "ref:cadastre_section",
                    "geometry": "the_geom",
                    "matchingproperties": {
                        "field1": "insee"
                    },
                    "valuefield": "texnumsect",
                    "displayfield": "texnumsect",
                    "template": "<b>{texnumsect}</b>"
                },
                ...
            },
            ...
        }
    }


By default, comboboxes are editable (which means that you can type ahead to filter the combo values). 
This behavior can be turned off by setting "editableCombos" to false in your addon options.


Tab 1
=====

Tab 1 can be configured with as many cascading fields as wanted, provided the field names ("field1", "field2", etc) are alphabetically ordered.
Each field can get its values either from a local static JSON file (eg: cities.json), or a WFS getFeature query.

A field object MUST have the following properties:
 * **valuefield** is the field value, which is used as the value of the filters for the next fields,
 * **displayfield** is the field to display once an item has been selected in the combobox.

If **geometry** is set (see below), the field object MUST have the additional two properties:
 * **wfs**, which points to an OGC WFS server URL,
 * **typename** is the layer name.

A field object MAY have the following properties:
 * **geometry** is the geometry column name. If specified, the real geometry (rather than the bbox) is fetched and displayed.
 * **template** is a template string for combos, in which store property names in curly brackets are replaced by their values,
 * **matchingproperties** is an object which specifies how the current field will be filtered with the values from the previous fields. There can be as many matchingproperties as previous fields. Eg: "field1": "code_insee" will instruct to filter the current field with values matching "code_insee = field1.valuefield"
 * a **file** property which points to a GeoJSON file (for instance, one provided by your build profile). This is to speedup operations, and also to prevent heavy requests. In order to create such a file, we assume that you have a WFS layer serving your cities data. You'll typically want to send a POST content (with the Poster FireFox extension, or cURL) to /geoserver/wms such as:
 
        <wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" version="1.1.0" service="WFS" outputFormat="json"><wfs:Query typeName="ign:ign_bdparcellaire_communes" srsName="EPSG:2154"><ogc:PropertyName>code_insee</ogc:PropertyName><ogc:PropertyName>nom_com</ogc:PropertyName><ogc:PropertyName>code_dep</ogc:PropertyName><ogc:SortBy><ogc:SortProperty><ogc:PropertyName>nom_com</ogc:PropertyName><ogc:SortOrder>ASC</ogc:SortOrder></ogc:SortProperty></ogc:SortBy></wfs:Query></wfs:GetFeature>


Tab 2
=====

The idea behind tab 2 is that one first selects a city from a combobox, then a parcel owner name by typing the first letters.

Tab 2 is less configurable than tab 1:
 * it features only two static fields, 
 * the first field will be the same as the one from tab 1
 * the second field can be configured as the fields from tab 1, but it does not support the **template** nor the **file** property.
