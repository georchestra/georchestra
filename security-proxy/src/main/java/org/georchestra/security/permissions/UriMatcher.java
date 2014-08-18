package org.georchestra.security.permissions;

import com.google.common.collect.Sets;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * @author Jesse on 8/15/2014.
 */
public class UriMatcher {
    private int port = -1;
    private String path;
    private Pattern pathPattern;
    private HashSet<InetAddress> hostNames;
    private String host;

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
            this.pathPattern = Pattern.compile(this.path);
        }
    }

    public boolean matches(URL url) {
        if (hostNames != null && !matchesHost(url)) {
            return false;
        }
        if (port != -1 && !matchesPort(url)) {
            return false;
        }
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

    public String getPath() {
        return path;
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
