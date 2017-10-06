package org.georchestra.analytics;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class GeorCustomController {
    @Autowired
    private GeorchestraConfiguration georConfig;
    /**
     * JS configuration entry point.
     *
     * This end point is served by commons as it is also used in extractorapp
     */
    @RequestMapping("/app/js/GEOR_custom.js")
    public void getGeorCustom(HttpServletRequest request, HttpServletResponse response) throws Exception {
        this.georConfig.getGeorCustom(request, response);
    }

}
