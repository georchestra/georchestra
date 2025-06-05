/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.console.model;

import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class EmailEntryTest {

    @Test
    public final void testDateFormat() throws JSONException {
        EmailEntry em = new EmailEntry();
        em.setId(0);
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
