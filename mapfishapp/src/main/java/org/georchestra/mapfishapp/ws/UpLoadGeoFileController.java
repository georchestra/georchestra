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

package org.georchestra.mapfishapp.ws;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.mapfishapp.ws.upload.FileDescriptor;
import org.georchestra.mapfishapp.ws.upload.UnsupportedGeofileFormatException;
import org.georchestra.mapfishapp.ws.upload.UpLoadFileManagement;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.base.Preconditions;

/**
 * This controller is responsible for uploading a geofiles and transform their
 * features to json syntax.
 *
 * <pre>
 * In case of success, returns
 *              {"success":"true","geojson":"{"type":"FeatureCollection","features":[...]}"}
 * with
 *              Content-Type: text/html (cf introductory paragraph regarding file uploads
 *      in http://docs.sencha.com/ext-js/3-4/?print=/api/Ext.form.BasicForm)
 * </pre>
 *
 * One of the following implementation can be set:
 *
 * <p>
 * Geotools Implementation </br>
 * expects the following files <lu>
 * <li>ESRI Shape in zip: shp, shx, prj file are expected</li>
 * <li>kml</li>
 * <li>gml</li> </lu>
 * </p>
 *
 *
 * @author Mauricio Pazos
 *
 */
@Controller
public final class UpLoadGeoFileController implements HandlerExceptionResolver {

    private static final Log LOG = LogFactory.getLog(UpLoadGeoFileController.class.getPackage().getName());

    private static final int MEGABYTE = 1048576;

    @Autowired
    private GeorchestraConfiguration georConfig;

    public void init() {
        if ((georConfig != null) && (georConfig.activated())) {
            File tmpDir = new File(this.docTempDir, "/geoFileUploadsCache");
            if (!tmpDir.exists()) {
                try {
                    FileUtils.forceMkdir(tmpDir);
                } catch (Exception e) {
                    LOG.error("Unable to create default upload directory, please check configuration", e);
                    // Using default (webapp-provided in ws-servlet.xml)
                    return;
                }
            }
            tempDirectory = tmpDir;
        }
    }

    /**
     * Status of the upload process
     *
     * @author Mauricio Pazos
     *
     */
    public enum Status {
        ok {
            @Override
            public String getMessage(final String jsonFeatures) {
                return "{\"success\": \"true\", \"geojson\": }";
            }

        },
        outOfMemoryError {

            @Override
            public String getMessage(final String detail) {
                return "{\"success\":false, \"error\":\"fileupload_error_outOfMemory\", \"msg\": \"out of memory - "
                        + detail + "\"}";
            }
        },
        ioError {
            @Override
            public String getMessage(final String detail) {
                return "{\"success\":false, \"error\":\"fileupload_error_ioError\", \"msg\": \"" + detail + "\"}";
            }

        },
        unsupportedFormat {
            @Override
            public String getMessage(final String detail) {
                return "{\"success\":false, \"error\":\"fileupload_error_unsupportedFormat\", \"msg\": \"unsupported file type\"}";
            }
        },
        unsupportedProtocol {
            @Override
            public String getMessage(final String detail) {
                return "{\"success\":false, \"error\":\"filedownload_error_unsupportedProtocol\", \"msg\": \"unsupported protocol\"}";
            }
        },
        projectionError {
            @Override
            public String getMessage(final String detail) {
                return "{\"success\":false, \"error\":\"fileupload_error_projectionError\", \"msg\": \"Error occured while parsing coordinates: "
                        + detail + "\"}";
            }
        },
        unsupportedTargetCRS {
            @Override
            public String getMessage(final String detail) {
                return "{\"success\":false, \"error\":\"fileupload_error_projectionError\", \"msg\": \"Unsupported target Coordinate Reference System: "
                        + detail + "\"}";
            }
        },
        sizeError {
            @Override
            public String getMessage(String detail) {
                return "{\"success\": \"false\", \"error\":\"fileupload_error_sizeError\", \"msg\": \"file exceeds the limit. "
                        + detail + "\"}";
            }
        },
        multiplefiles {
            @Override
            public String getMessage(final String detail) {
                return "{\"success\": \"false\", \"error\":\"fileupload_error_multipleFiles\", \"msg\": \"multiple files\"}";
            }
        },
        incompleteSHP {
            @Override
            public String getMessage(final String detail) {
                return "{\"success\": \"false\", \"error\":\"fileupload_error_incompleteSHP\", \"msg\": \"incomplete shapefile\"}";
            }
        },
        unzipError {
            @Override
            public String getMessage(final String detail) {
                return "{\"success\": \"false\", \"error\":\"fileupload_error_zipfile\", \"msg\": \"Error reading zip file\"}";
            }
        },
        ready {
            @Override
            public String getMessage(final String detail) {
                throw new UnsupportedOperationException("no message is associated to this status");
            }
        };

