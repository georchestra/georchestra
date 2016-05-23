package org.georchestra.atlas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.mapfish.print.MapPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class CamelMapfishPrintComponent {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final MapPrinter mp = new MapPrinter();
    @Handler
    public void toMapfishPrintPdf(Exchange ex) throws JSONException, DocumentException, URISyntaxException, IOException {
        String rawJson = ex.getProperty("rawJson", String.class);
        JSONObject jobSpec = new JSONObject(new JSONTokener(rawJson));
        URI configFile = this.getClass().getResource("/atlas/config.yaml").toURI();
        //byte[] configFileData = FileUtils.readFileToByteArray(new File(configFile));
        //mp.setConfiguration(configFile, configFileData);

        // To be continued
        try {
            Document document = new Document();
            ByteArrayOutputStream baos =  new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph(jobSpec.toString(4)));
            document.close();

            Message m = ex.getIn();
            m.setBody(baos.toByteArray());
            ex.setOut(m);
            // http://stackoverflow.com/questions/25604555/merge-pdf-documents-of-different-width-using-itext
            // using addDocument()
            //PdfCopy cop = new PdfCopy(document, new ByteArrayOutputStream());
            //cop.addDocument(reader);

        } catch (Exception e) {
            log.error("Error generating PDF", e);
        }

    }
}
