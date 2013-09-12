/**
 * 
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

	private String publicKey;

	public String getPublicKey() {
		return publicKey;
	}
	@Required
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
}
