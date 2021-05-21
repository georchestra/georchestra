package org.georchestra.security.webdav;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class SearchMethod extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "SEARCH";

    public SearchMethod() {
        super();
    }

    public SearchMethod(final URI uri) {
        super();
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
