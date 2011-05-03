package com.camptocamp.security;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

public abstract class HeaderProvider {
    /**
     * Called by {@link HeadersManagementStrategy#configureRequestHeaders(HttpServletRequest, HttpRequestBase)} to allow
     * extra headers to be added to the copied headers.
     */
    protected Collection<Header> getCustomRequestHeaders() {
        return Collections.emptyList();
    }
    /**
     * Called by {@link HeadersManagementStrategy#configureRequestHeaders(HttpServletRequest, HttpRequestBase)} to allow
     * extra headers to be added to the copied headers.
     */
    protected Collection<Header> getCustomResponseHeaders() {
        return Collections.emptyList();
    }

}
