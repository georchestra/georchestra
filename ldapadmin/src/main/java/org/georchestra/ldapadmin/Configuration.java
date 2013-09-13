/**
 * 
 */
package org.georchestra.ldapadmin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configurations
 *  
 * <p>
 * This class maintains the shared values in this application.
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
public final class Configuration {

	protected static final Log LOG = LogFactory.getLog(Configuration.class.getName());

	private String passwordRecoveryContext;

	public String getPasswordRecoveryContext() {
		
		checkConfiguration();
		
		return this.passwordRecoveryContext;
	}

	private void checkConfiguration() {

		String message = "password recovery context was not configured.";
		
		if(this.passwordRecoveryContext == null){

			LOG.warn(message);
		
			return;
		}
		if(this.passwordRecoveryContext.length() == 0){
			
			LOG.warn(message);
			return;
		}
		
		LOG.info("password recovery context was configured: "+ this.passwordRecoveryContext);
	}

	public void setPasswordRecoveryContext(String passwordRecoveryContext) {
		this.passwordRecoveryContext = passwordRecoveryContext;
	}
	
	

}
