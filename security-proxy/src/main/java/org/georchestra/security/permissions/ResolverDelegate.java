package org.georchestra.security.permissions;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface ResolverDelegate {
    public InetAddress[] resolve(String host) throws UnknownHostException;
}
