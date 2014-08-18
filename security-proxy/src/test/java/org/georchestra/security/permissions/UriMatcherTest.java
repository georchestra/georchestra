package org.georchestra.security.permissions;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UriMatcherTest {

    @Test
    public void testMatchesHost() throws Exception {
        final UriMatcher matcher = new UriMatcher().setHost("localhost");
        matcher.init();
        assertTrue(matcher.matches(new URL("http://localhost:8080/geonetwork")));
        assertTrue(matcher.matches(new URL("http://localhost")));
        assertTrue(matcher.matches(new URL("https://localhost/geonetwork")));
        assertTrue(matcher.matches(new URL("https://127.0.0.1/geonetwork")));
        assertFalse(matcher.matches(new URL("https://www.georchestra.net/geonetwork")));
        assertFalse(matcher.matches(new URL("https://nonsense.kiko.muk/geonetwork")));
    }
    @Test
    public void testMatchesPort() throws Exception {
        final UriMatcher matcher = new UriMatcher().setPort(8080);
        matcher.init();
        assertTrue(matcher.matches(new URL("http://localhost:8080/geonetwork")));
        assertTrue(matcher.matches(new URL("http://www.google.com:8080")));
        assertFalse(matcher.matches(new URL("http://localhost:80/geonetwork")));
        assertFalse(matcher.matches(new URL("http://localhost/geonetwork")));

        matcher.setPort(80);
        matcher.init();
        assertTrue(matcher.matches(new URL("http://localhost:80/geonetwork")));
        assertTrue(matcher.matches(new URL("http://localhost/geonetwork")));
        assertFalse(matcher.matches(new URL("https://localhost/geonetwork")));

        matcher.setPort(443);
        matcher.init();
        assertTrue(matcher.matches(new URL("http://localhost:443/geonetwork")));
        assertTrue(matcher.matches(new URL("https://localhost/geonetwork")));
        assertFalse(matcher.matches(new URL("http://localhost/geonetwork")));
    }
    @Test
    public void testMatchesPath() throws Exception {
        final UriMatcher matcher = new UriMatcher().setPath("/geonetwork");
        matcher.init();
        assertTrue(matcher.matches(new URL("http://localhost:8080/geonetwork")));
        assertFalse(matcher.matches(new URL("http://localhost")));
        assertTrue(matcher.matches(new URL("https://localhost/geonetwork")));
        assertFalse(matcher.matches(new URL("https://localhost/geonetwork/")));

        matcher.setPath("geonetwork");
        matcher.init();
        assertTrue(matcher.matches(new URL("http://localhost:8080/geonetwork")));
        assertFalse(matcher.matches(new URL("http://localhost")));
        assertTrue(matcher.matches(new URL("https://localhost/geonetwork")));
        assertFalse(matcher.matches(new URL("https://localhost/geonetwork/")));

        matcher.setPath("/.*geonetwork.*");
        matcher.init();
        assertTrue(matcher.matches(new URL("https://localhost/srv/geonetwork/yzx")));
        assertTrue(matcher.matches(new URL("https://localhost/geonetwork")));
        assertTrue(matcher.matches(new URL("https://localhost/xyzgeonetworkmmm")));
    }
    @Test
    public void testMatchesAll() throws Exception {
        final UriMatcher matcher = new UriMatcher().setHost("localhost").setPort(80).setPath("/geonetwork/.*");
        matcher.init();

        assertTrue(matcher.matches(new URL("http://localhost:80/geonetwork/srv/eng")));
        assertTrue(matcher.matches(new URL("http://localhost/geonetwork/srv/eng")));
        assertTrue(matcher.matches(new URL("http://localhost/geonetwork/")));
        assertFalse(matcher.matches(new URL("http://localhost/geonetwork")));
        assertFalse(matcher.matches(new URL("http://localhost/")));
        assertFalse(matcher.matches(new URL("http://localhost/other")));
        assertFalse(matcher.matches(new URL("https://localhost/geonetwork")));
        assertFalse(matcher.matches(new URL("http://localhost:8080/geonetwork")));
        assertFalse(matcher.matches(new URL("http://www.georchestra.org/geonetwork")));
    }
}