package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class WMCDocServiceTest {


    @Test
    public void testXEEOnExtractRealFileName() {
        assumeTrue("file /etc/passwd does not exist, which is unlikely if you are running the testsuite under linux. Skipping test",
                new File("/etc/passwd").exists());

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
