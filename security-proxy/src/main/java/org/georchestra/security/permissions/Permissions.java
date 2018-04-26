/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.security.permissions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

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

    public static Permissions Create(InputStream source) throws ClassNotFoundException, IOException {
        Map<String, Class<?>> aliases = Maps.newHashMap();
        aliases.put(Permissions.class.getSimpleName().toLowerCase(), Permissions.class);
        aliases.put(UriMatcher.class.getSimpleName().toLowerCase(), UriMatcher.class);
        XStreamMarshaller unmarshaller = new XStreamMarshaller();
        unmarshaller.setAliasesByType(aliases);
        Permissions res = (Permissions) unmarshaller.unmarshal(new StreamSource(source));
        res.init();
        return res;
    }
}
