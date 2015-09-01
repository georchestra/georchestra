package org.georchestra.mapfishapp.ws;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
        response.setContentType("application/javascript; charset=UTF-8");
        JSONArray jsAddons = constructAddonsSpec();

        response.getOutputStream().write(jsAddons.toString(4).getBytes());
    }

    @RequestMapping(value="/addons/**")
    public void getAddonFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String pathInfo = request.getPathInfo().replaceFirst("/addons/", "");
        String path = getMfappAddonPath();

        Path reqPath = new File(path, pathInfo).toPath();
        Path datadir = new File(path).toPath();

        if (! reqPath.toAbsolutePath().toString().startsWith(datadir.toAbsolutePath().toString())) {
            LOG.error("Unexpected file requested (not in datadir): " + reqPath.toString());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        File actualFile = new File(String.format("%s%s%s", datadir, File.separator, pathInfo));
        if (! actualFile.exists()) {
            LOG.error("requested file does not exist: " + actualFile);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String ext = FilenameUtils.getExtension(actualFile.getAbsolutePath());

        if ("css".equalsIgnoreCase(ext)) {
            response.setContentType("text/css; charset: UTF-8");
        } else if ("js".equalsIgnoreCase(ext)) {
            response.setContentType("application/javascript; charset: UTF-8");
        } else if ("png".equalsIgnoreCase(ext)) {
            response.setContentType("image/png");
        } else {
            response.setContentType("text/plain");
        }
        response.getOutputStream().write(FileUtils.readFileToByteArray(actualFile));
    }

    private String getMfappAddonPath() {
        String path;
        if ((georchestraConfiguration != null) && (georchestraConfiguration.activated())) {
            path = new File(georchestraConfiguration.getContextDataDir(), "addons").getAbsolutePath();
        } else {
            path = context.getRealPath("/app/addons");
        }
        return path;
    }

    public JSONArray constructAddonsSpec() {
        String path = getMfappAddonPath();

        JSONArray addons = new JSONArray();
        String[] files = new File(path).list(DirectoryFileFilter.INSTANCE);
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
                    if (addonInstance.getBoolean("enabled")) {
                        addons.put(addonInstance);
                    }
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
