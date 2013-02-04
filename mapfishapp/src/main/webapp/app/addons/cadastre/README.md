Cadastre ADDON
==============

This addon allows users to locate land parcels, either by reference (tab 1), or by owner name (tab 2).
author: @fvanderbiest, with ideas from @spelhate.


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

By default, comboboxes are editable (which means that you can type ahead to filter the combo values). 
This behavior can be turned off by setting "editableCombos" to false in your addon options.


Tab 1
=====

Tab 1 can be configured with as many cascading fields as wanted, provided the field names ("field1", "field2", etc) are alphabetically ordered.
Each field can get its values either from a local static JSON file (eg: cities.json), or a WFS getFeature query.

A field object MUST have the following properties:
 * **valuefield** is the field value, which is used as the value of the filters for the next fields,
 * **displayfield** is the field to display once an item has been selected in the combobox.
 
A field object MUST have either:
 * a **file** property which points to a GeoJSON file (for instance, one provided by your build profile). In order to create such a file, we assume that you have a WFS layer serving your cities data. You'll typically want to send a POST content (with the Poster FireFox extension, or cURL) to /geoserver/wms such as:
 
        <wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" version="1.1.0" service="WFS" outputFormat="json"><wfs:Query typeName="ign:ign_bdparcellaire_communes" srsName="EPSG:2154"><ogc:PropertyName>code_insee</ogc:PropertyName><ogc:PropertyName>nom_com</ogc:PropertyName><ogc:PropertyName>code_dep</ogc:PropertyName><ogc:SortBy><ogc:SortProperty><ogc:PropertyName>nom_com</ogc:PropertyName><ogc:SortOrder>ASC</ogc:SortOrder></ogc:SortProperty></ogc:SortBy></wfs:Query></wfs:GetFeature>

 * or a **wfs** and a **typename** property, which resp. point to an OGC WFS server URL and a layer name.

A field object MAY have the following properties:
 * **template** is a template string for combos, in which store property names in curly brackets are replaced by their values,
 * **matchingproperties** is an object which specifies how the current field will be filtered with the values from the previous fields. There can be as many matchingproperties as previous fields. Eg: "field1": "code_insee" will instruct to filter the current field with values matching "code_insee = field1.valuefield"


Tab 2
=====

The idea behind tab 2 is that one first selects a city from a combobox, then a parcel owner name by typing the first letters.

Tab 2 is less configurable than tab 1:
 * it features only two static fields, 
 * the first field will be the same as the one from tab 1
 * the second field can be configured as the fields from tab 1, but it does not support the **template** nor the **file** property.
