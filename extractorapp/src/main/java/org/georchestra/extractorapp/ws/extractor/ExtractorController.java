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

package org.georchestra.extractorapp.ws.extractor;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.extractorapp.ws.AbstractEmailFactory;
import org.georchestra.extractorapp.ws.Email;
import org.georchestra.extractorapp.ws.extractor.task.ExecutionMetadata;
import org.georchestra.extractorapp.ws.extractor.task.ExecutionPriority;
import org.georchestra.extractorapp.ws.extractor.task.ExtractionManager;
import org.georchestra.extractorapp.ws.extractor.task.ExtractionTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ServletContextAware;

/**
 * Controller for the Extractor
 *
 * @author jeichar
 */
@Controller
public class ExtractorController implements ServletContextAware {
    public static final String EXTRACTION_ZIP_EXT = "-extraction.zip";

    private static final Log LOG = LogFactory.getLog(ExtractorController.class.getPackage().getName());

    private static final String BASE_MAPPING = "/extractor/";
    private static final String EXTRACTOR_MAPPING = BASE_MAPPING + "initiate";
    private static final String TEST_EXTRACTOR_MAPPING = BASE_MAPPING + "test/initiate";
    private static final String RESULTS_MAPPING = BASE_MAPPING + "package";
    private static final String UUID_PARAM = "uuid";

    private static final String EXTRACTOR_TASKS = BASE_MAPPING + "tasks";

    private String responseTemplateFile;
    private String reponseMimeType;
    private String responseCharset;
    private AbstractEmailFactory emailFactory;
    private ServletContext servletContext;
    private String servletUrl;
    private String publicUrl;
    private String extractionFolderPrefix;
    private boolean remoteReproject = true;
    private boolean useCommandLineGDAL = false;

    private UsernamePasswordCredentials adminCredentials;
    private String secureHost;
    private long maxCoverageExtractionSize = Long.MAX_VALUE;

    private ExtractionManager extractionManager;
    private String userAgent;

    private @Autowired DataSource dataSource;

    public void validateConfig() throws PropertyVetoException, MalformedURLException {
        setSecureHostAndServletUrl(this.publicUrl);
        if (extractionManager == null) {
            throw new AssertionError("A extractionManager needs to be defined in spring configuration");
        }
        File storageFile = FileUtils.storageFile("");
        if (!storageFile.exists()) {
            if (!storageFile.mkdirs()) {
                throw new AssertionError("extractorapp does not have access to " + storageFile + " and cannot create it");
            }
        }
    }

    @RequestMapping(value = RESULTS_MAPPING, method = RequestMethod.GET)
    public void results(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uuid = request.getParameter(UUID_PARAM);
        File file = FileUtils.storageFile(uuid + EXTRACTION_ZIP_EXT);

        if (file.exists()) {
            LOG.info("request for extraction archive: " + file + " requested by " + request.getRemoteAddr());
            FileInputStream in = new FileInputStream(file);
            ServletOutputStream out = response.getOutputStream();
            try {
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=" + extractionFolderPrefix + uuid + ".zip");
                in.getChannel().transferTo(0, file.length(), Channels.newChannel(out));
            } finally {
                try {
                    in.close();
                } finally {
                    out.close();
                }
            }
        } else {
            LOG.warn("request for a non-existing extraction archive: " + file + " requested by " + request.getRemoteAddr());
            response.sendError(404, "Requested file not found");
        }
    }

    @RequestMapping(value = EXTRACTOR_MAPPING, method = RequestMethod.POST)
    public void extract(HttpServletRequest request, HttpServletResponse response) throws Exception {
        doExtraction(false, request, response);
    }

    @RequestMapping(value = TEST_EXTRACTOR_MAPPING, method = RequestMethod.POST)
    public void testExtract(HttpServletRequest request, HttpServletResponse response) throws Exception {
        doExtraction(true, request, response);
    }

