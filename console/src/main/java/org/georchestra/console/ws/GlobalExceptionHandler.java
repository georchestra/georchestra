package org.georchestra.console.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ws.backoffice.utils.Response;
import org.georchestra.console.ws.backoffice.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Log LOG = LogFactory.getLog(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(NameNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public Response handleNameNotFoundException(NameNotFoundException e) {
        LOG.info(e.getMessage());
        return ResponseUtil.failure(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    public Response handleAccessDeniedException(AccessDeniedException e) {
        LOG.debug(e.getMessage());
        return ResponseUtil.failure(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Response handleException(Exception e) {
        LOG.error(e.getMessage(), e);
        return ResponseUtil.failure(e.getMessage());
    }
}
