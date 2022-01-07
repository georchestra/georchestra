/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

public class EmailFactoryDefault extends AbstractEmailFactory {

    @Override
    public Email createEmail(HttpServletRequest request, final String[] recipients, final String url)
            throws IOException {

        final long expiry = this.expireDeamon.getExpiry();
        final String msgAck = readFile(request, emailAckTemplateFile);
        final String msgDone = readFile(request, emailTemplateFile);
        final HashMap<String, String> extraKeywords = readExtraKeywords(extraKeywordsFile);

        return new Email(request, recipients, emailSubject, this.smtpHost, this.smtpPort, this.emailHtml, this.replyTo,
                this.from, this.bodyEncoding, this.subjectEncoding, this.publicUrl, this.instanceName) {
            public void sendDone(List<String> successes, List<String> failures, List<String> oversized, long fileSize)
                    throws MessagingException {

                String globalStatus = "";
                if (successes.size() > 0 && failures.size() > 0) {
                    globalStatus = extraKeywords.get("partial_success");
                } else if (successes.size() > 0 && failures.size() <= 0) {
                    globalStatus = extraKeywords.get("success");
                } else if (successes.size() <= 0 && failures.size() > 0) {
                    globalStatus = extraKeywords.get("failure");
                }

                LOG.debug("preparing to send extraction result email");
                String msg = new String(msgDone);
                if (msg != null) {
                    msg = msg.replaceAll("\\{publicUrl\\}", publicUrl);
                    msg = msg.replaceAll("\\{instanceName\\}", instanceName);
                    msg = msg.replace("{link}", url);
                    msg = msg.replace("{emails}", Arrays.toString(recipients));
                    msg = msg.replace("{expiry}", String.valueOf(expiry));
                    msg = msg.replace("{successes}", format(successes, "{success}"));
                    msg = msg.replace("{failures}", format(failures, "{failure}"));
                    msg = msg.replace("{oversized}", format(oversized, "{failure_oversized}"));

                    // extra parameters
                    msg = msg.replace("{global_status}", globalStatus);
                    for (String key : extraKeywords.keySet()) {
                        msg = msg.replace(String.format("{%s}", key), extraKeywords.get(key));
                    }
                }
                // Normalize newlines
                msg = msg.replaceAll("\n[\n]+", "\n\n");
                sendMsg(msg);
            }

            public void sendAck() throws AddressException, MessagingException {
                LOG.debug("preparing to send extraction acknowledgement email");
                String msg = new String(msgAck);
                if (msg != null) {
                    msg = msg.replaceAll("\\{publicUrl\\}", publicUrl);
                    msg = msg.replaceAll("\\{instanceName\\}", instanceName);
                }
                // Normalize newlines
                msg = msg.replaceAll("\n[\n]+", "\n\n");
                sendMsg(msg);
            }
        };
    }

}
