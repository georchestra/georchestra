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

package org.georchestra.extractorapp.ws;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EmailFactoryPigma extends AbstractEmailFactory {

	protected static final Log LOG = LogFactory.getLog(EmailFactoryPigma.class.getPackage().getName());

	@Override
	public Email createEmail(HttpServletRequest request, final String[] recipients, final String url)
			throws IOException {

		final long expiry = this.expireDeamon.getExpiry();
		final String msgAck = readFile(request, emailAckTemplateFile);
		final String msgDone = readFile(request, emailTemplateFile);
		final String conv = request.getHeader("sec-convention");

		return new Email(request, recipients, emailSubject, this.smtpHost, this.smtpPort, this.emailHtml, this.replyTo,
				this.from, this.bodyEncoding, this.subjectEncoding, this.languages, this.georConfig) {
			public void sendDone(List<String> successes, List<String> failures, List<String> oversized, long fileSize)
					throws MessagingException {

				LOG.debug("preparing to send extraction done email");
				LOG.debug("pigma convention : " + conv);
				String msg = new String(msgDone);
				if (msg != null) {
					msg = msg.replace("{link}", url);
					msg = msg.replace("{emails}", Arrays.toString(recipients));
					msg = msg.replace("{expiry}", String.valueOf(expiry));
					msg = msg.replace("{successes}", format(successes));
					msg = msg.replace("{failures}", format(failures));
					msg = msg.replace("{oversized}", format(oversized));
					msg = msg.replace("{convention}", conv == null ? "" : conv);
					msg = formatTimeEstimation(msg, fileSize);
				}
				sendMsg(msg);
			}

			public void sendAck() throws AddressException, MessagingException {
				LOG.debug("preparing to send extraction ack email");
				String msg = new String(msgAck);
				if (msg != null) {
					msg = msg.replaceAll("\\{publicUrl\\}", this.georConfig.getProperty("publicUrl"));
				}
				// Normalize newlines
				msg = msg.replaceAll("\n[\n]+", "\n\n");
				sendMsg(msg);
			}
		};
	}

}
