/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 * 
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.mif;

import com.vividsolutions.jts.io.ParseException;


/**
 * Simple tokenizer class
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.5.x/modules/unsupported/mif/src/main/java/org/geotools/data/mif/MIFStringTokenizer.java $
 * @version $Id: MIFStringTokenizer.java 30702 2008-06-13 14:57:03Z acuster $
 */
public class MIFStringTokenizer {
    private String line = ""; // Current line buffer
    private String lastToken = null; // Current extracted token

    /**
     * Builds a tokenizer
     */
    public MIFStringTokenizer() {
        super();
    }

    /**
     * "Reads" a line from the given line, and initializes the token.
     *
     * @param line
     *
     * @return true if could read a non empty line (i.e. line != "")
     */
    public boolean readLine(String line) {

        if (line == null) {
            this.line = "";
        } else {
            this.line = ltrim(line);
        }

        return (!line.equals(""));
    }

    /**
     * Tries to read a line from the input buffer (if any) and store it in the
     * line buffer
     *
     * @return True if a non-null string was read
     */
    public boolean readLine() {
        return readLine("");
    }

    /**
     * Cuts the first token from line buffer using the given separator,  taking
     * in to account string delimiters if needed <br>
     * Strings might be delimited by double quotes. Escaping char is a double
     * quote, i.e. "\"" becomes """".
     *
     * @param separator Character used as token separator.
     * @param nextLineIfEmpty If the returned token is empty, try to read it
     *        from the next line.
     * @param quotedStrings If true, expects a quoted string and read it,
     *        otherwise treats the double quotes as a normal char.
     *
     * @return The parsed token
     *
     * @throws ParseException DOCUMENT ME!
     */
    public String getToken(char separator, boolean nextLineIfEmpty,
        boolean quotedStrings) throws ParseException {
        
        String token = "";

        if (lastToken != null) {
            token = lastToken;
            lastToken = null;
            return token;
        }
        
        line = ltrim(line);

        if (line.equals("") && nextLineIfEmpty) {
            readLine();
        }

        if (line.equals("")) {
            return "";
        }

        int index = -1;

        if (quotedStrings && line.startsWith("\"")) {
            try {
                index = 1;

                boolean loop = true;
                int len = line.length();

                while (loop) {
                    while ((index < len) && (line.charAt(index) != '"'))
                    		index ++;
                    
                    if (	(index < (len - 2)) && 
                    		(line.charAt(index) == '"') && 
                    		(line.charAt(index + 1) == separator)) {
                    	loop = false;
                    } else if (	(index == (len - 1)) && 
                    			(line.charAt(index) == '"') ) {
                    	loop = false;
                    } else {
                    	index ++;
                    }
                }
                
                token = line.substring(1, index).replaceAll("\"\"", "\"");
                line = ltrim(line.substring(index + 1));

                if (line.length() > 0) {
                    if (line.charAt(0) == separator) {
                        line = line.substring(1);
                    } else {
                        if (separator != ' ') throw new ParseException("Bad separator");
                    }
                }
            } catch (Exception e) {
                throw new ParseException("Error reading quoted string");
            }
        } else {
            index = line.indexOf(separator);

            if (index == -1) {
                token = line;
                line = "";
            } else {
                token = ltrim(line.substring(0, index));
                line = ltrim(line.substring(index + 1));
            }
        }

        return token;
    }

    /**
     * DOCUMENT ME!
     *
     * @param separator DOCUMENT ME!
     * @param nextLineIfEmpty DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    protected String getToken(char separator, boolean nextLineIfEmpty)
        throws ParseException {
        return getToken(separator, nextLineIfEmpty, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param separator DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    protected String getToken(char separator) throws ParseException {
        return getToken(separator, false, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    protected String getToken() throws ParseException {
        return getToken(' ', false, false);
    }

    /**
     * Puts a token back to the input buffer so that the next call to getToken will
     * return this token
     * @param tok The token which has to be put back in the input buffer
     */
    public void putToken(String tok) {
        lastToken = tok;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param unquoted DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String strQuote(String unquoted) {
        return "\"" + unquoted.replaceAll("\"", "\"\"") + "\"";
    }

    /**
     * DOCUMENT ME!
     *
     * @param quoted DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String strUnquote(String quoted) {
        if (quoted.startsWith("\"") && quoted.endsWith("\"")
                && (quoted.length() > 1)) {
            quoted = quoted.substring(1, quoted.length() - 1).replaceAll("\"\"",
                    "\"");
        }

        return quoted;
    }

    // Can't use String.trim() when Delimiter is \t
    // TODO use stringBuffer and a better algorithm
    public static String ltrim(String untrimmed) {
        while ((untrimmed.length() > 0) && (untrimmed.charAt(0) == ' ')) {
            untrimmed = untrimmed.substring(1);
        }

        return untrimmed;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getLine() {
        return line;
    }

    /**
     * Check for non-empty line buffer
     *
     * @return true if current line buffer is not empty
     */
    public boolean isEmpty() {
        return line.equals("");
    }
}
