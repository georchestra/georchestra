# From 23.x to 24.0.x

## Header

By default, geOrchestra uses the new header https://github.com/georchestra/header.

Some variables must be set in datadir's `default.properties` file.

```properties
# Variable use to keep the old iframe header.
# Set headerUrl accordingly
# Default false
useLegacyHeader=false

# Header script for web component header
# https://github.com/georchestra/header
headerScript=https://cdn.jsdelivr.net/gh/georchestra/header@dist/header.js

# Logo URL
# Used to set header's logo.
logoUrl=https://www.georchestra.org/public/georchestra-logo.svg

# Stylesheet used to override default colors of header
# More design can be set by overriding default classes & styles
# Default empty string
# georchestraStylesheet=http://my-domain-name/stylesheet.css
```

To edit colors and some other CSS properties, you can override the default stylesheet by setting the `georchestraStylesheet` variable.

```css
/* Example of custom stylesheet */
header {
    --georchestra-header-primary: #e20714;
    --georchestra-header-secondary: white;
    --georchestra-header-primary-light: white;
    --georchestra-header-secondary-light: #eee;
}
.admin-dropdown>li.active {
    background-color: red;
    color: white;
}
```
This header can be totally customized by creating a fork of the header repository and setting the `headerScript` variable accordingly.


## GeoNetwork 4.2.4 to 4.2.8 migration notes

After the upgrade :
- Delete index and reindex .
- JS and CSS cache must be cleared.

using the url : `/geonetwork/srv/eng/admin.console?debug#/tools`

## Elasticsearch

Elasticsearch can be upgraded to 7.17.15.

## LDAP

The `IMPORT` user was added to the ldap schema.

This role allows user to have access to the import tool (datafeeder).

By default, users couldn't use datafeeder application.

