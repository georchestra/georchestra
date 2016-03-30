# Notes

Mapfishapp addon to share notes about map issues.

Author: @Vampouille, @fvanderbiest & @jdenisgiguere .

## Multiple installations

Many copies of the add-on may be installed on the same server

The following change are required on each copy:

### In the `config.json` file:

* set addon id to an unique value with the attribute `id`;
* set backend name (see server-side instructions)
with the attribute `options.backend`;
* set distinctive title and description with the attributes `title`
and `description` (in every supported languages).

### Set different icons

* distinctive icons must be added to the `img` subdirectory;
* reference to the icons must be set in the file `css/main.css`.