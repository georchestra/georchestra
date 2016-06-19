package org.georchestra.atlas;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundlePdfComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String tempDir;

    public void init() throws IOException {
        FileUtils.forceMkdir(new File(tempDir));
    }
    
    @Handler
    public void pdfMerge(Exchange ex) {
        // http://stackoverflow.com/questions/25604555/merge-pdf-documents-of-different-width-using-itext
        // using addDocument()
        //PdfCopy cop = new PdfCopy(document, new ByteArrayOutputStream());
        //cop.addDocument(reader);
        
        
    }
    
    @Handler
    public void pdfZip(Exchange ex) {
        
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
}
