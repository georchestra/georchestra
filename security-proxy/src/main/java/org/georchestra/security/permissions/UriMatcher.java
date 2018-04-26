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

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

/**
 * @author Jesse on 8/15/2014.
 */
public class UriMatcher {
    private int port = -1;
    private String path;
    private Pattern pathPattern;
    private HashSet<InetAddress> hostNames;
    private String host;
    private String network;
    private IpAddressMatcher ipMatcher;
    private String domain;
    private Pattern domainPattern;

    public synchronized void init() throws UnknownHostException {
        this.hostNames = null;
        if (this.host != null) {
            this.hostNames = Sets.newHashSet(InetAddress.getAllByName(this.host));
        }
        this.pathPattern = null;
        if (this.path != null) {
            if (!this.path.startsWith("/")) {
                this.path = "(/" + this.path + ")|(" + this.path + ")";
            }
            this.pathPattern = Pattern.compile(this.path, Pattern.CASE_INSENSITIVE);
        }
        this.domainPattern = null;
        if (this.domain != null) {
            this.domainPattern = Pattern.compile(this.domain, Pattern.CASE_INSENSITIVE);
        }
        if(this.network != null){
            this.ipMatcher = new IpAddressMatcher(network);
        }
    }

    public boolean matches(URL url) {
        if (hostNames != null && !matchesHost(url))
            return false;
        if (domain != null && !matchesDomain(url))
            return false;
        if (port != -1 && !matchesPort(url))
            return false;
        if (network != null && !matchesNetwork(url))
            return false;
        return !(pathPattern != null && !matchesPath(url));
    }

    private boolean matchesPath(URL url) {
        return this.pathPattern.matcher(url.getPath()).matches();
    }

    private boolean matchesPort(URL url) {
        if (url.getPort() == this.port) {
            return true;
        }
        return url.getPort() == -1 && url.getDefaultPort() == this.port;
    }

    private boolean matchesHost(URL url) {
        final InetAddress[] allByName;
        try {
            allByName = InetAddress.getAllByName(url.getHost());
        } catch (UnknownHostException e) {
            return false;
        }

        for (InetAddress inetAddress : allByName) {
            if (this.hostNames.contains(inetAddress)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesNetwork(URL url) {
        final InetAddress[] allByName;
        try {
            allByName = InetAddress.getAllByName(url.getHost());
        } catch (UnknownHostException e) {
            return false;
        }

        for (InetAddress inetAddress : allByName) {
            if (this.ipMatcher.matches(inetAddress.getHostAddress())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesDomain(URL url) {
        return this.domainPattern.matcher(url.getHost()).matches();
    }


    public UriMatcher setHost(String host) throws UnknownHostException {
        this.host = host;
        return this;
    }

    public UriMatcher setPort(int port) {
        this.port = port;
        return this;
    }

    public UriMatcher setPath(String path) {
        this.path = path;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDomain() {
        return domain;
    }

    public UriMatcher setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getPath() {
        return path;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    private Object readResolve() {
        // The Xml unmarshaller will set port to 0 if the port element is missing
        // so I will assume that this means the port is missing and set to -1 which
        // is the correct ignore port number
        if (this.port == 0) {
            this.port = -1;
        }
        return this;
    }
}
