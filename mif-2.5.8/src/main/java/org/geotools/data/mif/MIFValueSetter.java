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

/**
 * <p>Utility class for setting object values from strings and vice-versa.</p>
 * <p>The main use of this class is building a schema-dependent array of "parsers" which speed up the process of reading
 * text lines and converting them into features.</p>
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.5.x/modules/unsupported/mif/src/main/java/org/geotools/data/mif/MIFValueSetter.java $
 * @version $Id: MIFValueSetter.java 30702 2008-06-13 14:57:03Z acuster $
*/
public abstract class MIFValueSetter {
    protected String strValue = null; // The object value as string
    protected Object objValue = null; // the object value
    private String defaultValue = ""; // String representation of the default value (must be correctly converted into object by the stringToValue method!!!)
    private String errorMessage = "";

    /**
     * <p>The constructor accepts a default value for the ValueSetter.</p>
     *
     * @param defa String representation of the default value
     */
    public MIFValueSetter(String defa) {
        defaultValue = defa;
    }

    /**
     * <p>Sets the value as a String. After a setString call, getValue() can be
     * used to access the converted value.</p>
     *
     * @param value String representation of the object value
     *
     * @return true if conversion was successful
     */
    public final boolean setString(String value) {
        strValue = value;

        try {
            stringToValue();

            return true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            strValue = defaultValue;

            try {
                stringToValue();
            } catch (Exception ex) {
                // Should never reach this place!!!!
                objValue = null;
                errorMessage += ". Bad default string value";
            }

            return false;
        }
    }

    /**
     * <p>Returns the string value.</p>
     *
     */
    public final String getString() {
        return strValue;
    }

    /**
     * <p>Sets the object value, and calculates the String value.</p>
     *
     * @param value The Object value
     */
    public final void setValue(Object value) {
        objValue = value;
        valueToString();
    }

    /**
     * <p>Gets the object value.</p>
     *
     */
    public final Object getValue() {
        return objValue;
    }

    /**
     * <p>Gets and resets the current error message.</p>
     *
     * @return The current error message, "" if none
     */
    public final String getError() {
        String tmp = errorMessage;
        errorMessage = "";

        return tmp;
    }

    /**
     * <p>Converts an object value to string - the default implementation uses
     * toString for non-null values.</p>
     */
    protected void valueToString() {
        // String.valueOf() would yeld "null"
        if (objValue == null) {
            strValue = "";
        } else {
            strValue = objValue.toString();
        }
    }

    /**
     * <p>This method must be overridden by descendants in order to implement the
     * correct conversion between strings and object values. <br>
     * Must throw an exception if conversion failed</p>.
     *
     * @throws Exception if value conversion failed
     */
    protected abstract void stringToValue() throws Exception;
}
