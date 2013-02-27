package org.georchestra.extractorapp.ws;

/**
 * Default params with the recipients and message
 * 
 * @author jeichar
 */
public class CompleteEmailParams extends EmailDefaultParams {

    private final String[] recipients;
    private final String   message;
    private final String   subject;

    /**
     * Copies the defaults to the new object (defaults are not modified)
     * 
     * @param defaults
     */
    public CompleteEmailParams(EmailDefaultParams defaults, String[] recipients, String subject, String message) {
        super(defaults);
        this.recipients = recipients;
        this.message = message;
        this.subject = subject;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public String getMessage() {
        return message;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public void freeze() {
        // do nothing. Only the actual
        // defaults object can be frozen
    }
}
