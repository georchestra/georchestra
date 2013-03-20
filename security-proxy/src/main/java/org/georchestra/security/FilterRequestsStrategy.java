package org.georchestra.security;

import java.net.URL;

public interface FilterRequestsStrategy {
    public boolean allowRequest(URL request);
}
