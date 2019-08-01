package org.georchestra.console.ws.utils.converter;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvMessageConverter extends AbstractHttpMessageConverter<String> {
    public static final MediaType MEDIA_TYPE = new MediaType("text", "csv", StandardCharsets.UTF_8);

    public CsvMessageConverter() {
        super(MEDIA_TYPE);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    protected void writeInternal(String response, HttpOutputMessage output)
            throws IOException, HttpMessageNotWritableException {
        try(OutputStream out = output.getBody()) {
            out.write(response.getBytes());
        }
    }

    @Override
    protected String readInternal(Class<? extends String> request,
                                   HttpInputMessage input) throws IOException,
            HttpMessageNotReadableException {
        InputStream in = input.getBody();
        return new InputStreamReader(in).toString();
    }
}