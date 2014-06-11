package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * Maintains useful file information about the uploaded file.
 *
 * @author Mauricio Pazos
 */
public class FileDescriptor {

    /** file name of upload file */
    public String       originalFileName;

    /** upload file's extension */
    public String       originalFileExt;

    /** upload file */
    public File         savedFile;

    /** the geofile format */
    public FileFormat   geoFileType;

    /** list of the geo file extensions */
    public List<String> listOfExtensions = new ArrayList<String>();

    /** list of files contained in the zip file */
    public List<String> listOfFiles      = new LinkedList<String>();

    public FileDescriptor(final String fileName) {

        assert fileName != null;

        originalFileName = fileName;
        originalFileExt = FilenameUtils.getExtension(fileName);
    }

    public boolean isValidFormat() {
        assert originalFileExt != null;
        return "zip".equalsIgnoreCase(originalFileExt)
                || "kml".equalsIgnoreCase(originalFileExt)
                || "gpx".equalsIgnoreCase(originalFileExt)
                || "gml".equalsIgnoreCase(originalFileExt)
                || "osm".equalsIgnoreCase(originalFileExt);
    }

    public boolean isZipFile() {
        return "zip".equalsIgnoreCase(originalFileExt);
    }

}
