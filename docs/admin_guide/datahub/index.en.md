# Configuring datahub

The `default.toml` file is the main configuration file for the datahub. It is located in the `datahub/conf` subfolder of the [georchestra datadir](https://github.com/georchestra/datadir/). Below are the main sections in the file.

## Global settings

The most important parameter is `geonetwork4_api_url` which should point at the API of the geonetwork instance. The default value `/geonetwork/srv/api` is a sensible one if datahub is installed on the same domain as geonetwork.

For the `metadata_language` there are 3 options:

  * either specify a ISO 639 language code, eg: `fre` or `eng` or `ger` - in this case only datasets matching this language code will be returned.
  * `current` - only datasets matching the current interface language will be shown.
  * If commented out, no language preference is applied for the search wich means, all languages are shown.

The other options can probably be left unmodified.

## Theme

Here are the main options:

* `primary_color` applies to the first line of the title.
* `secondary_color` applies to the second line of the title.
* `main_color` applies to the dataset description.
* `thumbnail_placeholder` it is recommanded to set it so that datasets without thumbnail or a broken thumbnail are shown with this fallback image.

## Search

There are several options to fine tune the search experience.

### More relevant search results

The datahub has an option to boost search results which are more relevant to the end user, based on a geographic perimeter.
geOrchestra leverages this option by offering a service which dynamically provides the organization's area of competence for the connected user (Note that this depends on the console application's configuration, as the `competenceAreaEnabled` option is set to `false` by default).
This allows for a dynamic boost of search results which are supposed to be more relevant to the end user.

These 2 options are mutually exclusive (one only should be present in the configuration file):

 * `filter_geometry_data` - if set to a static GeoJSON object, all datasets contained **inside** the Polygon geometry will be boosted on top.
 * `filter_geometry_url` - if set to ``"/console/account/areaofcompetence"``, the datahub uses the current (connected) user's organization's perimeter to boost search results.

 Whatever the selected option, results will be shown in the following order:

 * results contained **inside** the geometry
 * results **intersecting** the geometry
 * all other results

## Metadata quality widget

## Map settings

## Translations
