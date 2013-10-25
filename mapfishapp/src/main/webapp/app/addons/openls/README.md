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

This will match the following response:

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<XLS version="1.2" xsi:schemaLocation="http://gpp3-wxs.ign.fr/schemas/olsAll.xsd" xmlns:xls="http://www.opengis.net/xls" xmlns="http://www.opengis.net/xls" xmlns:xlsext="http://www.opengis.net/xlsext" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <ResponseHeader/>
    <Response version="1.2" requestID="5">
        <GeocodeResponse>
            <GeocodeResponseList numberOfGeocodedAddresses="1">
                <GeocodedAddress>
                    <gml:Point>
                        <gml:pos>48.829311 2.374579</gml:pos>
                    </gml:Point>
                    <Address countryCode="StreetAddress">
                        <StreetAddress>
                            <Building number="13"/>
                            <Street>rue de tolbiac</Street>
                        </StreetAddress>
                        <Place type="Municipality">Paris</Place>
                        <Place type="Bbox">2.374215;48.829177;2.375391;48.829831</Place>
                    </Address>
                    <GeocodeMatchCode matchType="Street number" accuracy="1.0"/>
                </GeocodedAddress>
            </GeocodeResponseList>
        </GeocodeResponse>
    </Response>
</XLS>
```

 * **comboTemplate** - the template used to render each address in the combobox list. Defaults to "{number} {street} {municipality}".
 * **sortField** - the field used to sort the responses. The field has to be one of above GeocodedAddressFields. Defaults to ```accuracy```.
 * **xy** - whether the GML Point position is encoded with x (longitudes) or y (latitudes) first. Defaults to false.
 
Note: to customize the field mappings, read ExtJS's [DomQuery documentation](http://docs.sencha.com/extjs/3.4.0/source/DomQuery.html#Ext-DomQuery)