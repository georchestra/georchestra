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

package org.georchestra.commons.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

@Controller
public class GeorchestraConfiguration implements ServletContextAware {

    protected String globalDatadir;
    protected String contextDataDir;

    protected String contextName;
    protected ServletContext ctx;

    protected Properties applicationSpecificProperties = new Properties();

    public String getContextDataDir() {
        return contextDataDir;
    }

    private boolean activated = false;

    public void init() {}

    public GeorchestraConfiguration(String context) {

        this.contextName = context;
        globalDatadir = System.getProperty("georchestra.datadir");
        if (globalDatadir != null) {
            contextDataDir = new File(String.format("%s%s%s%s", globalDatadir, File.separator, context, File.separator)).getAbsolutePath();

            // Simple check that the path exists
            if (new File(contextDataDir).exists() == false) {
                contextDataDir = null;
                return;
            }
            // loads the application context property file
            FileInputStream propsFis = null;
            try {
                try {
                    // application-context
                    propsFis = new FileInputStream(new File(contextDataDir, context + ".properties"));
                    InputStreamReader isr = new InputStreamReader(propsFis, "UTF8");
                    applicationSpecificProperties.load(isr);
                } finally {
                    if (propsFis != null) {
                        propsFis.close();
                    }
                }
            } catch (Exception e) {
                activated = false;
                return;
            }

            // log4j configuration
            File log4jProperties = new File(contextDataDir, "log4j" + File.separator + "log4j.properties");
            File log4jXml = new File(contextDataDir, "log4j" + File.separator + "log4j.xml");
            String log4jConfigurationFile = null;
            if (log4jProperties.exists()) {
                log4jConfigurationFile = log4jProperties.getAbsolutePath();
                PropertyConfigurator.configure(log4jConfigurationFile);
            } else if (log4jXml.exists()) {
                log4jConfigurationFile = log4jXml.getAbsolutePath();
                DOMConfigurator.configure(log4jConfigurationFile);
            }
            // everything went well
            activated = true;
        }
    }

    /**
     * Loads a property file from the context directory
     *
     * @param key the key file identifier in the context datadir
     * @return Properties a properties map from the given key.properties file
     * @throws IOException
     */
    public Properties loadCustomPropertiesFile(String key) throws IOException {
        Properties prop = new Properties();
        FileInputStream fisProp = null;
        try {
            fisProp = new FileInputStream(new File(contextDataDir, key + ".properties"));
            InputStreamReader isrProp = new InputStreamReader(fisProp, "UTF8");
            prop.load(isrProp);
        } finally {
            if (fisProp != null) {
                fisProp.close();
            }
        }
        return prop;
    }

    public String getProperty(String key) {
        if ((applicationSpecificProperties == null) || (activated == false))
            return null;
        return applicationSpecificProperties.getProperty(key);
    }

    public boolean activated() {
        return activated;
    }

    /**
     * This controller allows to intercept GEOR_custom.js used
     * in Mapfishapp and Extractorapp.
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value= "/app/js/GEOR_custom.js")
    public void getGeorCustom(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setContentType("application/javascript; charset=UTF-8");
        // we could get extra infos from the DB or elsewhere, and
        // add them to the variables provided by the js file :-)
        if (contextDataDir != null) {
            File georCustom = new File(contextDataDir, "js" + File.separator + "GEOR_custom.js");
            if (georCustom.exists()) {
                response.getOutputStream().write(FileUtils.readFileToByteArray(georCustom));
                return;
            }
        }
        // Fallback on the default one provided by the webapp
        InputStream is = this.ctx.getResourceAsStream("/app/js/GEOR_custom.js");
        byte[] content = IOUtils.toByteArray(is);
        response.getOutputStream().write(content);
        return;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.ctx = servletContext;

    }
}