        /**
         * Returns the message associated to this status.
         *
         * @return JSON string
         */
        public abstract String getMessage(final String detail);

        public String getMessage() {
            return getMessage("");
        };

    }

    // constants configured in the ws-servlet.xml file
    private String responseCharset;
    private File tempDirectory;
    private String docTempDir = "/tmp";

    // for test purposes only
    boolean allowFileProtocol;

    /**
     * The current file that was upload an is in processing
     *
     * @return {@link FileDescriptor}
     */
    private FileDescriptor createFileDescriptor(final String fileName) {

        return new FileDescriptor(fileName);
    }

    public void setTempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public void setDocTempDir(String docTempDir) {
        this.docTempDir = docTempDir;
    }

    public void setResponseCharset(String responseCharset) {
        this.responseCharset = responseCharset;
    }

    /**
     * Returns the set of file formats which this service can manage.
     *
     * <pre>
     * URL example
     *
     * http://localhost:8080/mapfishapp/ws/formats
     * </pre>
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/formats", method = RequestMethod.GET)
    public void formats(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UpLoadFileManagement fileManagement = UpLoadFileManagement.create();
        JSONArray formatList = fileManagement.getFormatListAsJSON();

        response.setCharacterEncoding(responseCharset);
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        try {
            out.println(formatList.toString(4));
        } catch (JSONException e) {
            out.println("[]");
        } finally {
            out.close();
        }
    }

    /**
     * Load the file provided in the request. The content of this file is returned
     * as a json object. If an CRS is provided the resultant features will be
     * projected to that CRS before.
     * <p>
     * The file is maintained in a temporal store that will be cleaned when the
     * response has be done.
     * </p>
     */
    @RequestMapping(value = "/togeojson", //
            method = { RequestMethod.POST, RequestMethod.GET }, //
            produces = "application/json")
    public void toGeoJsonFromURL(//
            HttpServletResponse response, //
            @RequestParam(name = "url", required = true) URL url, //
            @RequestParam(name = "srs", required = false) String targetSRS, @RequestHeader HttpHeaders requestHeaders)
            throws Exception {

        LOG.debug(String.format("toGeoJsonFromURL(%s, %s)", url, targetSRS));
        if (!validateRemoteURLProtocol(url)) {
            writeErrorResponse(response, Status.unsupportedProtocol, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        final File workDirectory = makeDirectoryForRequest(this.tempDirectory);
        try {
            Optional<FileDescriptor> downloadedFile = downloadURL(url, workDirectory);
            if (downloadedFile.isPresent()) {
                FileDescriptor fileDescriptor = downloadedFile.get();
                UpLoadFileManagement fileManagement = UpLoadFileManagement.create();
                fileManagement.setWorkDirectory(workDirectory);
                fileManagement.setFileDescriptor(fileDescriptor);
                transformAndSend(fileManagement, targetSRS, response, resolveResponseContentType(requestHeaders));
            } else {
                writeErrorResponse(response, Status.unsupportedFormat);
                return;
            }
        } finally {
            cleanTemporalDirectory(workDirectory);
        }
    }

    private boolean validateRemoteURLProtocol(URL url) {
        final String protocol = url.getProtocol();
        if ("file".equals(protocol) && this.allowFileProtocol) {
            LOG.debug("Loading from file " + url);
            return true;
        }
        return "http".equals(protocol) || "https".equals(protocol);
    }

    /**
     * Load the file provided in the request body. The content of this file is
     * returned as a json object. If an CRS is provided the resultant features will
     * be projected to that CRS before.
     * <p>
     * The file is maintained in a temporal store that will be cleaned when the
     * response has be done.
     * </p>
     */
    @RequestMapping(value = "/togeojson", //
            method = RequestMethod.POST, //
            params = { "!url" }, //
            produces = "application/json")
    public void toGeoJsonFromMultipart(//
            HttpServletResponse response, //
            @RequestParam(name = "geofile", required = true) MultipartFile geofile,
            @RequestParam(name = "srs", required = false) String targetSRS, //
            @RequestHeader HttpHeaders requestHeaders) throws Exception {

        LOG.debug(String.format("toGeoJsonFromMultipart(%s, %s)", geofile.getOriginalFilename(), targetSRS));

        if (geofile.getOriginalFilename().isEmpty()) {
            throw new IOException("a file is expected");
        }

        final File workDirectory = makeDirectoryForRequest(this.tempDirectory);
        try {
            UpLoadFileManagement fileManagement = UpLoadFileManagement.create();
            fileManagement.setWorkDirectory(workDirectory);
            FileDescriptor currentFile = createFileDescriptor(geofile.getOriginalFilename());
            // validates the format
            if (!currentFile.isValidFormat()) {
                writeErrorResponse(response, Status.unsupportedFormat);
                return;
            }
            fileManagement.setFileDescriptor(currentFile);
            fileManagement.save(geofile);

            MediaType forceResponseType = resolveResponseContentType(requestHeaders);
            transformAndSend(fileManagement, targetSRS, response, forceResponseType);
        } finally {
            cleanTemporalDirectory(workDirectory);
        }
    }

    private MediaType resolveResponseContentType(HttpHeaders requestHeaders) {
        // Workaround for the fact that the Ext.js form submission does not allow to
        // specify the Accept request header, and returning application/json when it
        // wasn't requested makes the response not being parsed and throwing a
        // javascript error. It asks for text/html instead.
        List<MediaType> accept = requestHeaders == null ? Collections.emptyList() : requestHeaders.getAccept();
        boolean jsonRequested = accept.stream().anyMatch(APPLICATION_JSON::includes);
        boolean htmlRequested = accept.stream().anyMatch(TEXT_HTML::includes);

        MediaType forceResponseType = htmlRequested && !jsonRequested ? TEXT_HTML : APPLICATION_JSON;
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    String.format("MediaType requested: %s, returning: %s, text/html requested: %s, json requested: %s",
                            accept, forceResponseType, htmlRequested, jsonRequested));
        }
        return forceResponseType;
    }

