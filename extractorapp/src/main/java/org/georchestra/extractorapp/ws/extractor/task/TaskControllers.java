/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

package org.georchestra.extractorapp.ws.extractor.task;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ServletContextAware;

@Controller
public class TaskControllers implements ServletContextAware {

    private ServletContext servletContext;
    private ExtractionManager extractionManager;
    private static final Log LOG = LogFactory.getLog(ExtractionTask.class.getPackage().getName());

    @RequestMapping(value = "/jobs/list", method = RequestMethod.GET)
    public void list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.debug("Into /jobs/list");
        JSONObject ret = new JSONObject();
        OutputStream outpStr = null;

        try {
            outpStr = response.getOutputStream();
            LOG.debug("printing output of ExtractionManager state");

            JSONArray jsarr = new JSONArray();
            List<ExecutionMetadata> lst = extractionManager.getTaskQueue();

            for (ExecutionMetadata elem : lst) {
                JSONObject task = new JSONObject();
                task.put("uuid", elem.getUuid());
                task.put("state", elem.getState());
                task.put("priority", elem.getPriority());

                jsarr.put(task);
            }
            ret.put("tasks", jsarr);

            ret.put("status", "success");

        } catch (Exception e) {
            LOG.error("Exception caught while running '/jobs/list' controller: ", e);

        } finally {

            outpStr.write(ret.toString().getBytes());
            if (outpStr != null)
                outpStr.close();

        }
        return;
    }

    @RequestMapping(value = "/job/change_priority", method = RequestMethod.POST)
    public void results(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void setServletContext(ServletContext _servletContext) {
        servletContext = _servletContext;
    }

    public void setExtractionManager(ExtractionManager em) {
        extractionManager = em;
    }
}
