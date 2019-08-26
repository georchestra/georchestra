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
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * This abstract class defines the template strategy required to write different
 * sort of vector files.
 * 
 * @author Mauricio Pazos
 *
 */
abstract class FileFeatureWriter implements FeatureWriterStrategy {

	protected static final Log LOG = LogFactory.getLog(FileFeatureWriter.class.getPackage().getName());

	protected ProgressListener progresListener;
	protected SimpleFeatureType schema;
	protected File basedir;
	protected FeatureCollection<SimpleFeatureType, SimpleFeature> features;

	/**
	 * Sets the strategy parameters
	 * 
	 * @param progresListener
	 * @param schema          output schema
	 * @param basedir         output base folder
	 * @param features        the input set of features to write
	 */
	public FileFeatureWriter(ProgressListener progresListener, SimpleFeatureType schema, File basedir,
			SimpleFeatureCollection features) {

		this.progresListener = progresListener;
		this.schema = schema;
		this.basedir = basedir;
		this.features = features;

	}

	/**
	 * Generates a vector files in the specified format
	 * 
	 * @throws IOException
	 */
	@Override
	public File[] generateFiles() throws IOException {

		File[] files = null;
		WriteFeatures writeFeatures = null;

		try {
			DatastoreFactory ds = getDatastoreFactory();

			// the sources features are projected in the requested output projections
			CoordinateReferenceSystem outCRS = this.features.getSchema().getCoordinateReferenceSystem();
			writeFeatures = new WriteFeatures(this.schema, this.basedir, outCRS, ds);

			this.features.accepts(writeFeatures, this.progresListener);

			files = writeFeatures.getShapeFiles();

			if (LOG.isDebugEnabled()) {

				for (int i = 0; i < files.length; i++) {
					LOG.debug("Generated file: " + files[i].getAbsolutePath());
				}
			}

			return files;

		} catch (IOException e) {
			e.printStackTrace();
			final String message = "Failed generation: " + this.schema.getName() + " - " + e.getMessage();
			LOG.error(message);

			throw e;

		} finally {
			if (writeFeatures != null)
				writeFeatures.close();

		}
	}

	/**
	 * @return a {@link DatastoreFactory} instance
	 * @throws IOException
	 */
	protected abstract DatastoreFactory getDatastoreFactory() throws IOException;

}
