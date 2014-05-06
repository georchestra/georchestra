package org.georchestra.extractorapp.ws.extractor.wcs;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoundWcsRequestTest {
	private InputStream describeCoverageSample;
	private InputStream getCapabilitiesSample;

	@Before
	public void setUp() throws Exception {
		describeCoverageSample = BoundWcsRequestTest.class.getResourceAsStream("/wcs/describecoveragesample.xml");
		getCapabilitiesSample = BoundWcsRequestTest.class.getResourceAsStream("/wcs/getcapabilitiessample.xml");
	}

	@After
	public void tearDown() throws IOException {
		describeCoverageSample.close();
		getCapabilitiesSample.close();
	}


	private HttpEntity heMocked = Mockito.mock(HttpEntity.class);

	private void setOutputDocument(InputStream f) throws Exception {
			Mockito.when(heMocked.getContent()).thenReturn(f);
	}

	public HttpClient getMockHttpClient() {
		HttpClient mock = Mockito.mock(HttpClient.class);
		HttpParams paramsMock = Mockito.mock(HttpParams.class);
		ClientConnectionManager connectionMock = Mockito.mock(ClientConnectionManager.class);
		HttpResponse hrMocked = Mockito.mock(HttpResponse.class);
		StatusLine slMocked = Mockito.mock(StatusLine.class);

		Header headerMocked = Mockito.mock(Header.class);


		Mockito.when(connectionMock.getSchemeRegistry()).thenReturn(SchemeRegistryFactory.createDefault());
		Mockito.when(hrMocked.getEntity()).thenReturn(heMocked);
		Mockito.when(mock.getParams()).thenReturn(paramsMock);
		Mockito.when(mock.getConnectionManager()).thenReturn(connectionMock);
		try {
			Mockito.when(mock.execute(Mockito.any(HttpUriRequest.class), Mockito.any(HttpContext.class))).thenReturn(hrMocked);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mockito.when(hrMocked.getStatusLine()).thenReturn(slMocked);
		Mockito.when(slMocked.getStatusCode()).thenReturn(200);
		Mockito.when(heMocked.getContentType()).thenReturn(headerMocked);
		Mockito.when(headerMocked.getElements()).thenReturn(new HeaderElement[0]);
		return mock;
	}


	@Test
	public void BoundWcsRequestTest() throws Throwable {
		WcsReaderRequest rq = new WcsReaderRequestFactory().create("1.0", "myCov", 0, 0, 1, 1, CRS.decode("EPSG:4326"),
				CRS.decode("EPSG:2154"), 1, "GeoTiff", true, true, true, "scott", "tiger");

		BoundWcsRequest bwr = new BoundWcsRequest(new URL("http://localhost/"), rq);


		HttpClient mockClient = getMockHttpClient();
		bwr.setHttpClient(mockClient);


		// DescribeCoverage related tests
		setOutputDocument(describeCoverageSample);
		bwr.getCoverage();

		// supported formats
		Set<String> fmts = bwr.getSupportedFormats();
		String[] expectedFmts =  {"jpg", "geotiff", "tif", "jpeg", "png", "gif", "tiff"};
		assertArrayEquals(expectedFmts, fmts.toArray());

		// supported response CRS
		Set<String> supportedResponseCrs = bwr.getSupportedResponseCRSs();
		String[] expectedResponseCrs = { "EPSG:4326" };
		assertArrayEquals(expectedResponseCrs, supportedResponseCrs.toArray());

		// num bands
		int numBands  = bwr.numBands();
		assertEquals(numBands, 1);

		// supported request CRS
		Set<String> supportedRequestCrs = bwr.getSupportedRequestCRSs();
		String[] expectedRequestCrs = { "EPSG:4326" };
		assertArrayEquals(expectedRequestCrs, supportedRequestCrs.toArray());

		// Native CRS
		Set<String> nativeCRSs = bwr.getNativeCRSs();
		String[] expectedNativeCRSs = { "EPSG:4326" };
		assertArrayEquals(expectedNativeCRSs, nativeCRSs.toArray());

		// just to recall getDescribeCoverage(), should be defined as for now
		assertTrue(bwr.getDescribeCoverage() instanceof String);

		// Same
		assertTrue(bwr.getSupportedFormats() instanceof Set<?>);
		assertTrue(bwr.getSupportedResponseCRSs() instanceof Set<?>);
		assertTrue(bwr.numBands() == 1);
		assertTrue(bwr.getSupportedRequestCRSs() instanceof Set<?>);
		assertTrue(bwr.getNativeCRSs() instanceof Set<?>);



		// GetCapabilities related tests
		setOutputDocument(getCapabilitiesSample);

		String getCap = bwr.getCapabilities();
		assertTrue(getCap instanceof String);
		// another time
		assertTrue(bwr.getCapabilities() instanceof String);
	}

}
