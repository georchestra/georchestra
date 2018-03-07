/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.ldapadmin.ws.utils;

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
import org.springframework.validation.Errors;

/**
 * Utility class to manage recaptcha.
 * Updated to use recaptcha V2
 * 
 * @author Sylvain Lesage
 *
 */
public class RecaptchaUtils {
	
	private static final Log LOG = LogFactory.getLog(RecaptchaUtils.class.getName());
	private String remoteAddr;
	private String privateKey;

	public RecaptchaUtils(final String remoteAddr, final String privateKey) {
		this.remoteAddr = remoteAddr;
		this.privateKey = privateKey;
	}

	/**
	 * This validate from server side the captchaV2 response from client
	 * 
	 * @param gRecaptchaResponse g-recaptcha-reponse from client side
	 * @param errors Errors already existing, and used to display validation errors
	 * 
	 * @throws IOException
	 */
	public boolean validate(String gRecaptchaResponse, Errors errors) throws IOException {
	
		boolean isValidated = false;
		if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
			LOG.info("The user response to recaptcha is empty.");
			errors.rejectValue("recaptcha_response_field", "recaptcha_response_field.error.required", "required");
			isValidated = true;
		}else{
			URL obj = new URL(remoteAddr);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		
			// add reuqest header
			con.setRequestMethod("POST");	
			String postParams = "secret=" + privateKey + "&response="+ gRecaptchaResponse;
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();
	
			if(LOG.isDebugEnabled()){
				int responseCode = con.getResponseCode();
				LOG.debug("\nSending 'POST' request to URL : " + remoteAddr);
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
			
				if(!captchaResponse.getBoolean("success")){
					LOG.info("The user response to recaptcha is not valid. The error message is '" + captchaResponse.getString("error-codes") + "' - see Error Code Reference at https://developers.google.com/recaptcha/docs/verify.");
					errors.rejectValue("recaptcha_response_field", "recaptcha.incorrect", "Validation error");
				} else {
					LOG.debug("The user response to recaptcha is valid.");
					isValidated = true;
				}
			} catch (JSONException e) {
				LOG.error("An exception occured when trying to parse response", e);
				errors.rejectValue("recaptcha_response_field", "recaptcha.incorrect", "An exception occured when trying to validate captcha");
			}
		}
		return isValidated;
	}
}
