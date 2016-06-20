package org.georchestra.atlas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

public class BundlePdfComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String tempDir;

    public void init() throws IOException {
        FileUtils.forceMkdir(new File(tempDir));
    }
    
    @Handler
    public void pdfMerge(Exchange ex) throws Exception {
        AtlasJob j = ex.getIn().getBody(AtlasJob.class);
        Long jobId = j.getId();
        Iterator<File> pdfs = FileUtils.iterateFiles(new File(this.tempDir,  "" + jobId),
                new String[] { "pdf" }, false);
        
        List<File> pdfsList = new ArrayList<File>();
        while (pdfs.hasNext()) {
            pdfsList.add(pdfs.next());
        }
        Collections.sort(pdfsList, NameFileComparator.NAME_COMPARATOR);
        Document document = new Document();
        PdfCopy cop = new PdfCopy(document, new ByteArrayOutputStream());
        document.open();
     for (File pdf : pdfsList) {
         PdfReader reader = new PdfReader(pdf.getAbsolutePath());
         int n = reader.getNumberOfPages();
         for (int page = 0; page < n; ) {
             cop.addPage(cop.getImportedPage(reader, ++page));
         }
         cop.freeReader(reader);
         reader.close();
     }
     document.close();
     // TODO: save doc somewhere ? add it to the exchange output ?

    }
    
    @Handler
    public void pdfZip(Exchange ex) throws IOException {
        // TODO refactor (DRY)
        AtlasJob j = ex.getIn().getBody(AtlasJob.class);
        Long jobId = j.getId();
        Iterator<File> pdfs = FileUtils.iterateFiles(new File(this.tempDir,  "" + jobId),
                new String[] { "pdf" }, false);
        
        List<File> pdfsList = new ArrayList<File>();
        while (pdfs.hasNext()) {
            pdfsList.add(pdfs.next());
        }

        ZipOutputStream zip = new ZipOutputStream(new ByteArrayOutputStream());
        for (File file : pdfsList) {
                ZipEntry e = new ZipEntry(FilenameUtils.getName(file.getAbsolutePath()));
                zip.putNextEntry(e);
                FileInputStream in = new FileInputStream(file);
                IOUtils.copy(in, zip);
                IOUtils.closeQuietly(in);
        }
        IOUtils.closeQuietly(zip);
        // TODO ?? same
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
}
