package org.georchestra.mapfishapp.ws.upload;

/**
 * GeoFile formats.
 * <p>
 * </p>
 *
 * @author Mauricio Pazos
 */
public enum FileFormat {

    shp, mif, tab, gml, kml, gpx, osm;

    public static boolean contains(final String ext) {

        String lcExt = ext.toLowerCase();

        FileFormat[] formats = values();
        for (int i = 0; i < formats.length; i++) {

            if (formats[i].toString().equals(lcExt)) {
                return true;
            }
        }
        return false;
    }
}