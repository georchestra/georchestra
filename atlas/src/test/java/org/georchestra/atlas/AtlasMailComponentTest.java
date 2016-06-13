package org.georchestra.atlas;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class AtlasMailComponentTest {

    private AtlasMailComponent amc;
    
    @Before
    public void setUp() {
        amc = new AtlasMailComponent();
        amc.init();
        
    }
    @Test
    public void testPrepareMail() {
        String ret = amc.formatMail(3L, "pmt@camptocamp.org");
        
        assertTrue("Missing patterns in the mail", ret.contains("http://localhost:8080/jobs/3"));
    }
}