    /**
     * Lists the tasks waiting in the extraction queue.
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = EXTRACTOR_TASKS, method = RequestMethod.GET)
    public void getTaskQueue(HttpServletRequest request, HttpServletResponse response) throws Exception {

        LOG.debug("Executing getTaskQueue - GET - " + request.getRequestURL());

        List<ExecutionMetadata> taskQueue = extractionManager.getTaskQueue();

        ExtractorGetTaskQueueResponse responseData = ExtractorGetTaskQueueResponse.newInstance(taskQueue);

        response.setCharacterEncoding(responseCharset);
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        try {
            out.println(responseData.asJsonString());
        } finally {
            out.close();
        }
    }

    /**
     * Analyzes the changes required in the task described in the parameter.
     * This method supposes that only one change is done in one call.
     * 
     * <pre>
     * Expected uri: /extractor/task/{uuid}
     * </pre>
     * 
     * Spring 2.5 has not got @PathVariable, thus this method was defined as
     * "/*" to match uuid. The task id is retrieved from json object maintained
     * in the request content.
     *
     */
    @RequestMapping(value = EXTRACTOR_TASKS + "/*", method = RequestMethod.PUT)
    public void updateTask(HttpServletRequest request, HttpServletResponse response) throws Exception {

        LOG.debug("Executing updateTask - PUT - " + request.getRequestURL());

        String jsonTask = FileUtils.asString(request.getInputStream());

        TaskDescriptor taskParam = new TaskDescriptor(jsonTask);

        // Analyzes the changes required. This method suppose that only one
        // change is done in one call.
        String id = taskParam.getID();
        TaskDescriptor currentTask;
        try {
            currentTask = findTask(id);
        } catch (TaskNotFoundException e) {
            // the task could be removed from the queue.
            return;
        }
        if (currentTask.getPriority() != taskParam.getPriority()) {
            // changes the priority
            try {
                updatePriority(id, taskParam.getPriority().ordinal());
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }

        }
        if (currentTask.getStatus() != taskParam.getStatus()) {
            // changes the task status
            this.extractionManager.updateStatus(id, taskParam.getStatus());
        }
    }

    /**
     * Finds the task with the indeed uuid
     * 
     * @param id
     *            the task id
     * @return the task id
     * @throws TaskNotFoundException
     */
    private TaskDescriptor findTask(final String id) throws TaskNotFoundException {

        ExtractionTask foundTask = extractionManager.findTask(id);
        if (foundTask == null) {
            // the required task could be removed. It could be removed for the
            // queue by other process.
            throw new TaskNotFoundException("The task wask not found. ID: " + id);
        }
        TaskDescriptor task = new TaskDescriptor(foundTask.executionMetadata);

        return task;
    }

    /**
     * Updates the task's priority.
     *
     * @param uuid
     * @param priority
     *            valid values are 0-LOW, 1-MEDIUM, 2-HIGH
     * @throws Exception
     */
    private void updatePriority(String uuid, int priority) throws IllegalArgumentException, InvalidPriorityException {

        if (uuid == null || "".equals(uuid)) {
            final String msg = "updatePriority method expects an uuid";
            IllegalArgumentException e = new IllegalArgumentException(msg);
            LOG.error(msg, e);
            throw e;
        }
        if (priority < ExecutionPriority.LOW.ordinal() || priority > ExecutionPriority.HIGH.ordinal()) {
            final String msg = "updatePriority method expects an priority value between " + ExecutionPriority.LOW.ordinal() + " and "
                    + ExecutionPriority.HIGH.ordinal();
            InvalidPriorityException e = new InvalidPriorityException(msg);
            LOG.error(msg, e);
            throw e;

        }
        ExtractorUpdatePriorityRequest updatePriorityRequest = ExtractorUpdatePriorityRequest.newInstance(uuid, priority);

        this.extractionManager.updatePriority(updatePriorityRequest._uuid, updatePriorityRequest._priority);
    }

    // ----------------- implementation of extraction ----------------- //

