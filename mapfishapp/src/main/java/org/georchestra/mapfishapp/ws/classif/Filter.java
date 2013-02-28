package org.georchestra.mapfishapp.ws.classif;

/**
 * A filter wrapper class.
 * @author eric.lemoine@camptocamp.com
 */
public class Filter {

    private final org.opengis.filter.Filter filter;
    private final String name;

    /**
     * Create a filter wrapping an OpenGIS filter.<br />
     * @param _filter The OpenGIS filter.
     * @param _name The filter name.
     */
    public Filter(final org.opengis.filter.Filter _filter, final String _name) {
        filter = _filter;
        name = _name;
    }

    /**
     * Get the filter name.
     * @return The filter name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the GIS filter.
     * @return The GIS filter.
     */
    public org.opengis.filter.Filter getGISFilter() {
        return filter;
    }
}
