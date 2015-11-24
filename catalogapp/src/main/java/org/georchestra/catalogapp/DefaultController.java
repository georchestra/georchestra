package org.georchestra.catalogapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;



@Controller
@RequestMapping("/home")
public class DefaultController {

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView handlePOSTRequest(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        return new ModelAndView("index");
    }
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleGETRequest(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("index");
    }

}