    private void doExtraction(boolean testing, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String postData = FileUtils.asString(request.getInputStream());
        String reponseData = "";

        UUID requestUuid = UUID.randomUUID();

        URL urlObj = new URL(servletUrl);
        if (urlObj.getPort() == urlObj.getDefaultPort()) {
            urlObj = new URL(urlObj.getProtocol(), urlObj.getHost(), urlObj.getFile());
        }
        StringBuilder url = new StringBuilder(urlObj.toString());
        url.append(RESULTS_MAPPING);
        url.append("?");
        url.append(UUID_PARAM);
        url.append("=");
        url.append(requestUuid);

        List<ExtractorLayerRequest> requests = Collections.unmodifiableList(ExtractorLayerRequest.parseJson(postData));
        if (requests.size() > 0) {

            String[] recipients = requests.get(0)._emails;
            Email email = emailFactory.createEmail(request, recipients, url.toString());

            String username = request.getHeader("sec-username");
            String roles = request.getHeader("sec-roles");
            String org = request.getHeader("sec-orgname");
            RequestConfiguration requestConfig = new RequestConfiguration(requests, requestUuid, email, servletContext, testing, username, roles, org,
                    adminCredentials, secureHost, extractionFolderPrefix, maxCoverageExtractionSize, remoteReproject, useCommandLineGDAL, postData, this.userAgent);
            ExtractionTask extractor = new ExtractionTask(requestConfig, this.dataSource);

            LOG.info("Sending mail to user");
            try {
                email.sendAck();
            } catch (Throwable e) {
                LOG.error("Error while sending the notification to the user.", e);
            }
            LOG.info("Extraction request submitted, request uuid = " + extractor.executionMetadata.getUuid());

            if (testing) {
                extractor.run();
            } else {
                extractionManager.submit(extractor);
            }

            reponseData = replace(readFile(responseTemplateFile), url.toString(), recipients);

            response.setCharacterEncoding(responseCharset);
            response.setContentType(reponseMimeType);
        }
        PrintWriter out = response.getWriter();

        try {
            out.println(reponseData);
        } finally {
            out.close();
        }
    }

    // ----------------- JavaBean methods ----------------- //

    /**
     * Sets the template used to respond to the user. Each instance of {link}
     * will be replaced with the URL
     */
    public void setResponseTemplateFile(String responseTemplate) throws IOException {
        this.responseTemplateFile = responseTemplate;
    }

    public void setReponseMimeType(String reponseMimeType) {
        this.reponseMimeType = reponseMimeType;
    }

    public void setResponseCharset(String charset) {
        this.responseCharset = charset;
    }

    /**
     * Sets the template used to will be emailed to the user when extraction is
     * complete.
     *
     * Each instance of {link} will be replaced with the extraction bundle URL
     */

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public void setSecureHostAndServletUrl(String rawPublicUrl) throws MalformedURLException {
        // extract hostname to set secureHost
        URL publicURL = new URL(rawPublicUrl);
        this.secureHost = publicURL.getHost();

        String servletBaseUrl = rawPublicUrl;

        if (!servletBaseUrl.endsWith("/"))
            servletBaseUrl = String.format("%s/", servletBaseUrl);

        // Append servlet context
        this.servletUrl = servletBaseUrl + this.servletContext.getServletContextName();
    }

    public void setAdminCredentials(UsernamePasswordCredentials adminCredentials) {
        this.adminCredentials = adminCredentials;
    }

    private String replace(String template, String url, String[] emails) {
        String t = template.replace("{link}", url);
        t = t.replace("{emails}", Arrays.toString(emails));
        return t;
    }

    private String readFile(String responseTemplate) throws IOException {
        String realPath = servletContext.getRealPath(responseTemplate);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(realPath), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line);
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }

    public void setMaxCoverageExtractionSize(long maxCoverageExtractionSize) {
        this.maxCoverageExtractionSize = maxCoverageExtractionSize;
    }

    public void setExtractionManager(ExtractionManager extractionManager) {
        this.extractionManager = extractionManager;
    }

    public void setRemoteReproject(boolean remoteReproject) {
        this.remoteReproject = remoteReproject;
    }

    public void setUseCommandLineGDAL(boolean useCommandLineGDAL) {
        this.useCommandLineGDAL = useCommandLineGDAL;
    }

    public void setExtractionFolderPrefix(String extractionFolderPrefix) {
        if (extractionFolderPrefix == null) {
            this.extractionFolderPrefix = "extraction-";
        } else {
            this.extractionFolderPrefix = extractionFolderPrefix;
        }
    }

    // ----------------- Methods for accessing servlet context -----------------
    // //
    // ServletContext is required for determining where files are within the
    // webapp
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public AbstractEmailFactory getEmailFactory() {
        return emailFactory;
    }

    public void setEmailFactory(AbstractEmailFactory emailFactory) {
        this.emailFactory = emailFactory;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
