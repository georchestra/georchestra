package org.georchestra.atlas;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.http.common.HttpMessage;
import org.apache.commons.io.FileUtils;
import org.georchestra.atlas.repository.AtlasJobRepository;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class JobController {

    private String tempDir;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AtlasJobRepository atlasRepo;

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public void init() {
    }

    @Handler
    public void provideResult(Exchange ex) throws IOException {

        HttpServletResponse response = ex.getIn(HttpMessage.class).getResponse();
        String jobId = (String) ex.getIn().removeHeader("jobId");
        String token = (String) ex.getIn().removeHeader("token");
        String ext = (String) ex.getIn().removeHeader("ext");

        File jobDir = new File(tempDir, jobId + "");

        if (!"zip".equalsIgnoreCase(ext) && !"pdf".equalsIgnoreCase(ext)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        File actualFile = new File(jobDir, token + "." + ext);
        if (!actualFile.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        AtlasJob job = atlasRepo.findOneByIdAndToken(Long.parseLong(jobId), token);

        if (job == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        byte[] b = FileUtils.readFileToByteArray(actualFile);
        String filename = "output." + ext;
        try {
            filename = job.getFileName();
        } catch (JSONException e) {
            log.error("Unable to parse the original query for job " + job, e);
        }
        response.setHeader("content-disposition", "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().write(b);
        return;
    }
}
