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

* `primary_color` applies to the first line of the title.
* `secondary_color` applies to the second line of the title.
* `main_color` applies to the dataset description.
* `thumbnail_placeholder` it is recommanded to set it so that datasets without thumbnail or a broken thumbnail are shown with this fallback image.

## Search

## Metadata quality widget

## Map settings

## Translations

