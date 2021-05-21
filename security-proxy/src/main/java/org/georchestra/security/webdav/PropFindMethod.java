package org.georchestra.security.webdav;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class PropFindMethod extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "PROPFIND";

    public PropFindMethod() {
        super();
    }

    public PropFindMethod(final URI uri) {
        super();
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
