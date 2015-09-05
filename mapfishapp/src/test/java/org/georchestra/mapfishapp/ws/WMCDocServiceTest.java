package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class WMCDocServiceTest {


    @Test
    public void testXEEOnExtractRealFileName() {
        final String xeeVuln = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
                + "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>\n"
                +"<wmc><Title>&xxe;</Title></wmc>";


        WMCDocService wmcds = new WMCDocService("xml", null) {};

        Method erfn = ReflectionUtils.findMethod(wmcds.getClass(), "extractRealFileName", InputStream.class);
        erfn.setAccessible(true);

        String ret = (String) ReflectionUtils.invokeMethod(erfn, wmcds,
                new ByteArrayInputStream(xeeVuln.getBytes()));

        // If no resolution, filename should be null
        assertTrue("title is not null, XEE attack vulnerable ?",
               StringUtils.isEmpty(ret));
    }
}
