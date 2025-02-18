package org.georchestra.security;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.georchestra.security.permissions.Permissions;
import org.georchestra.security.permissions.ResolverDelegate;
import org.georchestra.security.permissions.UriMatcher;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

public class PermissionsTest {

    private Permissions load(String permissionsFile) throws IOException {
        InputStream inStream = this.getClass().getClassLoader().getResource(permissionsFile).openStream();
        return Permissions.parse(inStream);
    }

    @Test
    public void testLoadPermissions() throws Exception {

        Permissions perm = this.load("test-permissions-loading.xml");
        assertTrue(perm.isAllowByDefault());
        assertEquals(1, perm.getAllowed().size());
        assertEquals(1, perm.getDenied().size());

        assertEquals("localhost", perm.getAllowed().get(0).getHost());
        assertEquals(-1, perm.getAllowed().get(0).getPort());
        assertEquals(null, perm.getAllowed().get(0).getPath());

        assertEquals("localhost", perm.getDenied().get(0).getHost());
        assertEquals(433, perm.getDenied().get(0).getPort());
        assertEquals("/geonetwork/", perm.getDenied().get(0).getPath());

        assertTrue(perm.isInitialized());
    }

    @Test
    public void testLoadEmptyPermissions() throws Exception {
        this.load("empty-permissions.xml");

        // no exception? good
    }

    @Test
    public void testAllowBydefault() throws IOException {
        Permissions perm = this.load("test-permissions-allowByDefault.xml");

        // Test URL not defined in xml: example.org
        assertFalse(perm.isDenied(new URL("http://example.org/test.html")));

        // Test URL in allowed part: 127.0.0.1
        assertFalse(perm.isDenied(new URL("http://127.0.0.1/test.html")));
        assertFalse(perm.isDenied(new URL("http://sdi.georchestra.org/test.html")));
        assertFalse(perm.isDenied(new URL("http://www.georchestra.org/test.html")));

        // Test URL in denied part but shadowed by ".*\.georchestra\.org" in allowed
        // part:
        assertFalse(perm.isDenied(new URL("https://sdi.georchestra.org:433/geonetwork/")));

        // Test URL in denied part: www.google.com
        assertTrue(perm.isDenied(new URL("https://www.google.com/search.php")));

    }

    @Test
    public void testDenyBydefault() throws IOException {
        Permissions perm = this.load("test-permissions-denyByDefault.xml");

        // Test URL not defined in xml: example.org
        assertTrue(perm.isDenied(new URL("http://example.org/test.html")));

        // Test URL in allowed part: 127.0.0.1
        assertFalse(perm.isDenied(new URL("http://127.0.0.1/test.html")));
        assertFalse(perm.isDenied(new URL("http://sdi.georchestra.org/test.html")));
        assertFalse(perm.isDenied(new URL("http://www.georchestra.org/test.html")));

        // Test URL in denied part:
        assertTrue(perm.isDenied(new URL("https://sdi.georchestra.org:433/geonetwork/")));

        // Test URL in denied part: www.google.com
        assertTrue(perm.isDenied(new URL("https://www.google.com/search.php")));

    }

    /**
     * The purpose of this test is to check that a URIMatcher of type host matches
     * several domain names behind the same IP address.
     *
     * @throws IOException
     */

