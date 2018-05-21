/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.mapfishapp.ws.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.json.JSONArray;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class is responsible to maintain the uploaded file. It includes the
 * method to save, unzip, and check the geofiles.
 *
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManagement {

    private static final Log LOG = LogFactory.getLog(UpLoadFileManagement.class.getPackage().getName());

    public enum Implementation {
        geotools, ogr
    };

    private static List<String>          VALID_EXTENSIONS;
    static {

        VALID_EXTENSIONS = new ArrayList<String>();
        // SHP
        VALID_EXTENSIONS.add("SHP");
        VALID_EXTENSIONS.add("DBF");
        VALID_EXTENSIONS.add("PRJ");
        VALID_EXTENSIONS.add("SHX");
        VALID_EXTENSIONS.add("QIX");

        // TAB
        VALID_EXTENSIONS.add("TAB");
        VALID_EXTENSIONS.add("ID");
        VALID_EXTENSIONS.add("MAP");
        VALID_EXTENSIONS.add("DAT");

        // MIF
        VALID_EXTENSIONS.add("MIF");
        VALID_EXTENSIONS.add("MID");

        VALID_EXTENSIONS.add("GML");

        VALID_EXTENSIONS.add("KML");

        VALID_EXTENSIONS.add("GPX");

        // OSM
        VALID_EXTENSIONS.add("OSM");

    }

    private FileDescriptor               fileDescriptor;

    private String                       workDirectory;

    private AbstractFeatureGeoFileReader reader;

    private UpLoadFileManagement() {
        // use the method factory
    }

    /**
     * Creates an instance of {@link UpLoadFileManagement} which is set to use
     * the implementation specified as parameter.
     *
     * @param impl
     *            implementation
     * @throws IOException
     */
    public static UpLoadFileManagement create(Implementation impl)
            throws IOException {

        UpLoadFileManagement manager = new UpLoadFileManagement();
        if (Implementation.geotools == impl) {
            manager.reader = new AbstractFeatureGeoFileReader(
                    new GeotoolsFeatureReader());
        } else {
            manager.reader = new AbstractFeatureGeoFileReader(
                    new OGRFeatureReader());
        }
        return manager;
    }

    /**
     * Creates an instance of {@link UpLoadFileManagement} which is set to use
     * the OGR implementation if only if gdal/ogr is installed in the system
     *
     * @return new instance of {@link UpLoadFileManagement}
     */
    public static UpLoadFileManagement create() {

        UpLoadFileManagement manager = new UpLoadFileManagement();
        manager.reader = new AbstractFeatureGeoFileReader();

        return manager;
    }

    public void unzip() throws IOException {

        ZipFile zipFile = new ZipFile(
                fileDescriptor.savedFile.getAbsolutePath());

        // creates the directories and extracts the geofiles
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {

            ZipEntry entry = entries.nextElement();
            String path = workDirectory + File.separator + entry.getName();

            String extension = FilenameUtils.getExtension(path).toUpperCase();
            if (VALID_EXTENSIONS.contains(extension)) {
                File outFile = new File(path);
                extractFile(zipFile, entry, outFile);
            } else {
                makeDirectory(path);
            }
        }
        zipFile.close();
    }

    /**
     * Creates the directory structure taking into account the directory path
     *
     * @param path
     * @throws IOException
     */
    private void makeDirectory(String path) throws IOException {

        File newDirectory = new File(path);
        if (!newDirectory.exists()) {
            makeDirectory(newDirectory.getParent());
            newDirectory.mkdir();
        }
    }

    /**
     * Extract the file entry from the zip file.
     *
     * @param zipFile
     * @param entry
     * @param outFile
     * @throws IOException
     */
    private void extractFile(final ZipFile zipFile, final ZipEntry entry, final File outFile) throws IOException {

        InputStream is = zipFile.getInputStream(entry);
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));

        byte[] buffer = new byte[1024];
        int len;

        while ((len = is.read(buffer)) >= 0) {
            os.write(buffer, 0, len);
        }

        is.close();
        os.close();

        // save the extension in the content extensions list
        String extension = FilenameUtils.getExtension(outFile.getName())
                .toUpperCase();
        this.fileDescriptor.listOfExtensions.add(extension);
        this.fileDescriptor.listOfFiles.add(outFile.getAbsolutePath());

    }

    /**
     * Saves the upload file in the temporal directory.
     *
     *
     * @param uploadFile
     * @param downloadDirectory
     * @return {@link File} the saved file
     *
     * @throws IOException
     *
     */
    public File save(MultipartFile uploadFile) throws IOException {

        try {
            // transfers the uploaded file to the work directory
            final String originalFileName = uploadFile.getOriginalFilename();
            File outFile = new File(this.workDirectory + "/" + originalFileName);
            uploadFile.transferTo(outFile);

            this.fileDescriptor.savedFile = outFile;

            // if it is a geofile then the descriptor is updated (zip file will
            // be processed later)
            String extension = FilenameUtils.getExtension(originalFileName)
                    .toUpperCase();
            if (VALID_EXTENSIONS.contains(extension)) {
                this.fileDescriptor.listOfExtensions.add(extension);
                this.fileDescriptor.listOfFiles.add(outFile.getAbsolutePath());
            }
            return outFile;

        } catch (IOException e) {
            LOG.fatal(e.getMessage());
            throw e;
        }
    }

    public boolean containsZipFile() {
        return this.fileDescriptor.isZipFile();
    }

    /**
     * Checks if the work directory contains files with valid extensions.
     *
     * @return true if the extensions are OK
     */
    public boolean checkGeoFileExtension() {

        for (String fileName : this.fileDescriptor.listOfFiles) {

            String ext = FilenameUtils.getExtension(fileName).toUpperCase();

            if (!VALID_EXTENSIONS.contains(ext)) {
                return false;
            }

        }
        return true;

    }

    /**
     * a zip file is unzipped to a temporary place and *.shp, *.mid, *.tab files
     * are looked for at the root of the archive. If several SHP or several MIF
     * or several TAB files are found, the error message is "multiple files"
     *
     * @return true if the work directory contain only a one shp or mid or tab
     */
    public boolean checkSingleGeoFile() {

        List<String> foundExtensions = new ArrayList<String>();
        for (String fileName : this.fileDescriptor.listOfFiles) {

            String ext = FilenameUtils.getExtension(fileName).toUpperCase();

            if (foundExtensions.contains(ext)) {
                return false;
            } else {
                foundExtensions.add(ext);
            }
        }
        return true;
    }

    public boolean isMIF() {
        return this.fileDescriptor.listOfExtensions.contains("MIF");
    }

    public boolean isSHP() {
        return this.fileDescriptor.listOfExtensions.contains("SHP");
    }

    public boolean isTAB() {
        return this.fileDescriptor.listOfExtensions.contains("TAB");
    }

    /**
     * if filename.mif is found, it is assumed that filename.mid exists too.
     *
     * @return false if fid file doesn't exist.
     */
    public boolean checkMIFCompletness() {

        return      this.fileDescriptor.listOfExtensions.contains("MIF")
                &&  this.fileDescriptor.listOfExtensions.contains("MID");
    }

    /**
     * if filename.shp is found, it is assumed that filename.shx and
     * filename.prj are also present (the DBF is not mandatory).
     *
     * @return true if shx and prj are found
     */
    public boolean checkSHPCompletness() {
        return      this.fileDescriptor.listOfExtensions.contains("SHP")
                &&  this.fileDescriptor.listOfExtensions.contains("SHX")
                &&  this.fileDescriptor.listOfExtensions.contains("PRJ");
    }

    public boolean checkTABCompletness() {
        return      this.fileDescriptor.listOfExtensions.contains("TAB")
                &&  this.fileDescriptor.listOfExtensions.contains("ID")
                &&  this.fileDescriptor.listOfExtensions.contains("MAP")
                &&  this.fileDescriptor.listOfExtensions.contains("DAT");
    }

    public FileFormat[] getFormatList() {

        return this.reader.getFormatList();
    }

    /**
     * Create a feature collection with based on the json syntax. The features
     * are read from the work directory. The could have one of the accepted
     * format:
     *
     * <ul>
     * <li>zip: shp, mif, tab</li>
     * <li>kml</li>
     * <li>gpx</li>
     * <li>gml</li>
     * </ul>
     *
     * <pre>
     * the file SRS is obtained :
     * 	from the prj file for shapefiles
     * 	directly from the mif/mid files
     * 	directly from the GML features
     * 	assumed EPSG:4326 for all kml files
     * 	assumed EPSG:4326 for all gpx files
     * </pre>
     *
     * @param writer where the featrue must be written.
     * @param crs if it is not null the features should be transformed to this
     *            {@link CoordinateReferenceSystem}, in other case they won't
     *            transformed.
     * @throws IOException
     */
    public void writeFeatureCollectionAsJSON(Writer writer, final CoordinateReferenceSystem crs) throws Exception {

        if (LOG.isDebugEnabled()) {
            LOG.debug("CRS to reproject:" + crs);
        }

        // retrieves the feature collection from the filesystem and writes the
        // underlying JSON string
        String fileName = searchGeoFile();
        assert fileName != null;

        SimpleFeatureIterator featuresIterator = null;
        try {
            SimpleFeatureCollection featureCollection = this.reader.getFeatureCollection(new File(fileName),
                            								this.fileDescriptor.geoFileType, crs);
            if (featureCollection == null) {
                return;
            }
            // TODO FeatureJSON2 is a workaround to solve the crs bug
            FeatureJSON fjson = new FeatureJSON2(new GeometryJSON(18));
            SimpleFeatureType schema = featureCollection.getSchema();

            fjson.setFeatureType(schema);
            fjson.setEncodeFeatureCollectionCRS(true);

            fjson.writeFeatureCollection(featureCollection, writer);

        } catch (ProjectionException e) {
            LOG.error("Failed reading " + fileName + ": " + e.getMessage());
            throw e;
        }
        catch (Exception e) {

            final String message = "Failed reading " + FilenameUtils.getName(fileName) + ".  "
                    + e.getMessage();
            LOG.error(message);
            throw new IOException(message, e);

        } finally {
            if (featuresIterator != null) featuresIterator.close();
        }
    }

    /**
     * Searches, in the set of file uploaded, a file with a geofile extension.
     * <ul>
     * <li>shp</li>
     * <li>mif</li>
     * <li>tab</li>
     * <li>kml</li>
     * <li>gpx</li>
     * <li>gml</li>
     * </ul>
     *
     * Returns the name of geofile.
     *
     * @return the geofile
     */
    public String searchGeoFile() {

        for (String fileName : this.fileDescriptor.listOfFiles) {

            String ext = FilenameUtils.getExtension(fileName);

            if (FileFormat.contains(ext)) {
                this.fileDescriptor.geoFileType = FileFormat.valueOf(ext.toLowerCase());
                return fileName;
            }
        }
        return null;
    }

    public void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
    }

    public void setFileDescriptor(FileDescriptor geoFile) {
        this.fileDescriptor = geoFile;
    }

    public void setSaveFile(File f) {
        this.fileDescriptor.savedFile = f;
    }

    public JSONArray getFormatListAsJSON() {
        return this.reader.getFormatListAsJSON();
    }

    public void addFileExtension(String extension) {
        this.fileDescriptor.listOfExtensions.add(extension);
    }
    public void addFile(File file) {
        this.fileDescriptor.listOfFiles.add(file.getAbsolutePath());
    }

}
