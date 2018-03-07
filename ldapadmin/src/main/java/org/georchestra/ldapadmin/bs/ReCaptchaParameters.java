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

package org.georchestra.ldapadmin.bs;
import org.springframework.beans.factory.annotation.Required;

/**
 * ReCaptchaParameters attribute
 * 
 * 
 * @author Sylvain Lesage
 *
 */
public final class ReCaptchaParameters {

	private String privateKey;
	
	private String publicKey;
	
	private String verifyUrl;

	public String getPublicKey() {
		return publicKey;
	}
	@Required
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	public String getPrivateKey() {
		return privateKey;
	}
	@Required
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	public String getVerifyUrl() {
		return verifyUrl;
	}
	@Required
	public void setVerifyUrl(String verifyUrl) {
		this.verifyUrl = verifyUrl;
	}
}
