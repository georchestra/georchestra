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

package org.georchestra.mapfishapp.ws;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

@Controller
public class AddonController implements ServletContextAware {

    @Autowired
    private GeorchestraConfiguration georchestraConfiguration;

    private ServletContext context;

    private static final Log LOG = LogFactory.getLog(AddonController.class.getPackage().getName());

    /**
     * Returns a dynamically-generated array of addons, previously
     * statically stored into the GEOR-custom.js file.
     *
     * The returned format looks like:
     * [{
     *   "id": "magnifier_0", // unique & stable string identifier for this addon instance
     *   "name": "Magnifier",
     *   "title": {
     *       "en": "Aerial imagery magnifier",
     *       "es": "Lupa ortofoto",
     *       "fr": "Loupe orthophoto",
     *       "de": "Ortofoto Lupe"
     *   },
     *   "description": {
     *       "en": "A tool which allows to zoom in an aerial image on a map portion",
     *       "es": "Una herramienta que permite hacer un zoom sobre una parte del mapa ortofoto",
     *       "fr": "Un outil qui permet de zoomer dans une orthophoto sur une portion de la carte",
     *       "de": "Utensil erlaubt Zoom mittels orthofoto auf Kartenbereich"
     *   }
     *  }, ...]
     *
     * The returned JSON is constructed reading the config.json of each addons.
     *
     * @param request
     * @param response
     * @throws Exception
     *
     */
    @RequestMapping(value= "/addons")
    public void getAddons(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json; charset=utf-8");
        JSONArray jsAddons = constructAddonsSpec();

        response.getOutputStream().write(jsAddons.toString(4).getBytes());
    }

    @RequestMapping(value="/addons/**")
    public void getAddonFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String pathInfo = request.getPathInfo().replaceFirst("/addons/", "");
        String path = getMfappAddonPath();

        // Step 1: checks in the webapp directory

