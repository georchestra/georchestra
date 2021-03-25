package org.fao.geonet;

import java.text.DateFormat;
import java.util.Date;

import feign.Param;

/**
 * Param Expander to convert {@link Date} to RFC3339
 */
public class ParamExpander implements Param.Expander {

    private static final DateFormat dateformat = new RFC3339DateFormat();

    @Override
    public String expand(Object value) {
        if (value instanceof Date) {
            return dateformat.format(value);
        }
        return value.toString();
    }
}
