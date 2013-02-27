package org.georchestra.extractorapp.ws.extractor.wcs;

import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.extractorapp.ws.ExtractorException;
import org.georchestra.extractorapp.ws.extractor.FileUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.coverage.processing.Operations;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.renderer.lite.RendererUtilities;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;


/**
 * Reads coverages from a WCS.
 * 
 * Currently a grid coverage can only be read if the name of the layer is known.
 * 
 * This is an implementation of the Geotools coverage reader API which makes a
 * request to the WCS and returns the grid coverage received.
 * 
 * Currently each request for the coverage results in a new read but future
 * versions may be able to add some caching.
 * 
 * The API is extended to allow the coverage to be written to a file rather than
 * constructing a GridCoverage object
 * 
 * Be warned that not all of the API is supported because they are not required
 * for geOrchestra but this class could be the start of support for a
 * full-fledged WCS client
 * 
 * @see #readToFile(File, String, GeneralParameterValue[])
 * 
 * @author jeichar
 */
@SuppressWarnings({ "deprecation" })
public class WcsCoverageReader extends AbstractGridCoverage2DReader {

    private static final Log LOG = LogFactory.getLog(BoundWcsRequest.class
            .getPackage().getName());

    private final URL _wcsUrl;
    private final long _maxCoverageExtractionSize;

    /**
     * @param url
     *            The url of the service <strong>WITH OUT</strong> the query
     * @param maxSize
     */
    public WcsCoverageReader(URL url, long maxSize) {
        _wcsUrl = url;
        _maxCoverageExtractionSize = maxSize;
    }

    @Override
    public WcsFormat getFormat() {
        return new WcsFormat(_maxCoverageExtractionSize);
    }

    @Override
    public GridCoverage2D read(GeneralParameterValue[] parameters)
            throws IllegalArgumentException, IOException {
        File baseDir = FileUtils.createTempDirectory();

        try {
            File file = readToFile(baseDir, "WcsCoverageReader", parameters);

            // since the requested format may be one of several we need to look
            // up the format object
            // for reading the coverage from the file
            Format format = GridFormatFinder.findFormat(file);

            if (format instanceof AbstractGridFormat
                    && UnknownFormat.class != format.getClass()) {
                AbstractGridFormat gridFormat = (AbstractGridFormat) format;

                // Now we need to find the parameters provided by the caller for
                // reading the coverage from the file
                // See the format API for details
                GeneralParameterValue[] readParams = new GeneralParameterValue[0];
                try {
                    ParameterValueGroup group = format.getReadParameters();
                    List<GeneralParameterValue> list = new ArrayList<GeneralParameterValue>();
                    for (GeneralParameterValue paramValue : group.values()) {
                        list.addAll(find(paramValue, parameters));
                    }
                    LOG.debug("Reading coverage from file using parameters: "
                            + list);
                    readParams = list.toArray(new GeneralParameterValue[list
                            .size()]);
                } catch (Exception e) {
                    // if can't configure the parameters then try to read with
                    // what we have been able to configure
                    LOG.warn(
                            "Exception occurred while getting request params for reading coverage",
                            e);
                }
                AbstractGridCoverage2DReader reader = gridFormat
                        .getReader(file);
                GridCoverage2D coverage = reader.read(readParams);
                return coverage;
            }
            throw new IllegalArgumentException(
                    "Current configuration is unable to read coverage of "
                            + file.getName()
                            + " format.  "
                            + "Check that you have the correct geotools plugins");

        } finally {
            FileUtils.delete(baseDir);
        }
    }

