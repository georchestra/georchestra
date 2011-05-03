package com.camptocamp.security;

import java.net.URL;

public interface FilterRequestsStrategy {
    public boolean allowRequest(URL request);
}
