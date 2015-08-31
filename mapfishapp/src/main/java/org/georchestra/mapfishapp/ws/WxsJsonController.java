package org.georchestra.mapfishapp.ws;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This controller allows the retrieval of the 3 files needed
 * by the layer browser interface:
 *
 * - wms.servers.json
 * - wfs.servers.json
 * - wmts.servers.json
 *
 * @author pmauduit
 *
 */
@Controller
public class WxsJsonController {

    @Autowired
    private GeorchestraConfiguration georConfig;

    // used for testing
    public void setGeorchestraConfiguration(GeorchestraConfiguration georConfig) {
        this.georConfig = georConfig;
    }

    @RequestMapping(value="/{wxs}.servers.json")
    public void wxsServerJson(@PathVariable("wxs") String proto, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        if ((georConfig == null) || (! georConfig.activated())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String msg = "GeorchestraConfiguration is not present or " +
                    " wrongly configured, please check configuration.";
            response.getOutputStream().write(msg.getBytes());
            return;
        }

        if ((StringUtils.isEmpty(proto)) ||
                ((! "wms".equalsIgnoreCase(proto)) &&
                 (! "wmts".equalsIgnoreCase(proto)) &&
                 (! "wfs".equalsIgnoreCase(proto))))
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().write(String.format("Bad parameter value: \"%s\"", proto).getBytes());
            return;
        }

        String wxsJsonPath = String.format("%s/js/%s.servers.json", georConfig.getContextDataDir(),
                proto.toLowerCase());
        File wxsJsonF = new File(wxsJsonPath);

        if (! wxsJsonF.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getOutputStream().write(String.format("file not found: \"%s\"",
                    wxsJsonF.getName()).getBytes());
            return;
        }
        byte[] wxsJson = FileUtils.readFileToByteArray(wxsJsonF);
        response.getOutputStream().write(wxsJson);
        return;
    }
}
