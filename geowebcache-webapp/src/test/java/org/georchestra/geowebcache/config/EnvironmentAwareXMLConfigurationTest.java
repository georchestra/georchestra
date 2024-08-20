package org.georchestra.geowebcache.config;

import org.geowebcache.GeoWebCacheEnvironment;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.ConfigurationException;
import org.geowebcache.config.DefaultGridsets;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.config.XMLFileResourceProvider;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.layer.wms.WMSLayer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class EnvironmentAwareXMLConfigurationTest {
    protected File cpDirectory;

    protected XMLFileResourceProvider xfrp;

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void setUp() throws URISyntaxException, ConfigurationException {
        environmentVariables.set("SUPERSECRET", "mysecretpassword&$");
        cpDirectory = new File(Objects.requireNonNull(this.getClass().getResource("./")).toURI());
        xfrp = new XMLFileResourceProvider("geowebcache-config-env.xml",
                (WebApplicationContext) null, cpDirectory.getAbsolutePath(), null);
    }

    @Test
    public void testLoadXMLConfigFileWithoutEnvironmentVariablesSubstitution() throws GeoWebCacheException, URISyntaxException {
        XMLConfiguration xmlConfig = new XMLConfiguration(null, xfrp);
        xmlConfig.setGridSetBroker(new GridSetBroker(List.of(new DefaultGridsets(true, true))));
        xmlConfig.afterPropertiesSet();
        WMSLayer layer = (WMSLayer) xmlConfig.getLayer("test").get();

        assertEquals("${SUPERSECRET}", layer.getHttpPassword());
    }

    @Test
    public void testLoadXMLConfigFileWithEnvironmentVariablesSubstitution() throws GeoWebCacheException, URISyntaxException {
        EnvironmentAwareXMLConfiguration xmlConfig = new EnvironmentAwareXMLConfiguration(null, xfrp);
        EnvironmentAwareXMLConfiguration.setGwcEnvironment(new GeoWebCacheEnvironment());
        xmlConfig.setGridSetBroker(new GridSetBroker(List.of(new DefaultGridsets(true, true))));
        xmlConfig.afterPropertiesSet();
        WMSLayer layer = (WMSLayer) xmlConfig.getLayer("test").get();

        assertEquals("mysecretpassword&$", layer.getHttpPassword());
    }

    @Test
    public void testSaveXMLConfigFileWithEnvironmentVariablesSubstitution() throws GeoWebCacheException, IOException {
        EnvironmentAwareXMLConfiguration xmlConfig = new EnvironmentAwareXMLConfiguration(null, xfrp);
        EnvironmentAwareXMLConfiguration.setGwcEnvironment(new GeoWebCacheEnvironment());
        xmlConfig.setGridSetBroker(new GridSetBroker(List.of(new DefaultGridsets(true, true))));
        xmlConfig.afterPropertiesSet();

        xmlConfig.addLayer(new WMSLayer("newWms",new String[] {"http://wms.example.org"},
                "","", new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new int[]{},
                "", false, null));

        // expect nothing thrown.
    }
}
