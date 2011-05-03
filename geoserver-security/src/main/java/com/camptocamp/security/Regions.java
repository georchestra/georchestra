package com.camptocamp.security;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Allows access to geoserver layers configuration
 * @author jesse.eichar@camptocamp.com
 */
@Controller
@RequestMapping("/regions")
public class Regions {

    protected static final Log logger = LogFactory.getLog(Regions.class.getPackage().getName());
    
    static final String PREFIX = "ROLE_GS_ADMIN_";

    @RequestMapping(method = RequestMethod.GET)
    public void regions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roles = request.getHeader("sec-roles");
        String[] names = roles.split(",");
        response.setContentType("application/json; charset=UTF-8");

        PrintWriter writer = response.getWriter();
        try {
            writer.write("{\"regions\": [");
            boolean comma = false;
            for (String name : names) {
                if(name.trim().startsWith(PREFIX)) {
                    if (comma) {
                        writer.write(",");
                    }
                    comma = true;
                    writer.write(String.format("{\"cn\": \"%s\"}", name.trim().substring(PREFIX.length())));
                }
            }
            writer.write("]}");
        } finally {
            writer.close();
        }
    }

}
