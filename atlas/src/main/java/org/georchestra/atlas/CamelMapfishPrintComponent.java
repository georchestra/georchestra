package org.georchestra.atlas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.FileUtils;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONException;
import org.json.JSONWriter;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.cli.Main;
import org.mapfish.print.wrapper.json.PJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import com.google.common.annotations.VisibleForTesting;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class CamelMapfishPrintComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private MapPrinter mapPrinter = null;

    @Autowired
    private GeorchestraConfiguration georConfiguration;

    public void init() throws IOException, URISyntaxException {
        ApplicationContext context = new ClassPathXmlApplicationContext(Main.DEFAULT_SPRING_CONTEXT);
        this.mapPrinter = context.getBean(MapPrinter.class);

        URI configFileUrl = this.getClass().getResource("/atlas/config.yaml").toURI();
        if (this.georConfiguration != null && this.georConfiguration.activated()) {
            configFileUrl = new File(this.georConfiguration.getContextDataDir() + "/mapfishprintv3/config.yaml")
                    .toURI();
        }
        Assert.notNull(configFileUrl);
        byte[] configFileData = FileUtils.readFileToByteArray(new File(configFileUrl));
        this.mapPrinter.setConfiguration(configFileUrl, configFileData);
    }

    public void toMapfishPrintPdf(Exchange ex)
            throws JSONException, DocumentException, URISyntaxException, IOException {
        String mfprintJsonSpec = ex.getIn().getBody(String.class);

        Assert.notNull(this.mapPrinter);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Message m = ex.getIn();

        try {
            PJsonObject mfSpec = MapPrinter.parseSpec(mfprintJsonSpec);
            this.mapPrinter.print(mfSpec, baos);
        } catch (Exception e) {
            log.error("Error generating PDF, returning a blank pdf with the error message", e);
            baos = generateErrorPdf(e);
        } finally {
            m.setBody(baos.toByteArray());
            ex.setOut(m);
        }
    }

    @VisibleForTesting
    public ByteArrayOutputStream generateErrorPdf(Throwable e) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document d = new Document();
        PdfWriter.getInstance(d, baos);
        d.open();
        d.add(new Paragraph(e.getMessage()));
        d.close();
        return baos;
    }

    public void printCapabilities(Exchange ex) throws JSONException {
        StringWriter strw = new StringWriter();

        JSONWriter w = new JSONWriter(strw);
        w.object();
        mapPrinter.printClientConfig(w);
        w.endObject();
        ex.getOut().setBody(strw.toString());
    }
}
