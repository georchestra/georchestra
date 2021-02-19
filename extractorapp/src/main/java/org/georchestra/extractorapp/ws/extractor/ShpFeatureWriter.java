/**
 * 
 */
package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;

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

    @Override
    public File[] generateFiles() throws IOException {

        ShapefileDumper dumper = new ShapefileDumper(super.basedir);
        dumper.setEmptyShapefileAllowed(true);
        dumper.setCharset(StandardCharsets.UTF_8);
        try {
            dumper.dump(super.features);
        } catch (IOException e) {
            LOG.error("Failed generation of " + super.schema.getName(), e);
            throw e;
        }
        File[] files = this.basedir.listFiles();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated " + Arrays.stream(files).map(File::getName).collect(Collectors.joining(",")));
        }
        return files;
    }
}
