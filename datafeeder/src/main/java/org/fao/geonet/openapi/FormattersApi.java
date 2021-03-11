package org.fao.geonet.openapi;

import org.fao.geonet.ApiClient;

import feign.Headers;
import feign.RequestLine;

public interface FormattersApi extends ApiClient.Api {

    /**
     * Clear formatter cache Formatters are used to render records in various format
     * (HTML, PDF, ...). When a record is rendered a cache is populated for better
     * performance. By default the cache is an H2 database with files on the
     * filesystems (See &lt;dataDirectory&gt;/resources/htmlcache/formatter-cache
     * folder).
     */
    @RequestLine("DELETE /srv/api/0.1/formatters/cache")
    @Headers({ "Accept: */*", })
    void clearFormatterCache();
}
