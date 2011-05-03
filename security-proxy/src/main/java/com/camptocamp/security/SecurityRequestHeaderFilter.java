/**
 * 
 */
package com.camptocamp.security;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Filters out sec-username when not from trusted hosts
 * 
 * @author jeichar
 */
public class SecurityRequestHeaderFilter implements HeaderFilter {

    List<String> trustedHosts = Collections.emptyList();

    @Override
    public boolean filter(String headerName, HttpServletRequest originalRequest, HttpRequestBase proxyRequest) {
        String remoteHost = originalRequest.getRemoteHost();
        for (String host : trustedHosts) {
            if (remoteHost.equalsIgnoreCase(host)) {
                return false;
            }
        }
        return headerName.equalsIgnoreCase("sec-username") ||
                headerName.equalsIgnoreCase("sec-roles");
    }

    public void setTrustedHosts(List<String> trustedHosts) {
        this.trustedHosts = trustedHosts;
    }
}
