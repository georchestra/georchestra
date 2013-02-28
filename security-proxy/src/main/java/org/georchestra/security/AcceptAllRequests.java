package org.georchestra.security;

import java.net.URL;

/**
 * Accepts all requests
 * @author jeichar
 */
public class AcceptAllRequests implements FilterRequestsStrategy {

    @Override
    public boolean allowRequest(URL request) {
        return true;
    }

}
