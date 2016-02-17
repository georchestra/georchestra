package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.geotools.data.ows.SimpleHttpClient;
import org.geotools.data.wfs.impl.WFSContentDataStore;
import org.geotools.data.wfs.impl.WFSDataStoreFactory;
import org.geotools.data.wfs.internal.WFSClient;
import org.geotools.data.wfs.internal.WFSConfig;
import org.geotools.ows.ServiceException;

public class MockWFSDataStoreFactory extends WFSDataStoreFactory {

	@Override
	public WFSContentDataStore createDataStore(Map arg0) throws IOException {
		WFSConfig conf = WFSConfig.fromParams(arg0);
		WFSClient wfsclient = null;
		try {
			URL getCap = (URL) arg0.get(WFSDataStoreFactory.URL.key);
			wfsclient = new WFSClient(getCap,
					new SimpleHttpClient(), conf); // TODO use TestHTTPClient instead
		} catch (ServiceException e) {
			fail("Unable to instantiate a WFSClient: " + e.getMessage());
		}
		WFSContentDataStore mockDs = new WFSContentDataStore(wfsclient);	
		return mockDs; 
	}
}