    /**
     * Obtains the coverage from the server and writes it to a file with a .prj
     * and .wld file unless the requested type is a geotiff.
     * 
     * @param containingDirector
     *            the data to write the files to
     * @param baseFilename
     *            the name (sans extension) of the files
     * @param parameters
     *            the parameters for making the request
     * @return the image file
     * 
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public File readToFile(File containingDirectory, String baseFilename,
            GeneralParameterValue[] parameters)
            throws IllegalArgumentException, IOException {

        try {
            WcsReaderRequest request = WcsReaderRequestFactory
                    .create(parameters);
            if (request.remoteReproject) {
                return remoteReproject(request, containingDirectory,
                        baseFilename);
            } else {
                return localReproject(request, containingDirectory,
                        baseFilename);
            }
        } catch (NoSuchAuthorityCodeException e) {
            throw new RuntimeException(e);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }

    /* ------------------- Support methods for readToFile ------------------- */
    private File remoteReproject(WcsReaderRequest request,
            File containingDirectory, String baseFilename)
            throws NoSuchAuthorityCodeException, FactoryException, IOException {
        InputStream input = null;
        try {
            BoundWcsRequest requestNegotiatedFormat = negotiateFormat(request
                    .bind(_wcsUrl));
            BoundWcsRequest requestNegotiatedFormatCrs = negotiateRequestCRS(requestNegotiatedFormat);
            BoundWcsRequest requestNegotiatedFormatCrs2 = negotiateResponseCRS(requestNegotiatedFormatCrs);
            requestNegotiatedFormatCrs2
                    .assertLegalSize(_maxCoverageExtractionSize);

            input = requestNegotiatedFormatCrs2.getCoverage();

            // file = new File (new File("/tmp/"),
            // baseFilename+"."+request.fileExtension());
            File file = null;
            file = new File(containingDirectory, baseFilename + "."
                    + request.fileExtension());
            LOG.debug("Writing GridCoverage obtained from " + _wcsUrl
                    + " to file " + file);

            convertFormat(baseFilename, input, file, request,
                    requestNegotiatedFormatCrs2);

            transformCoverage(file, file, request, requestNegotiatedFormatCrs2,false);

            return file;
        } finally {
            if (input != null)
                IOUtils.closeQuietly(input);
        }
    }

    private File localReproject(WcsReaderRequest request,
            File containingDirectory, String baseFilename)
            throws NoSuchAuthorityCodeException, FactoryException, IOException {
        InputStream input = null;
        File tmpFile = null;
        try {
            BoundWcsRequest geotiffRequest = request.bind(_wcsUrl).withFormat("geotiff");
            BoundWcsRequest requestNativeFormat = geotiffRequest.withCRS(geotiffRequest.getNativeCRSs().iterator().next());
            requestNativeFormat.assertLegalSize(_maxCoverageExtractionSize);

            input = requestNativeFormat.getCoverage();

            File file = null;
            file = new File(containingDirectory, baseFilename + "." + request.fileExtension());
            LOG.debug("Writing GridCoverage obtained from " + _wcsUrl + " to file " + file);

            tmpFile = File.createTempFile(baseFilename, ".tif");
            writeToFile(tmpFile, input);

            transformCoverage(tmpFile, file, request, requestNativeFormat, true);
            return file;
        } finally {
        	if(tmpFile != null)
        		FileUtils.delete(tmpFile);
        	
            if (input != null)
                IOUtils.closeQuietly(input);
        }
    }

    void transformCoverage(final File sourceFile, final File file,
            final WcsReaderRequest targetRequest,
            WcsReaderRequest executedRequest, boolean handleFormatTranform) throws IOException {
        final CoordinateReferenceSystem original = targetRequest.responseCRS;
        CoordinateReferenceSystem actual = executedRequest.responseCRS;

        if (!CRS.equalsIgnoreMetadata(original, actual)) {
            try {
                LOG.info("Need to reproject coverage from "
                        + CRS.lookupIdentifier(actual, false) + " to "
                        + CRS.lookupIdentifier(original, false));
            } catch (FactoryException e) {
                LOG.info("Need to reproject coverage from " + actual.getName()
                        + " to " + original.getName());
            }
            if (targetRequest.useCommandLineGDAL) {
                GDALCommandLine.gdalTransformation(sourceFile, file, executedRequest, targetRequest);
            } else {
                geotoolsTranformation(sourceFile, file, targetRequest, original);
            }
            
            LOG.info("Coverage reprojection/transformation complete");
        } else if(handleFormatTranform) {
            if(targetRequest.useCommandLineGDAL) {
                GDALCommandLine.gdalTransformation(sourceFile, file, executedRequest, targetRequest);
            } else {
                // we need to reimplement convertFormat so it can handle non-world+image outputformats
                throw new UnsupportedOperationException("We do not convert format from geotiff to another format yet in localReproject mode.  Should be pretty easy to implement though");
            }
        } else if(!sourceFile.equals(file)) {
            FileUtils.moveFile(sourceFile, file);
        }
        
    }

