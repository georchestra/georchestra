package org.georchestra.ldapadmin.model;

import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class EmailEntryTest {

	@Test
	public final void testDateFormat() throws JSONException {
		EmailEntry em = new EmailEntry();
		em.setId(0);
		em.setSender(UUID.randomUUID());
		em.setRecipient(UUID.randomUUID());
		em.setSubject("Test subject");
		em.setBody("Test email body");
		em.setAttachments(new ArrayList<Attachment>(0));
		em.setDate(new Date());

		JSONObject json = em.toJSON();

		// ensures the date can be parsed back
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			df1.parse(json.getString("date"));
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}

}
