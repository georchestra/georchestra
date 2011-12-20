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

import java.io.BufferedReader;
import java.io.IOException;


/**
 * Simple tokenizer class for BufferedReaders
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.5.x/modules/unsupported/mif/src/main/java/org/geotools/data/mif/MIFFileTokenizer.java $
 * @version $Id: MIFFileTokenizer.java 30702 2008-06-13 14:57:03Z acuster $
 */
public class MIFFileTokenizer extends MIFStringTokenizer {
    private BufferedReader reader = null;
    private int lineNumber = 0;

    /**
     * Builds a tokenizer for a BufferedReader
     *
     * @param aReader
     */
    public MIFFileTokenizer(BufferedReader aReader) {
        super();
        reader = aReader;
    }

    /**
     * Closes the associated reader.
     */
    public void close() {
        try {
            reader.close();
            reader = null;
        } catch (Exception e) {
        }
    }

    /**
     * Stores the next non-null line from file buffer in the line buffer
     *
     * @return True if a non-null string was read, false if EOF or error
     */
    public boolean readLine() {
        String buffer = "";

        do {
            try {
                buffer = reader.readLine();

                if (buffer == null) {
                    return readLine(""); //EOF
                }
            } catch (IOException e) {
                return readLine("");
            }

            lineNumber++;
            buffer = buffer.trim();
        } while (buffer.length() == 0);

        return readLine(buffer);
    }

    /**
     * Returns the current line number
     *
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