    @Test
    public void testHost() throws IOException {
        Permissions perm = new Permissions();
        UriMatcher deniedUm = new UriMatcher();
        deniedUm.setResolverDelegate(new ResolverDelegate() {
            @Override
            public InetAddress[] resolve(String host) throws UnknownHostException {
                if (host.equals("sdi-stable.georchestra.org") || host.equals("georchestra.org")) {
                    return new InetAddress[] { InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }) };
                } else {
                    throw new UnknownHostException();
                }
            }
        });
        deniedUm.setHostNames(
                Stream.of(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 })).collect(Collectors.toSet()));
        perm.setDenied(Arrays.asList(deniedUm));

        // sdi-stable.georchestra.org has same IP address as sdi.georchestra.org
        assertTrue(perm.isDenied(new URL("http://sdi-stable.georchestra.org/test.html")));
        assertTrue(perm.isDenied(new URL("http://georchestra.org/test.html")));
    }

    @Test
    public void testDomain() throws IOException {
        Permissions perm = this.load("test-permissions-uriMatcher.xml");

        assertTrue(perm.isDenied(new URL("http://www.google.fr/test.html")));
        assertTrue(perm.isDenied(new URL("http://google.com/test.html")));
        assertTrue(perm.isDenied(new URL("https://google.fzefezf:3453/test.html")));
        assertFalse(perm.isDenied(new URL("http://www.example.org/google.html")));
        assertTrue(perm.isDenied(new URL("https://Google.fzefezf:3453/test.html")));
    }

    @Test
    public void testNetwork() throws IOException {
        Permissions perm = this.load("test-permissions-uriMatcher.xml");

        assertTrue(perm.isDenied(new URL("http://192.168.11.12/geoserver")));
        assertTrue(perm.isDenied(new URL("http://192.168.0.0/geoserver")));
        assertTrue(perm.isDenied(new URL("http://10.28.12.145/geoserver")));
        assertFalse(perm.isDenied(new URL("http://yahoo.fr/geoserver")));
    }

    @Test
    public void testNetworkIPv6() throws IOException {
        Permissions perm = this.load("test-permissions-uriMatcher.xml");
        assertTrue(perm.isDenied(new URL("http://www.google.com/geoserver")));

    }

    @Test
    public void testIPv6() {
        IpAddressMatcher range = new IpAddressMatcher("192.168.0.0/16");
        assertFalse(range.matches("2a04:4e42:400:0:0:0:0:403"));
        assertTrue(range.matches("192.168.1.12"));
        assertTrue(range.matches("192.168.0.0"));

        range = new IpAddressMatcher("2a04:4e42::/32");
        assertTrue(range.matches("2a04:4e42:400:0:0:0:0:403"));
        assertTrue(range.matches("2a04:4e42:600:0:0:0:0:403"));
        assertTrue(range.matches("2a04:4e42:600::"));
        assertFalse(range.matches("2a04:4e43:600::"));
        assertFalse(range.matches("2a04:4e43:400:0:0:0:0:403"));
        assertFalse(range.matches("192.168.1.12"));
        assertFalse(range.matches("192.168.0.0"));
    }

    @Test
    public void testNetworkBadFormat() {
        assertThrows(IllegalArgumentException.class, () -> this.load("test-permissions-network-bad-format.xml"));
    }

    @Test
    public void testPort() throws IOException {
        Permissions perm = this.load("test-permissions-uriMatcher.xml");

        assertFalse(perm.isDenied(new URL("http://www.example.org/google.html")));
        assertFalse(perm.isDenied(new URL("http://www.example.org:8081/google.html")));
        assertTrue(perm.isDenied(new URL("http://www.example.org:8080/google.html")));
        assertTrue(perm.isDenied(new URL("http://www.google.org:8080/google.html")));
    }

    @Test
    public void testDefaultPort() throws IOException {
        Permissions perm = this.load("test-permissions-defaultPort.xml");

        assertTrue(perm.isDenied(new URL("http://www.example.org/google.html")));
        assertTrue(perm.isDenied(new URL("https://www.example.org/google.html")));
        assertTrue(perm.isDenied(new URL("http://www.example.org:80/google.html")));
        assertTrue(perm.isDenied(new URL("https://www.google.org:443/google.html")));
        assertFalse(perm.isDenied(new URL("http://www.example.org:8080/google.html")));
        assertFalse(perm.isDenied(new URL("https://www.google.org:8443/google.html")));
    }

    @Test
    public void testPath() throws IOException {
        Permissions perm = this.load("test-permissions-uriMatcher.xml");

        assertTrue(perm.isDenied(new URL("http://www.example.org/search.html")));
        assertTrue(perm.isDenied(new URL("http://www.example.org/api/v2/search.html?s=test")));
        assertTrue(perm.isDenied(new URL("http://www.example.org/api/v2/Search.html?s=test")));
        assertTrue(perm.isDenied(new URL("http://www.example.org/index.php?s=test")));
        assertTrue(perm.isDenied(new URL("http://www.example.org/index.php")));
        assertTrue(perm.isDenied(new URL("http://www.example.org/index.asP")));
        assertFalse(perm.isDenied(new URL("http://www.example.org/search.py")));

    }
}
