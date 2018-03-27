package org.georchestra.commons;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DatadirDefaultTest {

    @Test
    public void testDefaultProperty(){
        this.setDatadir("datadir");
        GeorchestraConfiguration datadir = new GeorchestraConfiguration("webapp");
        assertEquals(datadir.getProperty("testWebapp"), "webapp");
        assertEquals(datadir.getProperty("testCommon"), "common");
        assertEquals(datadir.getProperty("testOverride"), "webapp");
    }

    @Test
    public void testWithoutDefaultProperty(){
        this.setDatadir("datadir-without-default");
        GeorchestraConfiguration datadir = new GeorchestraConfiguration("webapp");
        assertEquals(datadir.getProperty("testWebapp"), "webapp");
        assertNull(datadir.getProperty("testCommon"));
        assertEquals(datadir.getProperty("testOverride"), "webapp");
    }

    @Test
    public void testWithoutWebappProperty(){
        this.setDatadir("datadir-without-webapp");
        GeorchestraConfiguration datadir = new GeorchestraConfiguration("webapp");
        assertNull(datadir.getProperty("testWebapp"));
        assertEquals(datadir.getProperty("testCommon"), "common");
        assertEquals(datadir.getProperty("testOverride"), "common");
    }

    private void setDatadir(String path){
        System.setProperty("georchestra.datadir",
                this.getClass().getClassLoader().getResource(path).getFile());
    }

}
