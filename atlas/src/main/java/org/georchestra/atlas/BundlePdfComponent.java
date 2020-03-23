package org.georchestra.atlas;

import com.google.common.annotations.VisibleForTesting;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.camel.Exchange;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BundlePdfComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String tempDir;

    @PostConstruct
    public void init() throws IOException {
        FileUtils.forceMkdir(new File(tempDir));
    }

    public void pdfMerge(Exchange ex) throws Exception {
        Long jobId = (Long) ex.getProperty("jobId");
        ex.setProperty("extension", "pdf");
        Document document = new Document();
        ByteArrayOutputStream bsDoc = new ByteArrayOutputStream();

        List<File> pdfs = getFiles(jobId);
        PdfCopy cop = new PdfCopy(document, bsDoc);

        document.open();
        for (File pdf : pdfs) {
            PdfReader reader = new PdfReader(pdf.getAbsolutePath());
            int n = reader.getNumberOfPages();
            for (int page = 0; page < n;) {
                cop.addPage(cop.getImportedPage(reader, ++page));
            }
            cop.freeReader(reader);
            reader.close();
        }
        document.close();
        ex.getOut().setBody(bsDoc.toByteArray());
    }

    public void pdfZip(Exchange ex) throws IOException {
        Long jobId = (Long) ex.getProperty("jobId");
        ex.setProperty("extension", "zip");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(out);
        List<File> files = getFiles(jobId);
        for (File file : files) {
            ZipEntry e = new ZipEntry(FilenameUtils.getName(file.getAbsolutePath()));
            zip.putNextEntry(e);
            FileInputStream in = new FileInputStream(file);
            IOUtils.copy(in, zip);
            IOUtils.closeQuietly(in);
        }
        IOUtils.closeQuietly(zip);
        ex.getOut().setBody(out.toByteArray());
    }

    @VisibleForTesting
    public List<File> getFiles(Long jobId) {
        Iterator<File> pdfs = FileUtils.iterateFiles(new File(this.tempDir, "" + jobId), new String[] { "pdf" }, false);

        List<File> pdfsList = new ArrayList<File>();
        while (pdfs.hasNext()) {
            pdfsList.add(pdfs.next());
        }
        Collections.sort(pdfsList, NameFileComparator.NAME_COMPARATOR);
        return pdfsList;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
}
