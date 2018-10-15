/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.json.JSONArray;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Mock Reader for testing support
 * 
 * @author Mauricio Pazos
 *
 */
public final class MockReader implements FeatureGeoFileReader {

	/* (non-Javadoc)
	 * @see org.georchestra.mapfishapp.ws.upload.FeatureFileReaderImplementor#getFeatureCollection(java.io.File, org.georchestra.mapfishapp.ws.upload.FileFormat)
	 */
	@Override
	public SimpleFeatureCollection getFeatureCollection(File file,
			FileFormat fileFormat) throws IOException {
		throw new IOException("unsupported");
	}

	/* (non-Javadoc)
	 * @see org.georchestra.mapfishapp.ws.upload.FeatureFileReaderImplementor#getFeatureCollection(java.io.File, org.georchestra.mapfishapp.ws.upload.FileFormat, org.opengis.referencing.crs.CoordinateReferenceSystem)
	 */
	@Override
	public SimpleFeatureCollection getFeatureCollection(File file,
			FileFormat fileFormat, CoordinateReferenceSystem targetCrs)
			throws IOException {
		
		throw new RuntimeException("unsupported");
		
	}

	@Override
	public JSONArray getFormatListAsJSON() {
		return new JSONArray();
	}

	/* (non-Javadoc)
	 * @see org.georchestra.mapfishapp.ws.upload.FeatureFileReaderImplementor#getFormatList()
	 */
	@Override
	public FileFormat[] getFormatList() {
		return new FileFormat[]{};
	}

	@Override
	public boolean isSupportedFormat(FileFormat fileFormat) {
		throw new UnsupportedOperationException("unsupported");
	}
}
