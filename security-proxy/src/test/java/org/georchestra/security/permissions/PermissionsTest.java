package org.georchestra.security.permissions;

import junit.framework.TestCase;

import java.net.URL;
import java.util.Collections;

public class PermissionsTest extends TestCase {

    public void testIsDeniedDisallowByDefault() throws Exception {
        Permissions permissions = new Permissions();
        permissions.setAllowByDefault(false);
        permissions.setAllowed(Collections.singletonList(new UriMatcher().
                setHost("localhost").
                setPath(".*").
                setPort(80)));
        permissions.setDenied(Collections.singletonList(new UriMatcher().
                setHost("localhost").
                setPath("geonetwork/.*").
                setPort(80)));
        permissions.init();

        assertTrue(permissions.isDenied(new URL("http://localhost:8080/")));
        assertTrue(permissions.isDenied(new URL("http://localhost:80/geonetwork/srv/eng")));
        assertFalse(permissions.isDenied(new URL("http://localhost:80/mapfishapp")));
        assertFalse(permissions.isDenied(new URL("http://localhost/mapfishapp")));
        assertFalse(permissions.isDenied(new URL("http://localhost")));
    }
    public void testIsDeniedAllowByDefault() throws Exception {
        Permissions permissions = new Permissions();
        permissions.setAllowByDefault(true);
        permissions.setAllowed(Collections.singletonList(new UriMatcher().
                setHost("localhost").
                setPath("geonetwork/.*").
                setPort(80)));
        permissions.setDenied(Collections.singletonList(new UriMatcher().
                setHost("localhost").
                setPath(".*").
                setPort(80)));
        permissions.init();

        assertFalse(permissions.isDenied(new URL("http://localhost:8080/")));
        assertFalse(permissions.isDenied(new URL("http://localhost:80/geonetwork/srv/eng")));
        assertTrue(permissions.isDenied(new URL("http://localhost:80/mapfishapp")));
        assertTrue(permissions.isDenied(new URL("http://localhost/mapfishapp")));
        assertTrue(permissions.isDenied(new URL("http://localhost")));
    }
}