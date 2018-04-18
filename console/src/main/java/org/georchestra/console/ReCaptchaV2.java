/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class used to verify CaptchaV2 response
 *
 * @author Pierre Jego
 *
 */
public class ReCaptchaV2 {

	private static final Log LOG = LogFactory.getLog(ReCaptchaV2.class.getName());

	/**
	 *
	 * @param url
	 * @param privateKey
	 * @param gRecaptchaResponse
	 *
	 * @return true if validaded on server side by google, false in case of error or if an exception occurs
	 */
	public boolean isValid(String url, String privateKey, String gRecaptchaResponse) {
		boolean isValid = false;

		try {
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			String postParams = "secret=" + privateKey + "&response=" + gRecaptchaResponse;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();

			if (LOG.isDebugEnabled()) {
				int responseCode = con.getResponseCode();
				LOG.debug("\nSending 'POST' request to URL : " + url);
				LOG.debug("Post parameters : " + postParams);
				LOG.debug("Response Code : " + responseCode);
			}

			// getResponse
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			LOG.debug(response.toString());

			JSONObject captchaResponse;
			try {
				captchaResponse = new JSONObject(response.toString());

				if (captchaResponse.getBoolean("success")) {
					isValid = true;
				} else {
					// Error in response
					LOG.info("The user response to recaptcha is not valid. The error message is '"
							+ captchaResponse.getString("error-codes")
							+ "' - see Error Code Reference at https://developers.google.com/recaptcha/docs/verify.");
				}
			} catch (JSONException e) {
				// Error in response
				LOG.error("Error while parsing ReCaptcha JSON response", e);
			}
		} catch (IOException e) {
			LOG.error("An error occured when trying to contact google captchaV2", e);
		}

		return isValid;
	}

}
