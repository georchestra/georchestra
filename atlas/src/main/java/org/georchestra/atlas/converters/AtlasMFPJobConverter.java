package org.georchestra.atlas.converters;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.georchestra.atlas.AtlasMFPJob;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.UUID;

@Converter
@SuppressWarnings("unused")
public class AtlasMFPJobConverter {

    private static String toString(InputStream property) throws IOException {

        property.reset();
        BufferedReader reader = new BufferedReader(new InputStreamReader(property));

        StringBuilder rawString = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
            rawString.append(line);
        return rawString.toString();
    }

    @Converter
    public AtlasMFPJob toAtlasMFPJob(InputStream is, Exchange exchange) throws IOException, JSONException {

        String body = AtlasMFPJobConverter.toString(is);
        return this.toAtlasMFPJob(body, exchange);
    }

    @Converter
    public AtlasMFPJob toAtlasMFPJob(String query, Exchange exchange) throws IOException, JSONException {

        JSONObject jobSpec = new JSONObject(new JSONTokener(AtlasMFPJobConverter.toString(exchange.getProperty("rawJson", InputStream.class))));
        Integer pageIndex = exchange.getProperty("CamelSplitIndex", Integer.class);
        UUID uuid = exchange.getProperty("uuid", UUID.class);
        String filename = ((JSONObject) jobSpec.getJSONArray("pages").get(pageIndex)).getString("filename");

        return new AtlasMFPJob(uuid, query, filename, pageIndex.shortValue());
    }

    @Converter
    public InputStream fromAtlasMFPJob(AtlasMFPJob job, Exchange exchange) throws UnsupportedEncodingException {
        String charsetName = exchange.getProperty(Exchange.CHARSET_NAME, String.class);
        if (charsetName == null) {
            charsetName = "UTF-8";
            exchange.setProperty(Exchange.CHARSET_NAME, charsetName);
        }

        exchange.setProperty("filename", job.getFilename());
        exchange.setProperty("pageIndex", job.getPageIndex());
        exchange.setProperty("jobId", job.getId());
        exchange.setProperty("uuid", job.getUuid());

        return new ByteArrayInputStream(job.getQuery().getBytes(charsetName));
    }




}
