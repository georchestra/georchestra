/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;



/**
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public class CustomMultipartResolver extends CommonsMultipartResolver {


	/**
	 * Parse the given servlet request, resolving its multipart elements.
	 * @param request the request to parse
	 * @return the parsing result
	 * @throws MultipartException if multipart resolution failed.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected MultipartParsingResult parseRequest(HttpServletRequest request)  {
		String encoding = determineEncoding(request);
		FileUpload fileUpload = prepareFileUpload(encoding);
		
		
		try {
			List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
			return parseFileItems(fileItems, encoding);
			
		} catch (FileUploadBase.SizeLimitExceededException ex) {
			
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
		} catch (FileUploadException ex) {
			
			throw new MultipartException("Could not parse multipart servlet request", ex);
		}
	}

	
}
