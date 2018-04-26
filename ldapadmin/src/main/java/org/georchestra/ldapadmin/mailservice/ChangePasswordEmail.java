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

package org.georchestra.ldapadmin.mailservice;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;

/**
 * Manages the change password email. This mail is send when a user has lost his password.
 *  
 * @author Mauricio Pazos
 *
 */
class ChangePasswordEmail extends Email {

	private static final Log LOG = LogFactory.getLog(ChangePasswordEmail.class.getName());
	
	private ServletContext servletContext;
	
	public ChangePasswordEmail(
			String[] recipients, 
			String emailSubject,
			String smtpHost, 
			int smtpPort, 
			String emailHtml,
			String replyTo, 
			String from,
			String bodyEncoding, 
			String subjectEncoding, 
			String[] languages, 
			String fileTemplate, 
			ServletContext servletContext,
			GeorchestraConfiguration georConfig) {

		super(recipients, emailSubject, smtpHost, smtpPort, emailHtml, replyTo, from,
				bodyEncoding, subjectEncoding, languages, fileTemplate, georConfig);

		this.servletContext = servletContext;
	}

	public void sendMsg( final String userName, final String uid, final String url) throws AddressException, MessagingException {

		if(LOG.isDebugEnabled() ){
			
			LOG.debug("send change password email to user "+ userName+ " - uid: ." + uid  );
		}
		
		String body = writeNewPasswordMail(userName, url, uid);
		
		super.sendMsg(body);
	}
	
	@Override
    protected String toAbsolutePath(String fileTemplate) {

    	return this.servletContext.getRealPath(fileTemplate);
    }
	
	private String writeNewPasswordMail(final String userName, final String url, final String uid) {
		
		String body = getBodyTemplate();
		
		body = body.replace("{name}", userName);
		body = body.replace("{url}", url);
		body = body.replace("{uid}", uid);
		
		if(LOG.isDebugEnabled() ){
			
			LOG.debug("built email: "+ body);
		}
		
		return body;
	}

}
