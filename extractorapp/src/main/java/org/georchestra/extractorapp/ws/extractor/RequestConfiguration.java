package org.georchestra.extractorapp.ws.extractor;

import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.georchestra.extractorapp.ws.Email;

public class RequestConfiguration {

	private static final InheritableThreadLocal<RequestConfiguration> instance = new InheritableThreadLocal<RequestConfiguration>();

	public static RequestConfiguration get() {
		return instance.get();
	}

	public final List<ExtractorLayerRequest> requests;
	public final UUID requestUuid;
	public final Email email;
	public final ServletContext servletContext;
	public final boolean testing;
	public final String username;
	public final String roles;
	public final String org;
	public final UsernamePasswordCredentials adminCredentials;
	public final String secureHost;
	public final String extractionFolderPrefix;
	public final long maxCoverageExtractionSize;
	public final boolean remoteReproject;
	public final boolean useCommandLineGDAL;
	/** the original request in json syntax */
	public final String strRequest;
	public final String userAgent;

	public RequestConfiguration(List<ExtractorLayerRequest> requests, UUID requestUuid, Email email,
			ServletContext servletContext, boolean testing, String username, String roles, String org,
			UsernamePasswordCredentials adminCredentials, String secureHost, String extractionFolderPrefix,
			long maxCoverageExtractionSize, boolean remoteReproject, boolean useCommandLineGDAL, String strRequest,
			String userAgent) {
		super();

		this.strRequest = strRequest;
		this.requests = requests;
		this.requestUuid = requestUuid;
		this.email = email;
		this.servletContext = servletContext;
		this.testing = testing;
		this.username = username;
		this.roles = roles;
		this.org = org;
		this.adminCredentials = adminCredentials;
		this.secureHost = secureHost;
		this.maxCoverageExtractionSize = maxCoverageExtractionSize;
		this.remoteReproject = remoteReproject;
		this.useCommandLineGDAL = useCommandLineGDAL;
		this.extractionFolderPrefix = extractionFolderPrefix;
		this.userAgent = userAgent;
	}

	public void setThreadLocal() {
		instance.set(this);
	}
}
