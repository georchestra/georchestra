package org.georchestra.mapfishapp.ws;

/**
 * This service handles the storage and the loading of a KML file on a temporary directory.
 * 
 * @author yoann buch  - yoann.buch@gmail.com
 *
 */

public class KMLDocService extends A_DocService {

    public static final String FILE_EXTENSION = ".kml";
    public static final String MIME_TYPE = "application/vnd.google-earth.kml+xml";

    public KMLDocService(final String tempDir) {
        super(FILE_EXTENSION, MIME_TYPE, tempDir);
    }

    /**
     * Called before saving the content
     * @throws DocServiceException
     */
    @Override
    protected void preSave() throws DocServiceException {

    }

    /**
     * Called right after the loading of the file content 
     * @throws DocServiceException
     */
    @Override
    protected void postLoad() throws DocServiceException {

    }

}
