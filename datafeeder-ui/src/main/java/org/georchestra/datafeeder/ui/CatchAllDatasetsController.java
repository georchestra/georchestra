/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.datafeeder.ui;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Controller which catch requests and returns the index.html file from the UI.
 * Regardless of the URL which is queried, we need to return the index.html
 * file, as the path will be interpreted client-side by the Javascript code.
 * <p>
 * This is mainly the reason why we need a webapp here, in the Docker image
 * which derives from Nginx, the same feature is implemented using the following
 * configuration:
 * <p>
 * ``` location ~
 * "^/[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}.*$"
 * { alias /usr/share/nginx/html/index.html; add_header "content-type"
 * "text/html"; } ```
 *
 * Using spring may sound a bit overkill, and we could probably implement the
 * same using a simpler webapp (e.g. using urlrewrite).
 *
 */
@Controller
public class CatchAllDatasetsController {

    private void serveIndexHtml(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "text/html;charset=UTF-8");

        OutputStream os = response.getOutputStream();
        InputStream is = request.getServletContext().getResourceAsStream("/index.html");
        IOUtils.copy(is, os);
    }

    /**
     * Returns the index.html whenever a url with the `/[uuid]` format is requested.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public void singleUuid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    /**
     * Returns the index.html whenever a url with the `/[uuid]/validation` format is
     * requested.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/validation")
    public void singleUuidValidation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    /**
     * Returns the index.html whenever a url with the `/[uuid]/step/[stepid]` format
     * is requested.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/step/{stepid}")
    public void uuidStep(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    /**
     * Returns the index.html whenever a url with the `/[uuid]/confirm` format is
     * requested.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/confirm")
    public void uuidConfirm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    /**
     * Returns the index.html whenever a url with the `/[uuid]/publish` format is
     * requested.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/publish")
    public void uuidPublish(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    /**
     * Returns the index.html whenever a url with the `/[uuid]/publishok` format is
     * requested.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/publishok")
    public void uuidPublishOk(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

    /**
     * Default entrypoint.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/")
    public void defaultRoot(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serveIndexHtml(request, response);
    }

}
