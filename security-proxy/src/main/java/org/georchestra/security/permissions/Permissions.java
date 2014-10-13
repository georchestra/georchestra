package org.georchestra.security.permissions;

import com.google.common.collect.Lists;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Jesse on 8/15/2014.
 */
public class Permissions {
    private List<UriMatcher> allowed = Lists.newArrayList();
    private List<UriMatcher> denied = Lists.newArrayList();
    private boolean allowByDefault = false;
    private boolean initialized = false;

    public Permissions setAllowed(List<UriMatcher> allowed) {
        this.allowed = allowed;
        return this;
    }

    public Permissions setDenied(List<UriMatcher> denied) {
        this.denied = denied;
        return this;
    }

    public boolean isDenied(URL url) {
        if (allowByDefault) {
            if (checkIfAllowed(url)) return false;
            if (checkIfDenied(url)) return true;
        } else {
            if (checkIfDenied(url)) return true;
            if (checkIfAllowed(url)) return false;
        }
        return !allowByDefault;
    }

    private boolean checkIfDenied(URL url) {
        for (UriMatcher uriMatcher : denied) {
            if (uriMatcher.matches(url)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfAllowed(URL url) {
        for (UriMatcher uriMatcher : allowed) {
            if (uriMatcher.matches(url)) {
                return true;
            }
        }
        return false;
    }

    public List<UriMatcher> getAllowed() {
        return allowed;
    }

    public List<UriMatcher> getDenied() {
        return denied;
    }

    public boolean isAllowByDefault() {
        return allowByDefault;
    }

    public void setAllowByDefault(boolean allowByDefault) {
        this.allowByDefault = allowByDefault;
    }

    public synchronized void init() throws UnknownHostException {
        for (UriMatcher uriMatcher : allowed) {
            uriMatcher.init();
        }

        for (UriMatcher uriMatcher : denied) {
            uriMatcher.init();
        }
        initialized = true;
    }

    public synchronized boolean isInitialized() {
        return this.initialized;
    }


    private Object readResolve() {
        if (this.denied == null) {
            this.denied = Lists.newArrayList();
        }
        if (this.allowed == null) {
            this.allowed = Lists.newArrayList();
        }
        return this;
    }
}
