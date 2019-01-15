/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

public class GeorchestraConfiguration {

    protected String globalDatadir;
    protected String contextDataDir;

    protected String contextName;
    protected ServletContext ctx;

    protected Properties applicationSpecificProperties = new Properties();
    protected Properties defaultProperties = new Properties();

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
            try {
                this.applicationSpecificProperties = this.loadCustomPropertiesFile(context);
            } catch (Exception e) {}

            // loads common context property file
            try {
                this.defaultProperties = this.loadPropertiesFile(new File(String.format("%s%s%s",
                        globalDatadir, File.separator, "default.properties")));
            } catch (Exception e) {}

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
        return this.loadPropertiesFile(new File(contextDataDir, key + ".properties"));
    }

    private Properties loadPropertiesFile(File path) throws IOException {
        Properties prop = new Properties();
        FileInputStream fisProp = null;
        try {
            fisProp = new FileInputStream(path);
            InputStreamReader isrProp = new InputStreamReader(fisProp, "UTF8");
            prop.load(isrProp);
        } finally {
            if (fisProp != null) {
                fisProp.close();
            }
        }
        return prop;
    }

    public String getProperty(String key, String defaultValue) {
        if (!this.activated)
            return defaultValue;
        if (this.applicationSpecificProperties.getProperty(key) != null)
            return this.applicationSpecificProperties.getProperty(key);
        if (this.defaultProperties.getProperty(key) != null)
            return this.defaultProperties.getProperty(key);
        return defaultValue;
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public boolean activated() {
        return activated;
    }

    /**
     * This method generate GEOR_custom.js used
     * in Mapfishapp, Extractorapp and Analytics.
     * @param request
     * @param response
     * @throws Exception
     */
    public void getGeorCustom(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setContentType("application/javascript");
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
        InputStream is = request.getSession().getServletContext().getResourceAsStream("/app/js/GEOR_custom.js");
        byte[] content = IOUtils.toByteArray(is);
        response.getOutputStream().write(content);
        return;
    }
}
