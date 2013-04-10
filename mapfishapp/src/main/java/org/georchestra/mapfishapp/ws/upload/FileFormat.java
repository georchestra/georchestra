package org.georchestra.mapfishapp.ws.upload;

/**
 * Available File formats.
 *
 * @author Mauricio Pazos
 */
public enum FileFormat {
	
		tab, mif ,shp , gml, kml , gpx; 

		/**
		 * Returns the enumerated value associated to the extension file name
		 *  
		 * @param ext
		 * @return FileFormat enumerated value or null if it doesn't exist.
		 */
		public static FileFormat getFileFormat(String ext) {
			
			if("tab".equalsIgnoreCase(ext))	return tab;
			if("mif".equalsIgnoreCase(ext))	return mif;
			if("shp".equalsIgnoreCase(ext))	return shp;
			if("gml".equalsIgnoreCase(ext))	return gml;
			if("gpx".equalsIgnoreCase(ext))	return gpx;
			if("kml".equalsIgnoreCase(ext))	return kml;
			
			return null;
		}
}