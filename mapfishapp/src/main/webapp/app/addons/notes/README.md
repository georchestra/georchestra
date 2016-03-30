# Notes

Mapfishapp addon to share notes about map issues.

Author: @Vampouille, @fvanderbiest & @jdenisgiguere .

## Multiple installations

Many instance of the add-on may be available on the server.

The following change are required on each copy of the configuration object
in the `config.json` file.

* set addon id to an unique value with the attribute `id`;
* set backend name (see server-side instructions)
with the attribute `options.backend`;
* set distinctive title and description with the attributes `title`
and `description` (in every supported languages);
* set the path to the icon relative to add-on root with the attribute
`options.icon`.