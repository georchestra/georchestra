package org.georchestra.security;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

public class SecurityProxyMetadataSourceTest {

    @Test
    public void testParseXml() throws Exception {
        URL res = this.getClass().getResource("security-mappings-test.xml");
        assumeTrue("Unable to find resource file security-mappings-test.xml, skipping test",
                 res != null);

        SecurityProxyMetadataSource sp = new SecurityProxyMetadataSource();
        Method m = ReflectionUtils.findMethod(SecurityProxyMetadataSource.class, "loadSecurityRules", File.class);
        m.setAccessible(true);

        File f = new File(res.toURI());

        Field rm = ReflectionUtils.findField(sp.getClass(), "requestMap");
        rm.setAccessible(true);
        Map<RequestMatcher, Collection<ConfigAttribute>> map = (Map<RequestMatcher, Collection<ConfigAttribute>>) ReflectionUtils.getField(rm, sp);

        assertTrue("Expected 0 item, found " + map.keySet().size(), map.keySet().size() == 0);

        ReflectionUtils.invokeMethod(m, sp, f);

        // Checks that the requestMap has correctly been populated
        assertTrue("Expected 14 items, found " + map.keySet().size(), map.keySet().size() == 14);
    }

}
