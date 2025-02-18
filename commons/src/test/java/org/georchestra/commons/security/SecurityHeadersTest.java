/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.commons.security;

import static java.util.Arrays.asList;
import static org.georchestra.commons.security.SecurityHeaders.decode;
import static org.georchestra.commons.security.SecurityHeaders.decodeAsList;
import static org.georchestra.commons.security.SecurityHeaders.encodeBase64;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

public class SecurityHeadersTest {

    @Test
    public void encodeBase64_Null() {
        assertNull(encodeBase64((String) null));
    }

    @Test
    public void encodeBase64_Japanese() {
        final String givenName = "ガブリエル";
        final String lastName = "ロルダン";

        final String encodedGivenName = "44Ks44OW44Oq44Ko44Or";
        final String encodedLastName = "44Ot44Or44OA44Oz";

        assertEquals("{base64}" + encodedGivenName, encodeBase64(givenName));
        assertEquals("{base64}" + encodedLastName, encodeBase64(lastName));
    }

    @Test
    public void encodeBase64_Empty() {
        assertEquals("", encodeBase64(""), "empty string should remain empty string");
        assertEquals("", decode(""));
    }

    @Test
    public void encodeBase64_Space() {
        assertEquals("{base64}IA==", encodeBase64(" "));
        assertEquals(" ", decode("{base64}IA=="));
    }

    @Test
    public void encodeBase64_Spaces() {
        assertEquals("{base64}ICAgICAgICAgIA==", encodeBase64("          "));
        assertEquals("          ", decode("{base64}ICAgICAgICAgIA=="));
    }

    @Test
    public void encodeBase64_PrefixString() {
        assertEquals("{base64}e2Jhc2U2NH0=", encodeBase64("{base64}"));
        assertEquals("{base64}", decode("{base64}e2Jhc2U2NH0="));
    }

    @Test
    public void encodeBase64_Newlines() {
        assertEquals("{base64}dGV4dAp3aXRoDQoKZXdsaW5lcwoK", encodeBase64("text\nwith\r\n\newlines\n\n"));
        assertEquals("text\nwith\r\n\newlines\n\n", decode("{base64}dGV4dAp3aXRoDQoKZXdsaW5lcwoK"));
    }

    @Test
    public void testDecodeNull() {
        assertNull(decode(null));
    }

    @Test
    public void testDecodeEmpty() {
        assertEquals("", decode(""));
    }

    @Test
    public void testDecode_NoEncoding() {
        final String givenName = "ガブリエル";
        assertEquals(givenName, decode(givenName));
        assertEquals("{}" + givenName, decode("{}" + givenName));
        assertEquals("{" + givenName + "}", decode("{" + givenName + "}"));
    }

    @Test
    public void testDecode_Base64() {
        final String givenName = "ガブリエル";
        final String lastName = "ロルダン";

        assertEquals(givenName, decode(encodeBase64(givenName)));
        assertEquals(lastName, decode(encodeBase64(lastName)));

        assertEquals("{}" + givenName, decode(encodeBase64("{}" + givenName)));
        assertEquals("{" + lastName + "}", decode(encodeBase64("{" + lastName + "}")));
    }

    @Test
    public void testMultipleValues() {
        final String givenName = "ガブリエル";
        final String lastName = "ロルダン";
        // a header value with a comma itself should always be encoded, up to the user
        // if it's not, by HTTP spec, multiple header values shall be comma separated
        final String fullName = lastName + ", " + givenName;

        String encoded = encodeBase64(givenName, lastName, fullName);
        String decoded = decode(encoded);
        assertEquals(String.format("%s,%s,%s", givenName, lastName, fullName), decoded);

        List<String> values = decodeAsList(encoded);
        assertEquals(3, values.size());
        assertEquals(givenName, values.get(0));
        assertEquals(lastName, values.get(1));
        assertEquals(fullName, values.get(2));
    }

    @Test
    public void testMultipleValuesWithEmptyElementsPreserved() {
        final String givenName = "ガブリエル";
        final String lastName = "ロルダン";

        String encoded = encodeBase64("", givenName, "", lastName, "");
        String decoded = decode(encoded);
        assertEquals(String.format(",%s,,%s,", givenName, lastName), decoded);

        List<String> values = decodeAsList(encoded);
        assertEquals(5, values.size());
        assertEquals("", values.get(0));
        assertEquals(givenName, values.get(1));
        assertEquals("", values.get(2));
        assertEquals(lastName, values.get(3));
        assertEquals("", values.get(4));
    }

    @Test
    public void testDecodeMultipleValuesNoEncoding() {
        assertEquals("v1,V2,v3", decode("v1,V2,v3"));
        assertEquals(",v1,,V2,,v3,", decode(",v1,,V2,,v3,"));
        assertEquals(",v1,,V2,,v3,,,", decode(",v1,,V2,,v3,,,"));
        assertEquals(",,,,", decode(",,,,"));

        assertEquals(asList("v1", "V2", "v3"), decodeAsList("v1,V2,v3"));
        assertEquals(asList("", "v1", "", "V2", "", "v3", ""), decodeAsList(",v1,,V2,,v3,"));
        assertEquals(asList("", "", ""), decodeAsList(",,"));
    }
}
