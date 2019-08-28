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
        AtlasJob j = new AtlasJob();
        j.setId(3L);
        j.setToken("594aa148-a4fe-45c7-a5a8-14baeac9661e");
        j.setQuery("{ outputFormat: \"zip\" }");
        String ret = amc.formatMail(j);

        assertTrue("Wrong patterns in the mail (bad generated document URL)",
                ret.contains("http://localhost:8080/jobs/3/594aa148-a4fe-45c7-a5a8-14baeac9661e/zip"));
    }
}
