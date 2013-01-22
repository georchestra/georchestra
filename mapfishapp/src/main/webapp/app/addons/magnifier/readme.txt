Put this in GEOR_custom.js ADDONS variable

For static Magnifier
-------------------------------------------------------------------------------------------
{
    name: "magnifier",
    title: "Loupe dynamique",
    thumbnail: "img/osm.png",
    description: "Un super outil qui permet de grossir une zone du fond carto courant",
    options: {
        mode: "static",
        layer: "satellite",
        format: "image/jpeg",
        buffer: 8,
        wmsurl: "http://tile.geobretagne.fr/gwc02/service/wms"
    }
}

-------------------------------------------------------------------------------------------
For dynamic Magnifier
-------------------------------------------------------------------------------------------
{
    name: "magnifier",
    title: "Loupe dynamique",
    thumbnail: "img/osm.png",
    description: "Un super outil qui permet de grossir une zone du fond carto courant",
    options: {
        mode: "dynamic",
        buffer: 8
    }
}
-------------------------------------------------------------------------------------------

Magnifier control comes from https://github.com/fredj/openlayers-magnifier