/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

/**
 * @author Jesse on 8/15/2014.
 */
@XmlRootElement(name = "permissions")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Permissions {
    private List<UriMatcher> allowed = Lists.newArrayList();
    private List<UriMatcher> denied = Lists.newArrayList();
    private boolean allowByDefault;
    private boolean initialized;

    public void setAllowed(List<UriMatcher> allowed) {
        this.allowed = allowed;
    }

    public void setDenied(List<UriMatcher> denied) {
        this.denied = denied;
    }

    public boolean isDenied(URL url) {
        if (allowByDefault) {
            if (checkIfAllowed(url))
                return false;
            if (checkIfDenied(url))
                return true;
        } else {
            if (checkIfDenied(url))
                return true;
            if (checkIfAllowed(url))
                return false;
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

    @XmlElementWrapper(name = "allowed")
    @XmlElementRef(type = UriMatcher.class)
    public List<UriMatcher> getAllowed() {
        return allowed;
    }

    @XmlElementWrapper(name = "denied")
    @XmlElementRef(type = UriMatcher.class)
    public List<UriMatcher> getDenied() {
        return denied;
    }

    @XmlElement
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

    public static Permissions parse(InputStream source) throws IOException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Permissions.class, UriMatcher.class);
            Unmarshaller unamrshaller = jaxbContext.createUnmarshaller();
            Permissions permissions = (Permissions) unamrshaller.unmarshal(source);
            permissions.init();
            return permissions;
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
