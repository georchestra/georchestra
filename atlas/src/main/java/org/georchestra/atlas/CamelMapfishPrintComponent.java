package org.georchestra.atlas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.cli.Main;
import org.mapfish.print.wrapper.json.PJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class CamelMapfishPrintComponent {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private MapPrinter mapPrinter = null;
    private ApplicationContext context = null;

    public void init() throws IOException, URISyntaxException {
        this.context = new ClassPathXmlApplicationContext(Main.DEFAULT_SPRING_CONTEXT);
        this.mapPrinter = context.getBean(MapPrinter.class);
        URL configFileUrl = this.getClass().getResource("/atlas/config.yaml");
        Assert.notNull(configFileUrl);
        byte[] configFileData = FileUtils.readFileToByteArray(new File(configFileUrl.toURI()));
        this.mapPrinter.setConfiguration(configFileUrl.toURI(), configFileData);
    }

    
    @Handler
    public void toMapfishPrintPdf(Exchange ex) throws JSONException, DocumentException, URISyntaxException, IOException {
        String rawJson = ex.getProperty("rawJson", String.class);

        Assert.notNull(this.mapPrinter);

        try {
            ByteArrayOutputStream baos =  new ByteArrayOutputStream();
            PJsonObject mfSpec = this.mapPrinter.parseSpec(rawJson); 
            this.mapPrinter.print(mfSpec, baos);
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
