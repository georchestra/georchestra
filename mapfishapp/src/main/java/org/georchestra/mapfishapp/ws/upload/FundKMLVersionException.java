/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;


/**
 * <p>
 * This exception is used to break the SAX parsing process, which explore the document in order to identify its version. 
 * Thus if the version is found in the kml document. 
 * the rest of document is ignored.
 * </p>
 * @author Mauricio Pazos
 *
 */
 final class FundKMLVersionException extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = -7708515311150341495L;

}
