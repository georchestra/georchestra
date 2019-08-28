package org.georchestra.atlas.converters;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;

import org.georchestra.atlas.AtlasJob;
import org.json.JSONException;

import java.io.*;

@Converter
@SuppressWarnings("unused")
public class AtlasJobConverter {

    private static String toString(InputStream property) throws IOException {

        property.reset();
        BufferedReader reader = new BufferedReader(new InputStreamReader(property));

        StringBuilder rawString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            rawString.append(line);
        return rawString.toString();
    }

    @Converter
    public AtlasJob toAtlasJob(InputStream is, Exchange exchange) throws IOException, JSONException {

        String body = AtlasJobConverter.toString(is);
        return this.toAtlasJob(body, exchange);
    }

    @Converter
    public AtlasJob toAtlasJob(String body, Exchange exchange) throws IOException, JSONException {
        return new AtlasJob(body);
    }

    @Converter
    public InputStream fromAtlasMFPJob(AtlasJob job, Exchange exchange) throws UnsupportedEncodingException {
        String charsetName = exchange.getProperty(Exchange.CHARSET_NAME, String.class);
        if (charsetName == null) {
            charsetName = "UTF-8";
            exchange.setProperty(Exchange.CHARSET_NAME, charsetName);
        }

        exchange.setProperty("jobId", job.getId());
        exchange.setProperty("state", job.getState());

        return new ByteArrayInputStream(job.getQuery().getBytes(charsetName));
    }

}
