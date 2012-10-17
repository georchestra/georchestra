Put this in GEOR_custom.js SECTION ADDONS_LIST

For static Magnifier
-------------------------------------------------------------------------------------------
{
    module: "magnifier",
    js: ["app/addons/magnifier/Magnifier.js", "app/addons/magnifier/GEOB_magnifier.js"],
    css: ["app/addons/magnifier/magnifier.css"],
    options: {
        title: "Loupe ortho",
        abstract: "Afficher l'ortho dans une fenêtre de balayage",
        roles: [],
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
    module: "magnifier",
    js: ["app/addons/magnifier/Magnifier.js", "app/addons/magnifier/GEOB_magnifier.js"],
    css: ["app/addons/magnifier/magnifier.css"],
    options: {
        title: "Magnifier",
        abstract: "Magnifier",
        roles: [],
        mode: "dynamic",
        buffer: 8
    }
}
-------------------------------------------------------------------------------------------