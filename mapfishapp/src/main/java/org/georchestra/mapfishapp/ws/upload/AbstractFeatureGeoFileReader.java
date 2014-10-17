/**
 *
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.json.JSONArray;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Defines the abstract interface (Bridge Pattern). This class is responsible of
 * create the implementation OGR or Geotools for the feature reader. Thus the
 * client don't need to create a specific reader implementation.
 *
 * @author Mauricio Pazos
 */
public class AbstractFeatureGeoFileReader implements FeatureGeoFileReader {

    private static final Log       LOG        = LogFactory
                                                      .getLog(AbstractFeatureGeoFileReader.class
                                                              .getPackage()
                                                              .getName());

    protected FeatureGeoFileReader readerImpl = null;

    private FeatureGeoFileReader getReaderImpl() {

        LOG.info("Using implementation: "
                + this.readerImpl.getClass().getName());
        return this.readerImpl;
    }

    private void setReaderImpl(FeatureGeoFileReader readerImpl) {

        LOG.info("It was set: " + readerImpl.getClass().getName());

        this.readerImpl = readerImpl;
    }

    /**
     * Creates a new instance of {@link AbstractFeatureGeoFileReader}.
     *
     * <p>
     * The default implementation will be OGR if it was installed in the system.
     * </p>
     *
     * @param basedir
     *            file to read
     * @param fileFormat
     *            the format
     */
    public AbstractFeatureGeoFileReader() {
        setReaderImpl(createImplementationStrategy());
    }

    /**
     * Creates a new instance of {@link AbstractFeatureGeoFileReader}. The
     * reader will use the implementation provided as parameter.
     *
     * @param impl
     */
    public AbstractFeatureGeoFileReader(FeatureGeoFileReader impl) {

        setReaderImpl(impl);
    }

    /**
     * @return the list of available format depending on the reader
     *         implementation.
     */
    @Override
    public FileFormat[] getFormatList() {
        return getReaderImpl().getFormatList();
    }

    public JSONArray getFormatListAsJSON() {
        JSONArray ret = new JSONArray();

        FileFormat[] ff = getFormatList();
        for (FileFormat f: ff) {
            ret.put(f.toString());
        }
        return ret;
    }

    /**
     * Returns the feature collection contained by the file.
     *
     * @param file
     * @param fileFormat
     * @param targetCrs
     *            crs used to reproject the returned feature collection
     *
     * @return {@link SimpleFeatureCollection}
     *
     * @throws IOException
     * @throws UnsupportedGeofileFormatException
     */
    @Override
    public SimpleFeatureCollection getFeatureCollection(final File file,
            final FileFormat fileFormat) throws IOException,
            UnsupportedGeofileFormatException, ProjectionException {

        return getFeatureCollection(file, fileFormat, null);
    }

    /**
     * Returns the feature collection contained by the file. The features will
     * be reprojected to the target CRS
     *
     * @param file
     *            path and file name
     * @param fileFormat
     * @param targetCrs
     *            crs used to reproject the returned feature collection
     *
     * @return {@link SimpleFeatureCollection}
     * @throws IOException
     * @throws UnsupportedGeofileFormatException
     */
    @Override
    public SimpleFeatureCollection getFeatureCollection(final File file,
            final FileFormat fileFormat,
            final CoordinateReferenceSystem targetCrs) throws IOException,
            UnsupportedGeofileFormatException, ProjectionException {
        try {
            return getReaderImpl().getFeatureCollection(file, fileFormat,
                    targetCrs);
        } catch (UnsupportedGeofileFormatException e) {
            throw e;
        } catch (ProjectionException e) {
            throw e;
        } catch (RuntimeException e) {
            // if an error was found and the current implementation is the OGR
            // the implementation then it will be changed to geotools (only for
            // this operation)
            if (this.readerImpl.allowsGeoToolsFallback()) {

                LOG.info("OGRFeatureReader fail. Try using the geotools implementation: "
                        + readerImpl.getClass().getName());

                FeatureGeoFileReader savedReader = this.readerImpl;
                setReaderImpl(new GeotoolsFeatureReader());

                // if the format is available in geotools then the last read
                // operation will be re-executed.
                if (getReaderImpl().isSupportedFormat(fileFormat)) {

                    SimpleFeatureCollection features = getReaderImpl()
                            .getFeatureCollection(file, fileFormat, targetCrs);

                    setReaderImpl(savedReader);

                    return features;

                } else {

                    setReaderImpl(savedReader);

                    throw new UnsupportedGeofileFormatException(
                            "The format is not supported by geotools implementation");
                }
            } else {

                throw e; // geotools implementation fails

            }
        }

    }

    /**
     * Selects which of the implementations must be created.
     */
    private static FeatureGeoFileReader createImplementationStrategy() {

        FeatureGeoFileReader ogrReader = null;

        // checks the OGR status
        if (OGRFeatureReader.isOK()) {

            try {
                ogrReader = new OGRFeatureReader();

            } catch (IOException e) {
                LOG.info("It cannot create OGR implementation, Geotools will be set.");
            }
        }
        // if the ogr implementation cannot be created, then use the Geotools
        // implementation.
        if (ogrReader == null) {
            return new GeotoolsFeatureReader();
        }

        // Decides what is the better implementation.
        // OGR will be better implementation than Geotools if and only if the
        // OGR contains all geotools formats. (In other words, geotools formats
        // are a subset of ogr)
        FileFormat[] ogrFormats = ogrReader.getFormatList();

        FeatureGeoFileReader gtReader = new GeotoolsFeatureReader();
        FileFormat[] gtFormats = gtReader.getFormatList();

        for (FileFormat gtFormat : gtFormats) {

            boolean found = false;
            for (FileFormat ogrFormat : ogrFormats) {

                if (gtFormat.equals(ogrFormat)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return gtReader;
            }
        }
        return ogrReader;
    }

    @Override
    public boolean isSupportedFormat(FileFormat fileFormat) {

        return this.readerImpl.isSupportedFormat(fileFormat);
    }

	@Override
	public boolean allowsGeoToolsFallback() {
		return false;
	}

}
