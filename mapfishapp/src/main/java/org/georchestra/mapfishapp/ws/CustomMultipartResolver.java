/**
 * 
 */
package org.georchestra.mapfishapp.ws;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * This class redefine the exception handler in order provides more feedback to the user.
 *  
 * @author Mauricio Pazos
 *
 */
public class CustomMultipartResolver {
//extends CommonsMultipartResolver{
//	
//	
//	public RobustFileUploadSupport() {
//		super();
//	}
//
//	/**
//	 * Constructor for standalone usage. Determines the servlet container's
//	 * temporary directory via the given ServletContext.
//	 * @param servletContext the ServletContext to use
//	 */
//	public RobustFileUploadSupport(ServletContext servletContext) {
//		this();
//		setServletContext(servletContext);
//	}
//	
//	protected MultipartParsingResult parseRequest(HttpServletRequest request) throws MultipartException {
//		String encoding = determineEncoding(request);
//		FileUpload fileUpload = prepareFileUpload(encoding);
//		try {
//			List fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
//			return parseFileItems(fileItems, encoding);
//		}
//		catch (FileUploadBase.SizeLimitExceededException ex) {
//			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
//		}
//		catch (FileUploadException ex) {
//			throw new MultipartException("Could not parse multipart servlet request", ex);
//		}
//	}
//
}
