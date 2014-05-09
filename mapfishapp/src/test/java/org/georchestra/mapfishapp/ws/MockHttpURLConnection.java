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

	private Map<String, List<String> > headerFields = new HashMap<String,List<String>>();

	private OutputStream fakedStream = new ByteArrayOutputStream();

	private InputStream fakedInputStream;
	private String contentType = null;
	private int fakedResponseCode = HttpURLConnection.HTTP_OK;
	
	public void setResponseCode(int c) { fakedResponseCode = c ; }

	@Override
	public int getResponseCode() {return fakedResponseCode ; }

	public void setContentType(String ct) { contentType = ct; }
	@Override
	public String getContentType() { return contentType; }

	protected MockHttpURLConnection() {
		super(null);
	}

	@Override
	public void disconnect() {
		return;
	}
	
	@Override
	public boolean usingProxy() {
		return false;
	}
	
	@Override
	public void connect() throws IOException {
		return;
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
	
	public void setHeaderFields(String k, String v) {
		List<String> l = headerFields.get(k);
		if (l == null) {
			List<String> val = new ArrayList<String>();
			val.add(v);
			headerFields.put(k, val);
			return;
		}
		l.add(v);
		headerFields.put(k, l);
	}

	@Override
	public Map<String, List<String>> getHeaderFields() {
		return headerFields;
	}
	
	public void setInputStream(InputStream stream) {
		fakedInputStream = stream;
	}
	@Override
	public InputStream getInputStream() {
		return fakedInputStream;
	}

	public void reset() {
		requestProperties = new HashMap<String,List<String>>();
		headerFields = new HashMap<String,List<String>>();
		try { fakedStream.close(); } catch (Throwable e) {}
		fakedStream = new ByteArrayOutputStream();
		try { fakedInputStream.close(); } catch (Throwable e) {};
		contentType = null;
		fakedResponseCode = HttpURLConnection.HTTP_OK;		
	}
}
