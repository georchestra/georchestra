package org.georchestra.ldapadmin.mailservice;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletContext;

import org.georchestra.commons.configuration.GeorchestraConfiguration;

public class AccountUidRenamedEmail extends Email {
	
	private ServletContext context;

	public AccountUidRenamedEmail(String[] recipients,
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
			ServletContext ctx,
			GeorchestraConfiguration georConfig) {
		super(recipients, emailSubject, smtpHost, smtpPort, emailHtml, replyTo,
				from, bodyEncoding, subjectEncoding, languages,
				fileTemplate, georConfig);
		
		context = ctx;
	}

	@Override
	protected String toAbsolutePath(String fileTemplate) {
    	return this.context.getRealPath(fileTemplate);
	}
	
	public void sendMsg(final String userName, final String uid)
			throws AddressException, MessagingException {
		String body = getBodyTemplate();
		body = body.replace("{uid}", uid);
		body = body.replace("{name}", userName);

		if(LOG.isDebugEnabled() ){
			LOG.debug("built email: "+ body);
		}

		super.sendMsg(body);
	}

}
