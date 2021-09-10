package org.georchestra.console.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A simple controller to serve an area.json file from your datadir.
 */
@Controller
public class AreaController {

    @Value("${AreasUrl:area.geojson}")
    private String areasUrl;

    @Value("${georchestra.datadir:/etc/georchestra}")
    private String datadir;

    /**
     * serve a json file put in the datadir or redirect to a url if AreaUrl in the
     * config is set to a url.
     * 
     * @return json or rediurect to the resource if area is an URL
     * @throws IOException
     */
    @GetMapping(value = "/public/area.geojson", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String serveArea(HttpServletResponse response) throws IOException {
        // if arealUrl in config is an http endpoint, then send it directly.
        if (isURL(areasUrl)) {
            response.sendRedirect(areasUrl);
            return "";
        }
        File areaJsonFile = lookForAreaUrl();
        try (InputStream stream = new FileInputStream(areaJsonFile)) {
            String jsonString = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
            return new JSONObject(jsonString).toString();
        } catch (IOException ioe) {
            // in case there are any errors with the area file, pretend it didn't exist.
            response.setStatus(404);
            return "{\"error\": \"area.geojson not found\"}";
        } catch (JSONException ex) {
            response.setStatus(500);
            return "{\"error\": \"specifed file (area.geojson) could not be parsed server side\"}";
        }
    }

    /**
     * try to be a bit permissive in what path we accept for area.json file, could
     * be just `area.json` could be /the/whole/path/to/datadir/
     */
    private File lookForAreaUrl() {
        String[] possiblePath = { areasUrl, Paths.get(datadir, areasUrl).toString(),
                Paths.get(datadir, "/console/", areasUrl).toString(), };
        File f = null;
        for (String p : possiblePath) {
            f = new File(p);
            if (f.exists()) {
                return f;
            }
        }
        return f;
    }

    /**
     * Check if the path given in AreaUrl is an actual URL or a file
     */
    private boolean isURL(String possibleUrl) {
        return possibleUrl.startsWith("http");
    }
}
