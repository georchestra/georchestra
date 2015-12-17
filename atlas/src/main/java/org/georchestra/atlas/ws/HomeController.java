/**
 *
 */
package org.georchestra.atlas.ws;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * Displays the home page, also intercepts some assets.
 *
 *
 * @author Mauricio Pazos, Pierre Mauduit
 *
 */

@Controller
public class HomeController {

    private static final Log LOG = LogFactory.getLog(HomeController.class.getName());

    @Autowired
    private ServletContext context;

    @RequestMapping(value = "/")
    @ResponseBody
    public String root(HttpServletRequest request, HttpServletResponse response) throws IOException {

         return "Hello World!";
    }

 
}