    private void geotoolsTranformation(final File sourceFile,
            final File file, final WcsReaderRequest request,
            final CoordinateReferenceSystem original) throws IOException {
        LOG.info("using Geotools libraries to tranform the coverage");
        CoverageTransformation<Object> transformation = new CoverageTransformation<Object>() {

            @Override
            public Object transform(GridCoverage coverage) throws IOException,
                    FactoryException {
                boolean writeToTmp = sourceFile.equals(file);
                Hints hints = new Hints(GeoTools.getDefaultHints());
                hints.put(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
                GeoTools.init(hints);
                Coverage transformed = Operations.DEFAULT.resample(coverage,
                        original);

                AbstractGridFormat format = Formats.getFormat(request.format);
                if (writeToTmp) {
                    File tmpDir = FileUtils.createTempDirectory();
                    try {
                    File tmpFile = new File(tmpDir, file.getName());

                    // write must be to tmpFile because Geotools does not always
                    // load coverages into memory but reads off disk
                    GridCoverageWriter writer = format.getWriter(tmpFile);

                    file.delete();
                    writer.write((GridCoverage) transformed, null);
                    // There may be several files created if dest format is
                    // world+image
                    // so move all files in the tmpDir
                    for (File f : tmpDir.listFiles()) {
                        File dest = new File(file.getParentFile(), f.getName());
                        FileUtils.moveFile(f, dest);
                    }
                    } finally {
                        FileUtils.delete(tmpDir);
                    }
                } else {
                    GridCoverageWriter writer = format.getWriter(file);
                    writer.write((GridCoverage) transformed, null);
                }

                LOG.debug("Finished reprojecting output");
                return null;
            }
        };

        CoverageTransformation.perform(sourceFile, transformation);
    }

    private void convertFormat(String baseFilename, InputStream in, File file,
            WcsReaderRequest request, BoundWcsRequest requestNegotiatedFormat)
            throws IOException, AssertionError, FileNotFoundException {
        if (!request.format.equals(requestNegotiatedFormat.format)) {
            // the server did not support the desired format so we have to
            // reproject
            if (request.format.equalsIgnoreCase("geotiff")) {
                File tmpDir = FileUtils.createTempDirectory();
                try {
                    File tmpFile = new File(tmpDir, baseFilename + "."+ request.fileExtension());
                    writeWorldImage(request, tmpFile, in);
                    convertToGeotiff(tmpFile, file);
                } finally {
                    FileUtils.delete(tmpDir);
                }
            } else {
                BufferedImage image = ImageIO.read(in);
                ImageIO.write(image, request.format, file);

                String baseFilePath = file.getPath().substring(0,
                        file.getPath().lastIndexOf('.'));
                createWorldFile(request, baseFilePath);
                createPrjFile(request.responseCRS, baseFilePath);
            }
        } else {
            if (Formats.embeddedCrsFormats.contains(request.format)) {
                writeToFile(file, in);
            } else {
                writeWorldImage(request, file, in);
            }
        }
    }

    private void convertToGeotiff(File tmpFile, final File file)
            throws IOException {

        CoverageTransformation<Object> transformation = new CoverageTransformation<Object>() {

            @Override
            public Object transform(GridCoverage coverage) throws IOException {
                GeoTiffWriter writer = new GeoTiffWriter(file);

                GeneralParameterValue[] params = new GeneralParameterValue[0];
                writer.write(coverage, params);
                return null;
            }
        };

        CoverageTransformation.perform(tmpFile, transformation);
    }

    private void writeToFile(File file, InputStream in) throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        try {
            ReadableByteChannel channel = Channels.newChannel(in);
            fout.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            fout.flush();
            if (file.length() < 8000) {
                if (file.length() == 0) {
                    throw new ExtractorException(
                            "GetCoverageRequests returned no data, see administrator");
                }
                String text = null;
                try {
                    text = org.apache.commons.io.FileUtils.readFileToString(
                            file, "UTF-8");
                } catch (Throwable e) {
                    // ignore. I assume an image or something that this can't
                    // read
                }
                if (text != null && text.contains("<ServiceException>"))
                    throw new ExtractorException(text);
            }
        } finally {
            fout.close();
        }
    }

