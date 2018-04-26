/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.util.ProgressListener;

/**
 * This class implements the KML file writing strategy
 *
 * @author Florent Gravin
 *
 */
final class KMLFeatureWriter extends FileFeatureWriter {

	/**
	 * New instance of {@link OGRFeatureWriter}
	 *
	 * @param progressListener
	 * @param schema		output schema
	 * @param basedir		output folder
	 * @param features		input the set of Features to write
	 */
	public KMLFeatureWriter(
			ProgressListener progresListener,
			SimpleFeatureType schema,
			File basedir,
			SimpleFeatureCollection features) {

		super(progresListener, schema, basedir, features);

	}

	/**
	 * @return {@link ShpDatastoreFactory}
	 */
	@Override
	protected DatastoreFactory getDatastoreFactory() throws  IOException{
		return null;
	}

	/**
	 *
	 * @return Format file extension
	 */
	protected String extension() {
		return "kml";
	}

	/**
	 * Generates a vector files in the specified format
	 *
	 * @throws IOException
	 */
	@Override
	public File[] generateFiles() throws IOException {

		File[] files = null;
		FileOutputStream fop = null;

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder ();
		builder.setName (schema.getName ());

		try{
			File file = new File(basedir, builder.getName() + "."+ extension());
			fop = new FileOutputStream(file);

			Encoder encoder = new Encoder(new KMLConfiguration());
			encoder.setIndenting(true);

			encoder.encode(features, KML.kml, fop );

			files = new File[1];
			files[0] = file;

			if(LOG.isDebugEnabled()){

				for (int i = 0; i < files.length; i++) {
					LOG.debug("Generated file: " + files[i].getAbsolutePath() );
				}
			}
			fop.flush();
			return files;

		} catch (IOException e ){

			final String message = "Failed generation: " + this.schema.getName() + " - "  +  e.getMessage();
			LOG.error(message);

			throw e;
		}
		finally {
			if(fop != null) {
				fop.close();
			}

		}
	}


}
