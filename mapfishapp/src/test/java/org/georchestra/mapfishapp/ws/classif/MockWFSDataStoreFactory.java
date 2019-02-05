package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.geotools.data.ows.SimpleHttpClient;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.internal.WFSClient;
import org.geotools.data.wfs.internal.WFSConfig;
import org.geotools.ows.ServiceException;

public class MockWFSDataStoreFactory extends WFSDataStoreFactory {

	@Override
	public WFSDataStore createDataStore(Map arg0) throws IOException {
        // connect to remote WFS

		WFSConfig conf = WFSConfig.fromParams(arg0);
		WFSClient wfsclient = null;
		try {
			URL getCap = (URL) arg0.get(WFSDataStoreFactory.URL.key);
			SimpleHttpClient hc = new SimpleHttpClient(); // TODO use TestHTTPClient instead
			wfsclient = new WFSClient(getCap, hc, conf);
		} catch (ServiceException e) {
			fail("Unable to instantiate a WFSClient: " + e.getMessage());
		}
		WFSDataStore mockDs = new WFSDataStore(wfsclient);	
		return mockDs; 
	}
}
