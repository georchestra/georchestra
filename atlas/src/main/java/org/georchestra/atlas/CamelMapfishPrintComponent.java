package org.georchestra.atlas;

import com.itextpdf.text.DocumentException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONWriter;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.cli.Main;
import org.mapfish.print.wrapper.json.PJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

public class CamelMapfishPrintComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private MapPrinter mapPrinter = null;

    public void init() throws IOException, URISyntaxException {
        ApplicationContext context = new ClassPathXmlApplicationContext(Main.DEFAULT_SPRING_CONTEXT);
        this.mapPrinter = context.getBean(MapPrinter.class);

        URL configFileUrl = this.getClass().getResource("/atlas/config.yaml");
        Assert.notNull(configFileUrl);
        byte[] configFileData = FileUtils.readFileToByteArray(new File(configFileUrl.toURI()));
        this.mapPrinter.setConfiguration(configFileUrl.toURI(), configFileData);
    }

    public void toMapfishPrintPdf(Exchange ex)
            throws JSONException, DocumentException, URISyntaxException, IOException {
        String mfprintJsonSpec = ex.getIn().getBody(String.class);

        Assert.notNull(this.mapPrinter);

        try {
            ByteArrayOutputStream baos =  new ByteArrayOutputStream();
            PJsonObject mfSpec = MapPrinter.parseSpec(mfprintJsonSpec); 
            this.mapPrinter.print(mfSpec, baos);
            Message m = ex.getIn();
            m.setBody(baos.toByteArray());
            ex.setOut(m);
        } catch (Exception e) {
            log.error("Error generating PDF", e);
        }
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
