/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

public class SecurityProxyMetadataSourceTest {

    @Test
    public void testParseXml() throws Exception {
        URL res = this.getClass().getResource("security-mappings-test.xml");
        assumeTrue(res != null, "Unable to find resource file security-mappings-test.xml, skipping test");

        SecurityProxyMetadataSource sp = new SecurityProxyMetadataSource();
        Method m = ReflectionUtils.findMethod(SecurityProxyMetadataSource.class, "loadSecurityRules", File.class);
        m.setAccessible(true);

        File f = new File(res.toURI());

        Field rm = ReflectionUtils.findField(sp.getClass(), "requestMap");
        rm.setAccessible(true);
        Map<RequestMatcher, Collection<ConfigAttribute>> map = (Map<RequestMatcher, Collection<ConfigAttribute>>) ReflectionUtils
                .getField(rm, sp);

        assertTrue(map.keySet().size() == 0, "Expected 0 item, found " + map.keySet().size());

        ReflectionUtils.invokeMethod(m, sp, f);

        // Checks that the requestMap has correctly been populated
        assertTrue(map.keySet().size() == 11, "Expected 11 items, found " + map.keySet().size());
    }

}
