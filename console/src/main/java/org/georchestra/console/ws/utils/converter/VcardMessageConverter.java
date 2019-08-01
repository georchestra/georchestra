package org.georchestra.console.ws.utils.converter;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class VcardMessageConverter extends AbstractHttpMessageConverter<String> {
    public static final MediaType MEDIA_TYPE = new MediaType("text", "x-vcard", StandardCharsets.UTF_8);

    public VcardMessageConverter() {
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
