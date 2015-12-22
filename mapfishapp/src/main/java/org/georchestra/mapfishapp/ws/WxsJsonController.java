package org.georchestra.mapfishapp.ws;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

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
public class WxsJsonController implements ServletContextAware {

    @Autowired
    private GeorchestraConfiguration georConfig;

    private ServletContext context;

    // used for testing
    public void setGeorchestraConfiguration(GeorchestraConfiguration georConfig) {
        this.georConfig = georConfig;
    }

    @RequestMapping(value="/{wxs}.servers.json")
    public void wxsServerJson(@PathVariable("wxs") String proto, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String contextDir = "";
        if ((georConfig == null) || (! georConfig.activated())) {
            contextDir = context.getRealPath("/");
        } else
            contextDir = georConfig.getContextDataDir();

        if ((StringUtils.isEmpty(proto)) ||
                ((! "wms".equalsIgnoreCase(proto)) &&
                 (! "wmts".equalsIgnoreCase(proto)) &&
                 (! "wfs".equalsIgnoreCase(proto))))
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().write(String.format("Bad parameter value: \"%s\"", proto).getBytes());
            return;
        }

        String wxsJsonPath = String.format("%s/%s.servers.json", contextDir,
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

    @Override
    public void setServletContext(ServletContext servletContext) {
       this.context = servletContext;
    }
}