        // First check if the file is in the webapp directory
        String officialAddonPath = context.getRealPath("/app/addons/") + File.separator + pathInfo;
        officialAddonPath = FilenameUtils.normalize(officialAddonPath);
        if (officialAddonPath == null) {
            LOG.error("Unexpected file requested (not in datadir): " + pathInfo);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Path officialAddonFile = new File(officialAddonPath).toPath();
        Path officialWebappPath = new File(context.getRealPath("/app/addons/")).toPath();
        // user input security check
        if (! officialAddonFile.toAbsolutePath().toString()
                .startsWith(officialWebappPath.toAbsolutePath().toString())) {
            LOG.error("Unexpected file requested (not in datadir): " + officialAddonFile.toString());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        File officialFile = officialAddonFile.toFile();

        if (officialFile.exists()) {
            dumpFile(response, officialFile);
            return;
        } else if (path == null) {
            // File not found
            LOG.error("requested file does not exist: " + officialFile);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Step 2: checks in the datadir (if available)
        String checkedPath = FilenameUtils.normalize(path + File.separator + pathInfo);
        if (checkedPath == null) {
            LOG.error("Unexpected file requested (not in datadir): " + checkedPath);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Path reqPath = new File(checkedPath).toPath();
        Path datadir = new File(path).toPath();

        // same user input security check
        if (!reqPath.toAbsolutePath().toString().startsWith(datadir.toAbsolutePath().toString())) {
            LOG.error("Unexpected file requested (not in datadir): " + reqPath.toString());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // checks that the file exists
        File actualFile = reqPath.toFile();
        if (! actualFile.exists()) {
            LOG.error("requested file does not exist: " + actualFile);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        dumpFile(response, actualFile);
    }

    /**
     * This actually dumps the file to the user.
     *
     * @param response the HttpServletResponse object
     * @param actualFile the File object to dump
     *
     * @throws IOException
     */
    private void dumpFile(HttpServletResponse response, File actualFile) throws IOException {
        String ext = FilenameUtils.getExtension(actualFile.getAbsolutePath());

        if ("css".equalsIgnoreCase(ext)) {
            response.setContentType("text/css");
        } else if ("js".equalsIgnoreCase(ext)) {
            response.setContentType("application/javascript");
        } else if ("json".equalsIgnoreCase(ext)) {
            response.setContentType("application/json; charset=utf-8");
        } else if ("png".equalsIgnoreCase(ext)) {
            response.setContentType("image/png");
        } else if (("jpg".equalsIgnoreCase(ext)) || ("jpeg".equalsIgnoreCase(ext))) {
            response.setContentType("image/jpeg");
        } else if (("htm".equalsIgnoreCase(ext)) || ("html".equalsIgnoreCase(ext))) {
            response.setContentType("text/html");
        } else {
            response.setContentType("text/plain");
        }
        response.getOutputStream().write(FileUtils.readFileToByteArray(actualFile));
    }

    /**
     * Get the addon path if the georchestra.datadir is available.
     * @return the addon path as a string if datadir is activated, else returns null.
     */
    private String getMfappAddonPath() {
        String path;
        if ((georchestraConfiguration != null) && (georchestraConfiguration.activated())) {
            path = new File(georchestraConfiguration.getContextDataDir(), "addons").getAbsolutePath();
        } else {
            // No georchestra.datadir available, return null
            return null;
        }
        return path;
    }

    /**
     * Get the official path to the addons, i.e. the ones that are provided in the webapp.
     * @return the path to the addons directory in the webapp.
     */
    private String getMfappOfficialAddonsPath() {
        return context.getRealPath("/app/addons");
    }

    private JSONArray buildAddonSpecs(String path) {
        JSONArray addons = new JSONArray();
        String[] files = new File(path).list(DirectoryFileFilter.INSTANCE);
        if (files == null) {
            return addons;
        }
        for (int i = 0; i < files.length; i++) {
            File curConfig = new File(String.format("%s%s%s%s%s", path,
                    File.separator, files[i], File.separator, "config.json"));

            if (!curConfig.exists()) {
                LOG.error(String.format("Addon %s does not have a config.json configuration file, skipping it.", files[i]));
                continue;
            }
            try {
                JSONArray parsed = new JSONArray(FileUtils.readFileToString(curConfig));
                for (int j = 0; j < parsed.length(); ++j) {
                    JSONObject addonInstance = parsed.getJSONObject(j);
                    addons.put(addonInstance);
                }
            } catch (IOException e) {
                LOG.error(String.format("Addon %s does not have a readable config.json configuration file, skipping it.", files[i]), e);
                continue;
            } catch (ClassCastException e) {
                LOG.error(String.format("config.json for addon %s is not a JSON array, skipping it.", files[i]), e);
                continue;
            } catch (JSONException e) {
                LOG.error(String.format("Error parsing config.json for addon %s, skipping it.", files[i]), e);
                continue;
            }
        }

        return addons;
    }

    /**
     * Constructs the array of addons specifications. This is similar with what was defined statically in the GEOR_custom.js file.
     * It first scans the official addons, before scanning (if activated and available) the addons from the datadir.
     *
     * @return a JSON array with the addons specifications.
     * @throws JSONException
     */
    public JSONArray constructAddonsSpec() throws JSONException {
        JSONArray addons = new JSONArray();

        // First, looks up the addons in the webapp dir
        String path1 = getMfappOfficialAddonsPath();
        addons = buildAddonSpecs(path1);


        String path2 = getMfappAddonPath();

        JSONArray addons2 = new JSONArray();

        if (path2 != null) {
            addons2 = buildAddonSpecs(path2);
        }

        HashMap<String, JSONObject> instanceAddon = new HashMap<String, JSONObject>();
        // elements from addons2 override the one from addons
        for (int i = 0; i < addons2.length(); ++i) {
            JSONObject cur = addons2.getJSONObject(i);
            instanceAddon.put(cur.getString("id"), cur);
        }
        // same loop onto the "official" addons
        for (int i = 0; i < addons.length(); ++i) {
            JSONObject cur = addons.getJSONObject(i);
            String currentInstanceId = cur.getString("id");
            // only adds the addon if not overridden in the datadir
            if (instanceAddon.get(currentInstanceId) == null) {
                instanceAddon.put(cur.getString("id"), cur);
            }
        }

        JSONArray finalAddonArray = new JSONArray();
        for (String key : instanceAddon.keySet()) {
            // do not add the addon if it is not enabled
            JSONObject curAddon = instanceAddon.get(key);
            boolean enabled;
            try {
                enabled = curAddon.getBoolean("enabled");
            } catch (JSONException e) {
                LOG.warn("No flag \"enabled\" found in the config.json specification for addon \"" + key
                        + "\", considering as disabled. Please check your addon configuration.");
                enabled = false;
            }
            if (enabled) {
                finalAddonArray.put(curAddon);
            }
        }
        return finalAddonArray;
    }

    /**
     * Setter for georchestraConfiguration, used only for testing.
     * @param gc
     */
    public void setGeorchestraConfiguration(GeorchestraConfiguration gc) {
        georchestraConfiguration = gc;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.context = servletContext;
    }

}
