package org.georchestra.analytics;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {
    
    @RequestMapping(value = "/index.jsp", method=RequestMethod.GET)
    public ModelAndView handleIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return new ModelAndView("index");
    }
    
}