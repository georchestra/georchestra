package org.georchestra.console.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ws.backoffice.roles.RolesController;
import org.georchestra.console.ws.backoffice.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Log LOG = LogFactory.getLog(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    public String handleAccessDenied(Exception e, HttpServletResponse response) throws IOException {
        LOG.error(e.getMessage());
        ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                HttpServletResponse.SC_FORBIDDEN);
        throw new IOException(e);
    }

    @ExceptionHandler(NameNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleNotFound(Exception e, HttpServletResponse response) throws IOException {
        LOG.error(e.getMessage());
        ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                HttpServletResponse.SC_NOT_FOUND);
        throw new IOException(e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e, HttpServletResponse response) throws IOException {
        LOG.error(e.getMessage());
        ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        throw new IOException(e);
    }

}
