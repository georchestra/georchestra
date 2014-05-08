package org.georchestra.mapfishapp.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MockHttpURLConnection extends HttpURLConnection {
	private Map<String, List<String> > requestProperties = new HashMap<String,List<String>>();

	private OutputStream fakedStream = new ByteArrayOutputStream();
	private InputStream fakedInputStream;
	private String contentType = null;
	
	public String getContentType() { return contentType; }
	public void setContentType(String ct) { contentType = ct; }
	
	protected MockHttpURLConnection() {
		super(null);
	}
	@Override
	public void disconnect() {
		
	}
	@Override
	public boolean usingProxy() {
		return false;
	}
	@Override
	public void connect() throws IOException {
		
	}
	@Override
	public OutputStream getOutputStream() {
		return fakedStream;
	}
	
	@Override
	public void setRequestProperty(String k, String v) {
		List<String> l = requestProperties.get(k);
		if (l == null) {
			List<String> val = new ArrayList<String>();
			val.add(v);
			requestProperties.put(k, val);
			return;
		}
		l.add(v);
		requestProperties.put(k, l);
	}
	@Override
	public Map<String, List<String>> getRequestProperties() {
		return requestProperties;
	}

	public void setInputStream(InputStream stream) {
		fakedInputStream = stream;
	}
	@Override
	public InputStream getInputStream() {
		return fakedInputStream;
	}
}
