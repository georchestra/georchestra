/**
 * 
 */
package org.georchestra.ogcservstatistics.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.io.FileUtils;

/**
 * utility methods for test cases
 * 
 * @author Mauricio Pazos
 *
 */
public class Utility {

	private Utility() {

	}

	public static String loadRequest(String data) {

		try {
			return FileUtils.readFileToString(urlToFile(Utility.class.getResource(data)), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static File urlToFile(URL url) {
		if (!"file".equals(url.getProtocol())) {
			return null; // not a File URL
		}
		String string = url.toExternalForm();
		if (string.contains("+")) {
			// this represents an invalid URL created using either
			// file.toURL(); or
			// file.toURI().toURL() on a specific version of Java 5 on Mac
			string = string.replace("+", "%2B");
		}
		try {
			string = URLDecoder.decode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not decode the URL to UTF-8 format", e);
		}

		String path3;

		String simplePrefix = "file:/";
		String standardPrefix = "file://";
		String os = System.getProperty("os.name");

		if (os.toUpperCase().contains("WINDOWS") && string.startsWith(standardPrefix)) {
			// win32: host/share reference
			path3 = string.substring(standardPrefix.length() - 2);
		} else if (string.startsWith(standardPrefix)) {
			path3 = string.substring(standardPrefix.length());
		} else if (string.startsWith(simplePrefix)) {
			path3 = string.substring(simplePrefix.length() - 1);
		} else {
			String auth = url.getAuthority();
			String path2 = url.getPath().replace("%20", " ");
			if (auth != null && !auth.equals("")) {
				path3 = "//" + auth + path2;
			} else {
				path3 = path2;
			}
		}

		return new File(path3);
	}

}
