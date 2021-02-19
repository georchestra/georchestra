/**
 * 
 */
package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.util.NullProgressListener;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This class implements the shape file writing strategy
 * 
 * @author Mauricio Pazos
 *
 */
final class ShpFeatureWriter extends FileFeatureWriter {

    /**
     * New instance of {@link OGRFeatureWriter}
     * 
     * @param schema   output schema
     * @param basedir  output folder
     * @param features input the set of Features to write
     */
    public ShpFeatureWriter(SimpleFeatureType schema, File basedir, SimpleFeatureCollection features) {

        super(schema, basedir, features);

    }

    /**
     * @return {@link ShpDatastoreFactory}
     */
    protected DatastoreFactory getDatastoreFactory() throws IOException {
        ShpDatastoreFactory factory = new ShpDatastoreFactory();
        return factory;
    }

    @Override
    public File[] generateFiles() throws IOException {

        File[] files = null;
        WriteFeatures writeFeatures = null;

        try {
            DatastoreFactory ds = getDatastoreFactory();

            // the sources features are projected in the requested output projections
            CoordinateReferenceSystem outCRS = this.features.getSchema().getCoordinateReferenceSystem();
            writeFeatures = new WriteFeatures(this.schema, this.basedir, outCRS, ds);

            this.features.accepts(writeFeatures, new NullProgressListener());

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

}
