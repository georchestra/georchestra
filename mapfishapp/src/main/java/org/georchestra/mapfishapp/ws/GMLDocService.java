package org.georchestra.mapfishapp.ws;
import org.georchestra.mapfishapp.model.ConnectionPool;

/**
 * This service handles the storage and the loading of a GML file
 * 
 * @author yoann buch  - yoann.buch@gmail.com
 *
 */

public class GMLDocService extends A_DocService {

    public static final String FILE_EXTENSION = ".gml";
    public static final String MIME_TYPE = "application/gml+xml";

    public GMLDocService(final String tempDir, ConnectionPool pgpool) {
        super(FILE_EXTENSION, MIME_TYPE, tempDir, pgpool);
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
