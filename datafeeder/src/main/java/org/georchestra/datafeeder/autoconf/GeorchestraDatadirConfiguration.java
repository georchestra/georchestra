package org.georchestra.datafeeder.autoconf;

import org.springframework.context.annotation.PropertySource;

@PropertySource(value = { //
        "file:${georchestra.datadir}/default.properties", //
        "file:${georchestra.datadir}/datafeeder/datafeeder.properties" }, //
        ignoreResourceNotFound = false)
public class GeorchestraDatadirConfiguration {
}