    private void writeWorldImage(WcsReaderRequest request, File file,
            InputStream in) throws IOException {
        writeToFile(file, in);

        String baseFilePath = file.getPath().substring(0,
                file.getPath().lastIndexOf('.'));
        createWorldFile(request, baseFilePath);
        createPrjFile(request.responseCRS, baseFilePath);
    }

    private BoundWcsRequest negotiateFormat(BoundWcsRequest request)
            throws IOException {
        final Set<String> formats = request.getSupportedFormats();
        if (formats.isEmpty())
            return request;
        if (!formats.contains(request.format)) {
            for (String format : Formats.preferredFormats) {
                if (formats.contains(format))
                    return request.withFormat(format);
            }

            // Note: If none of the preferred formats are offered by server
            // then we download a format and see if we can convert it to the
            // desired format
            return request.withFormat(formats.iterator().next());
        }
        return request;
    }

    private BoundWcsRequest negotiateResponseCRS(BoundWcsRequest request)
 throws IOException {

        Set<String> crss = request.getSupportedResponseCRSs();
        // Hack mostly for pigma. It will work so long as backing servers
        // are Geoservers
        if (crss.isEmpty() && request.getNativeCRSs().isEmpty())
            return request;
        if (!crss.contains(request.getResponseEpsgCode())) {
            String newCrs = "EPSG:4326";
            if (request.getNativeCRSs().isEmpty()) {
                Iterator<String> crsIter = crss.iterator();
                if (!crss.contains(newCrs) && crsIter.hasNext()) {
                    newCrs = crsIter.next();
                } else {

                }
            } else {
                newCrs = request.getNativeCRSs().iterator().next();
            }
            return request.withCRS(newCrs);
        }
        return request;
    }

    private BoundWcsRequest negotiateRequestCRS(BoundWcsRequest request)
            throws IOException, FactoryException {
        Set<String> crss = request.getSupportedRequestCRSs();
        String requestCrs = "EPSG:"
                + CRS.lookupEpsgCode(
                        request.requestBbox.getCoordinateReferenceSystem(),
                        true);
        if (crss.isEmpty())
            return request;
        if (!crss.contains(requestCrs)) {
            ReferencedEnvelope newBBox = null;
            for (String crs : crss) {
                try {
                    newBBox = request.requestBbox.transform(CRS.decode(crs),
                            true, 10);
                } catch (Exception e) {
                    // try next crs
                }
            }
            if (newBBox != null && !crss.isEmpty()) {
                return request.withRequestBBox(newBBox);
            }
        }
        return request;
    }

    private void createPrjFile(CoordinateReferenceSystem crs,
            String baseFilename) throws FileNotFoundException {
        LOG.debug("Writing PRJ file: " + baseFilename + ".prj");

        PrintWriter out = new PrintWriter(new FileOutputStream(baseFilename
                + ".prj"));
        try {
            out.write(crs.toWKT());
            out.flush();
        } finally {
            out.close();
        }
    }