    private Optional<FileDescriptor> downloadURL(URL toDl, final File workDirectory) throws IOException, Exception {
        FileDescriptor descriptor = null;
        String tempName = UUID.randomUUID().toString();
        File destFile = new File(workDirectory, tempName);
        FileUtils.copyURLToFile(toDl, destFile);

        // naive file detection the downloaded file should either be a ZIP file or an
        // XML derivative at the current state of supported formats (see
        // FileDescriptor.isValidFormat())
        String guessedExtension = guessFileTypeExtension(destFile);
        // if guessedExtension is still blank, give up
        if (!StringUtils.isBlank(guessedExtension)) {
            File file = new File(destFile.getAbsoluteFile() + "." + guessedExtension);
            FileUtils.moveFile(destFile, file);
            FileDescriptor fd = new FileDescriptor(file.getAbsolutePath());
            if (fd.isValidFormat()) {
                fd.savedFile = file;
                fd.listOfFiles.add(file.getAbsolutePath());
                descriptor = fd;
            }
        }
        return Optional.ofNullable(descriptor);
    }

    private void transformAndSend(UpLoadFileManagement fileManagement, @Nullable String targetSRS,
            HttpServletResponse response, MediaType forceResponseType) {

        // processes the uploaded || downloaded file
        Status st = Status.ready;

        // if the uploaded file is a zip file then checks its content
        if (fileManagement.containsZipFile()) {
            try {
                fileManagement.unzip();
            } catch (IOException e) {
                writeErrorResponse(response, Status.unzipError);
                return;
            }
            st = checkGeoFiles(fileManagement);
            if (st != Status.ok) {
                writeErrorResponse(response, st);
                return;
            }
        }

        // create a CRS object from the srs parameter
        @Nullable
        CoordinateReferenceSystem crs;
        try {
            crs = parseCRS(targetSRS);
        } catch (IOException e) {
            writeErrorResponse(response, Status.unsupportedTargetCRS, targetSRS, HttpStatus.BAD_REQUEST.value());
            return;
        }

        // retrieves the feature collection and write the response
        writeOKResponse(response, fileManagement, crs, forceResponseType);
    }

    private @Nullable CoordinateReferenceSystem parseCRS(String targetSRS) throws IOException {
        CoordinateReferenceSystem crs = null;
        try {
            if (!StringUtils.isEmpty(targetSRS)) {
                crs = CRS.decode(targetSRS);
            }
        } catch (FactoryException e) {
            LOG.error(e.getMessage());
            throw new IOException(e);
        }
        return crs;
    }

