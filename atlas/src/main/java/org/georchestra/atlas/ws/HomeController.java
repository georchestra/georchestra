/**
 *
 */
package org.georchestra.atlas.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
