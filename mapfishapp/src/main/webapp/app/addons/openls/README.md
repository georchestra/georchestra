OpenLS ADDON
===============

author: @fvanderbiest

The addon config should look like this:

    {
        "id": "openls_0",
        "name": "OpenLS",
        "title": {
            "en": "OpenLS recentering",
            "es": "OpenLS recentering",
            "fr": "Recentrage OpenLS"
        },
        "description": {
            "en": "OpenLS recentering",
            "es": "OpenLS recentering",
            "fr": "Recentrage OpenLS"
        },
        "options": {
            "serviceURL": "http://my.server.tld/openls"
        }
    }

Options
========

Mandatory options:
 * **serviceURL** - points to your OpenLS service
 
Options to customize the behaviour:
 * **GeocodedAddressFields** - describe the fields available in the GeocodedAddress tag of the OpenLS GeocodeResponse. By default, we assume the following fields and mappings:
 
```
    "GeocodedAddressFields": [
        {"name": "street", "mapping": "Address > StreetAddress > Street"},
        {"name": "number", "mapping": "Address > StreetAddress > Building/@number"},
        {"name": "municipality", "mapping": "Place[type=Municipality]"},
        {"name": "bbox", "mapping": "Place[type=Bbox]"}, 
        {"name": "accuracy", "mapping": "GeocodeMatchCode/@accuracy"}
    ]
```

 * **comboTemplate** - the template used to render each address in the combobox list. Defaults to "{number} {street} {municipality}".
 * **sortField** - the field used to sort the responses. The field has to be one of above GeocodedAddressFields. Defaults to ```accuracy```.
 * **xy** - whether the GML Point position is encoded with x (longitudes) or y (latitudes) first. Defaults to false.
 
Note: to customize the field mappings, read ExtJS's [DomQuery documentation](http://docs.sencha.com/extjs/3.4.0/source/DomQuery.html#Ext-DomQuery)