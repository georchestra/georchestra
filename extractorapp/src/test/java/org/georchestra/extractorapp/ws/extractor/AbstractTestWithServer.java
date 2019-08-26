package org.georchestra.extractorapp.ws.extractor;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A test that starts a web server on startup and allows the tests to use the
 * test.
 *
 * @author Jesse on 5/6/2014.
 */
public abstract class AbstractTestWithServer {
	private HttpServer server;

	private static AtomicInteger portInc = new AtomicInteger(13878);

	@Before
	public final void startServer() throws IOException {
		this.server = HttpServer.create(new InetSocketAddress(portInc.incrementAndGet()), 0);
		configureContext(this.server);
		this.server.start();
	}

	/**
	 * Add any contexts to the server that are needed for all tests.
	 * 
	 * @param server the server.
	 */
	protected abstract void configureContext(HttpServer server);

	@After
	public final void stopServer() {
		this.server.stop(0);
	}

	public final int getServerPort() {
		return this.server.getAddress().getPort();
	}

	protected final HttpContext setServerContext(String context, HttpHandler handler) {
		this.server.removeContext(context);
		return this.server.createContext(context, handler);
	}

	protected void writeResponse(HttpExchange httpExchange, byte[] response) throws IOException {
		httpExchange.getResponseHeaders().set("Content-Type", "text/xml");
		httpExchange.sendResponseHeaders(200, response.length);

		httpExchange.getResponseBody().write(response);
		httpExchange.getResponseBody().close();
	}

	public void sendError(HttpExchange httpExchange, int status, String errorMessage) throws IOException {
		final byte[] response = errorMessage.getBytes("UTF-8");
		httpExchange.sendResponseHeaders(status, response.length);
		httpExchange.getResponseBody().write(response);
	}
}
