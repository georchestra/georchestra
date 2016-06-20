package org.georchestra.atlas;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.georchestra.atlas.repository.AtlasJobRepository;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class JobController {

    private String tempDir;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private AtlasJobRepository atlasRepo;

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
    
    public void init() {}
    
    @RequestMapping("/blah")
    public void itBlah(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getOutputStream().write("It blah".getBytes());
    }

    @RequestMapping("/{jobId}/{token}.{ext}")
    public void provideFile(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String jobId,
            @PathVariable String token,
            @PathVariable String ext) throws IOException {
        
        File jobDir = new File(tempDir, jobId + "");

        if ("zip".equalsIgnoreCase(ext) && "pdf".equalsIgnoreCase(ext)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        File actualFile = new File(jobDir, token + "." + ext);
        if (! actualFile.exists()) {
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
