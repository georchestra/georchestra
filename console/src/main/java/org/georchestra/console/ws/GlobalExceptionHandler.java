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
import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Log LOG = LogFactory.getLog(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletResponse response){

        // Set HTTP response code according to exception type
        if(e instanceof NameNotFoundException){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else if(e instanceof AccessDeniedException){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // Generate stack trace as String
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);

        // Log error and stack trace
        LOG.error(sw.getBuffer().toString());

        // Send error message to browser
        PrintWriter output = null;
        try {
            output = response.getWriter();
            output.write(e.toString());
            output.close();
        } catch (IOException ex) {}

    }

}