    /**
     * This method is responsible fro creating a world file to georeference an
     * image given the image bounding box and the image geometry. The name of
     * the file is composed by the name of the image file with a proper
     * extension, depending on the format (see WorldImageFormat). The projection
     * is in the world file.
     * 
     * @param baseFile
     *            Basename and path for this image.
     * @throws IOException
     *             In case we cannot create the world file.
     * @throws TransformException
     */
    private void createWorldFile(final WcsReaderRequest request,
            final String baseFile) throws IOException {
        int width = (int) (request.requestBbox.getWidth() / request.groundResolutionX);
        int height = (int) (request.requestBbox.getWidth() / request.groundResolutionX);
        ReferencedEnvelope transformedBBox;
        try {
            transformedBBox = new ReferencedEnvelope(request.requestBbox, WGS84)
                    .transform(request.responseCRS, true, 10);
        } catch (Exception e) {
            throw new ExtractorException(e);
        }
        Rectangle imageSize = new Rectangle(width, height);
        AffineTransform transform = RendererUtilities.worldToScreenTransform(
                transformedBBox, imageSize);

        // /////////////////////////////////////////////////////////////////////
        //
        // CRS information
        //
        // ////////////////////////////////////////////////////////////////////
        // final AffineTransform gridToWorld = (AffineTransform)
        // gc.getGridGeometry ().getGridToCRS ();
        final boolean lonFirst = (XAffineTransform.getSwapXY(transform) != -1);

        // /////////////////////////////////////////////////////////////////////
        //
        // World File values
        // It is worthwhile to note that we have to keep into account the fact
        // that the axis could be swapped (LAT,lon) therefore when getting
        // xPixSize and yPixSize we need to look for it a the right place
        // inside the grid to world transform.
        //
        // ////////////////////////////////////////////////////////////////////
        final double xPixelSize = (lonFirst) ? transform.getScaleX()
                : transform.getShearY();
        final double rotation1 = (lonFirst) ? transform.getShearX() : transform
                .getScaleX();
        final double rotation2 = (lonFirst) ? transform.getShearY() : transform
                .getScaleY();
        final double yPixelSize = (lonFirst) ? transform.getScaleY()
                : transform.getShearX();
        final double xLoc = transform.getTranslateX();
        final double yLoc = transform.getTranslateY();

        // /////////////////////////////////////////////////////////////////////
        //
        // writing world file
        //
        // ////////////////////////////////////////////////////////////////////
        final StringBuffer buff = new StringBuffer(baseFile);
        // looking for another extension
        buff.append(".wld");
        final File worldFile = new File(buff.toString());

        LOG.debug("Writing world file: " + worldFile);

        final PrintWriter out = new PrintWriter(new FileOutputStream(worldFile));
        try {
            out.println(xPixelSize);
            out.println(rotation1);
            out.println(rotation2);
            out.println(yPixelSize);
            out.println(xLoc);
            out.println(yLoc);
            out.flush();
        } finally {
            out.close();
        }
    }

    /* ------------------- Shared support methods ------------------- */

    /**
     * given the template, finds the parameter in the array that has the same
     * name. Or returns empty collection
     */
    private Collection<? extends GeneralParameterValue> find(
            GeneralParameterValue template, GeneralParameterValue[] parameters) {
        String codeToFind = template.getDescriptor().getName().getCode();
        for (GeneralParameterValue generalParameterValue : parameters) {
            String currentCode = generalParameterValue.getDescriptor()
                    .getName().getCode();
            if (currentCode.equals(codeToFind)) {
                return Collections.singleton(generalParameterValue);
            }
        }

        if (template.getDescriptor().getMinimumOccurs() < 1
                && template instanceof Parameter<?>) {
            Parameter<?> param = (Parameter<?>) template;
            if (param.getValue() != null) {
                return Collections.singleton(param);
            }
        }
        return Collections.emptyList();
    }

    /*-------------------------  Unsupported methods  --------------------*/
    @Override
    public String[] listSubNames() {
        throw new UnsupportedOperationException(
                "Does not need to be implemented for geOrchestra");
    }

    @Override
    public int getGridCoverageCount() {
        throw new UnsupportedOperationException(
                "Does not need to be implemented for geOrchestra");
    }

    @Override
    public ServiceInfo getInfo() {
        throw new UnsupportedOperationException(
                "Does not need to be implemented for geOrchestra");
    }

    @Override
    public String getCurrentSubname() {
        throw new UnsupportedOperationException(
                "Does not need to be implemented for geOrchestra");
    }

}
