package org.georchestra.console.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Log LOG = LogFactory.getLog(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public void handleAccessDenied(Exception e, HttpServletResponse response) throws Exception {
        LOG.error(e.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        throw e;
    }

    @ExceptionHandler(NameNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleNotFound(Exception e, HttpServletResponse response) throws Exception {
        LOG.error(e.getMessage());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        throw e;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleException(Exception e, HttpServletResponse response) throws Exception {
        LOG.error(e.getMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        throw e;
    }

}
