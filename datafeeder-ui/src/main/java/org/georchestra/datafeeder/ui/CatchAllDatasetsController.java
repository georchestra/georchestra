package org.georchestra.datafeeder.ui;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


@Controller
public class CatchAllDatasetsController {

    private void serveIndexHtml(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OutputStream os = response.getOutputStream();
        InputStream is = request.getServletContext().getResourceAsStream("/index.html");
        IOUtils.copy(is, os);
    }

    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public void singleUuid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/validation")
    public void singleUuidValidation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/step/{stepid}")
    public void uuidStep(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/confirm")
    public void uuidConfirm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/publish")
    public void uuidPublish(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/publishok")
    public void uuidPublishOk(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    @RequestMapping("/")
    public void defaultRoot(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }


}
