# Configuring datahub

The `default.toml` file is the main configuration file for the datahub. It is located in the `datahub/conf` subfolder of the [georchestra datadir](https://github.com/georchestra/datadir/). Below are the main sections in the file.

Full configuration can be found in the [geonetwork-ui documentation](https://geonetwork.github.io/geonetwork-ui/main/docs/guide/configure.html)

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

* the parameters `max_zoom`, `max_extent` and `external_viewer_open_new_tab` are obvious.
* If a WMS web service has been declared as a resource in the dataset, it can be pushed into a map viewer (in a new tab: `external_viewer_url_template = true` ). The `external_viewer_url_template` parameter allows to define the URL to this viewer displaying the selected layer.


## Translations

Translations can be set for each key available in [`translations/`](https://github.com/geonetwork/geonetwork-ui/tree/main/translations) folder.

# Organizations and images synchronization.

By default, in geOrchestra, organizations are synchronized with the console application and create associated groups in Geonetwork. 

In datahub, the organizations are, by default, retrieved from the first resource contact of the metadata. This can result in different names than the ones in GeoNetwork's group.

## Recommended method

To ensure images for organizations in datahub, it is recommended to set up images in one or more of the metadata that create a datahub organization.

In full view edition, it is located in:
- For a 19115-3 metadata: `Identification` tab > `Point of contact` > `Responsibility` > `Party` > `Organization` > `Logo`
- For a 19139 metadata: `Identification` tab > `Point of contact` > `Responsible party` > `Contact information` > `Contact` > `Contact instructions`

## Other method

Alternatively, you can create a group in GeoNetwork with the same name as the organization in the metadata and set a logo for it.

This method is not recommended as it can lead to inconsistencies between the console and GeoNetwork.