    // naive file detection heuristics
    // the downloaded file should either be a ZIP file, a json file, or an XML
    // derivative
    // at the current state of supported formats (seeFileDescriptor.isValidFormat())
    private String guessFileTypeExtension(File file) throws Exception {
        try {
            ZipFile zif = new ZipFile(file.getCanonicalPath());
            zif.close();
            return "zip";
        } catch (ZipException e) {
            LOG.debug("provided file is not a ZIP file");
        }
        String xmlExtension = tryGetXmlExtension(file);
        if (!StringUtils.isBlank(xmlExtension)) {
            return xmlExtension;
        }
        if (isGeoJSON(file)) {
            return "geojson";
        }
        return null;
    }

    private boolean isGeoJSON(File file) {
        FeatureJSON fjson = new FeatureJSON();
        try {
            FeatureIterator<SimpleFeature> featureIterator = fjson.streamFeatureCollection(file);
            featureIterator.hasNext();
            featureIterator.close();
            return true;
        } catch (Exception e) {
            LOG.debug("provided file is not a GeoJSON file: " + e.getMessage());
        }
        return false;
    }

    private String tryGetXmlExtension(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            // manages messed-up xml documents, selects the first
            // significant element (i.e. which is not a comment).
            NodeList lst = doc.getChildNodes();
            String rootElement = "";
            int i = 0;
            do {
                if (i < lst.getLength())
                    rootElement = lst.item(i++).getNodeName();
                // last element (unlikely to happen):
                // and we only found #comment elements
                else
                    rootElement = "";
            } while ("#comment".equals(rootElement));

            if ("osm".equals(rootElement)) {
                return "osm";
            } else if ("kml".equals(rootElement)) {
                return "kml";
            } else if ("gpx".equals(rootElement)) {
                return "gpx";
            } else if (rootElement.contains("FeatureCollection")) {
                return "gml";
            }
        } catch (SAXParseException e) {
            LOG.debug("provided file is not an XML file either, giving up.");
        }
        return null;
    }

    /**
     * Write the features in the response object.
     * <p>
     * The output to build is like to
     *
     * "{\"success\": \"true\", \"geojson\":" + jsonFeatures+"}"
     * </p>
     *
     * @param response
     * @param fileManagement
     * @param crs
     * @param forceResponseType
     *
     * @throws Exception
     */
    private void writeOKResponse(final HttpServletResponse response, final UpLoadFileManagement fileManagement,
            final CoordinateReferenceSystem crs, MediaType forceResponseType) {

        final File tmpJsonFile = new File(fileManagement.getWorkDirectory(), "tmpresponse.json");
        try (Writer writer = new FileWriter(tmpJsonFile)) {
            // builds the following response:
            // "{\"success\": \"true\", \"geojson\":" + jsonFeatures+"}");
            writer.write("{\"success\": \"true\", \"geojson\":");

            fileManagement.writeFeatureCollectionAsJSON(writer, crs);

            writer.write("}");
            writer.flush();
        } catch (OutOfMemoryError unlikely) {
            LOG.error(unlikely);
            writeErrorResponse(response, Status.outOfMemoryError, buildOutOfMemoryErrorMessage(),
                    HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        } catch (IOException e) {
            LOG.error(e);
            writeErrorResponse(response, Status.ioError, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (ProjectionException e) {
            LOG.error(e);
            writeErrorResponse(response, Status.projectionError, e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (UnsupportedGeofileFormatException e) {
            LOG.error(e);
            writeErrorResponse(response, Status.unsupportedFormat, e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("RESPONSE: OK");
        }
        response.setCharacterEncoding(responseCharset);
        response.setContentType(forceResponseType.toString());
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            Files.copy(tmpJsonFile.toPath(), response.getOutputStream());
        } catch (IOException streamClosedByClient) {
            LOG.trace(streamClosedByClient);
        }
    }

    /**
     * Writes in the response object the message taking into account the process
     * {@link Status}. Additionally the working directory is removed.
     *
     * @param response
     * @param st
     * @param errorDetail
     * @param responseStatusError
     *
     * @throws IOException
     */
    private void writeErrorResponse(HttpServletResponse response, final Status st, final String errorDetail,
            final int responseStatusError) {
//        response.reset();
        PrintWriter out = null;
        try {
            out = response.getWriter();
            response.setCharacterEncoding(responseCharset);
            response.setContentType("application/json");
            response.setStatus(responseStatusError);

            String statusMsg = StringUtils.isEmpty(errorDetail) ? st.getMessage() : st.getMessage(errorDetail);
            out.println(statusMsg);
            out.flush();

            if (LOG.isDebugEnabled()) {
                LOG.debug("RESPONSE:" + statusMsg);
            }

        } catch (IOException e) {

            LOG.error(e.getMessage());

        } finally {

            if (out != null)
                out.close();
        }
    }

    /**
     * writes the response using the information provided as parameter
     *
     *
     * @param response
     * @param st
     * @param workDirectory
     * @param httpStatusCode
     * @throws IOException
     */
    private void writeErrorResponse(HttpServletResponse response, final Status st) {
        writeErrorResponse(response, st, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private void writeErrorResponse(HttpServletResponse response, final Status st, int httpStatusCode)
            throws IOException {

        writeErrorResponse(response, st, "", httpStatusCode);
    }

    /**
     * Builds the out of memory Error
     *
     * @return out of memory error message
     */
    private String buildOutOfMemoryErrorMessage() {

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        final long max = memoryMXBean.getHeapMemoryUsage().getMax() / MEGABYTE;
        final long used = memoryMXBean.getHeapMemoryUsage().getUsed() / MEGABYTE;
        final String msg = Status.outOfMemoryError
                .getMessage("There is not enough memory. Maximum = " + max + "Mb, Used = " + used + " Mb.");

        LOG.error(msg);

        return msg;
    }

    /**
     * Handles the exception throws by the {@link CommonsMultipartResolver}. A
     * response error will be made if the size of the uploaded file is greater than
     * the configured maximum (see ws-servlet.xml for more details).
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception exception) {

        LOG.error(exception.getMessage());

        if (exception instanceof MaxUploadSizeExceededException) {

            MaxUploadSizeExceededException sizeException = (MaxUploadSizeExceededException) exception;
            long size = sizeException.getMaxUploadSize() / MEGABYTE; // converts
                                                                     // to Mb
            writeErrorResponse(response, Status.sizeError,
                    "The configured maximum size is " + size + " MB. (" + sizeException.getMaxUploadSize() + " bytes)",
                    HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        } else {

            writeErrorResponse(response, Status.ioError, exception.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return null;
    }

    /**
     * Creates a work directory for this request. An
     *
     * @param tempDirectory
     * @return
     * @throws IOException
     */
    private File makeDirectoryForRequest(final File tempDirectory) throws IOException {
        // create a temporal root directory if it doesn't exist
        if (!tempDirectory.exists() && !tempDirectory.mkdirs()) {
            throw new IOException("Unable to create tem directory " + tempDirectory.getAbsolutePath());
        }
        Preconditions.checkState(tempDirectory.isDirectory(), "%s is not a directory", tempDirectory);
        File requestDirectory = new File(tempDirectory, UUID.randomUUID().toString());
        if (!requestDirectory.mkdir()) {
            throw new IOException("cannot create the directory " + requestDirectory);
        }
        LOG.debug("Created request temp directory: " + requestDirectory.getAbsolutePath());
        return requestDirectory;
    }

    private void cleanTemporalDirectory(File workDirectory) throws IOException {
        FileUtils.cleanDirectory(workDirectory);
        LOG.debug("Removing request temp directory " + workDirectory.getAbsolutePath());
        boolean removed = workDirectory.delete();
        if (!removed) {
            LOG.warn("cannot remove temporary directory: " + workDirectory.getAbsolutePath());
        }
    }

    /**
     * Checks the content of zip file.
     *
     * @param fileManagement
     * @return
     */
    private Status checkGeoFiles(UpLoadFileManagement fileManagement) {
        // a zip file is unzipped to a temporary place and *.SHP and *.shp files
        // are looked for at the root of the archive.
        // If several SHP files are found, the error message is "multiple files"
        if (!fileManagement.checkGeoFileExtension()) {
            return Status.unsupportedFormat;
        }

        if (!fileManagement.checkSingleGeoFile()) {
            return Status.multiplefiles;
        }

        if (fileManagement.isSHP()) {
            // if filename.shp is found, it is assumed that filename.shx and
            // filename.prj are also present (the DBF is not mandatory). If not:
            // msg = "incomplete shapefile"
            if (!fileManagement.checkSHPCompletness()) {
                return Status.incompleteSHP;
            }
        }

        return Status.ok;
    }
}
