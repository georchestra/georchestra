package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONArray;
import org.junit.Test;
import org.mockito.Mockito;

public class AddonControllerTest {

    @Test
    public void testConstructAddonsSpec() throws Exception {
        AddonController ac = new AddonController();
        String resDir = new File(this.getClass().getResource(".").toURI()).getPath();
        GeorchestraConfiguration gc = Mockito.mock(GeorchestraConfiguration.class);
        Mockito.when(gc.activated()).thenReturn(true);
        Mockito.when(gc.getContextDataDir()).thenReturn(resDir);
        ac.setGeorchestraConfiguration(gc);

        JSONArray ret = ac.constructAddonsSpec();

        boolean id0 =  (ret.getJSONObject(0).getString("id").equals("magnifier_0") ||
                ret.getJSONObject(0).getString("id").equals("annotation_0"));
        boolean id1 =  (ret.getJSONObject(1).getString("id").equals("magnifier_0") ||
                ret.getJSONObject(1).getString("id").equals("annotation_0"));

        assertTrue("Expected 2 elements, found " + ret.length(), ret.length() == 2);
        assertTrue("Unexpected key: " + ret.getJSONObject(0).getString("id"), id0);
        assertTrue("Unexpected key: " + ret.getJSONObject(1).getString("id"), id1);
    }

}
