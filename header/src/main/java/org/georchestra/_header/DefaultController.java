/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra._header;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;



@Controller
@RequestMapping("/home")
public class DefaultController {

    @Autowired
    private ServletContext context;

    @Autowired
    private GeorchestraConfiguration georchestraConfiguration;

    @PostConstruct
    public void replaceLogo() throws IOException {

        // Try to replace logo with logo from datadir
        File imgDirectory = new File(this.context.getRealPath("/img"));
        if(imgDirectory.exists() && imgDirectory.isDirectory() && imgDirectory.canWrite()){
            File target = new File(imgDirectory, "logo.png");
            File source = new File(georchestraConfiguration.getContextDataDir() + "/logo.png");
            if(source.isFile() && source.canRead() && (target.canWrite() || !target.exists()))
                Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

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
