package org.georchestra.security;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class UrlFormEncodedPostFilter extends OncePerRequestFilter {
    public void destroy() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getMethod().equalsIgnoreCase(HttpMethod.POST.name()) && isFormContentType(request)) {
            // Morph the request to a WrappedRequest, as this will
            // allow us to read the body content in the Proxy.java class once again.
            request = new WrappedRequest(request);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns if the request is a POST x-www-form-urlencoded or not.
     *
     * @return true if this is the case, else false.
     *
     */
    private boolean isFormContentType(HttpServletRequest request) {
        if (request.getContentType() == null) {
            return false;
        }
        String contentType = request.getContentType().split(";")[0].trim();

        return "application/x-www-form-urlencoded".equalsIgnoreCase(contentType);
    }

    private static class WrappedRequest extends HttpServletRequestWrapper {
        byte[] content = new byte[0];
        private static Logger Logger = LoggerFactory.getLogger(WrappedRequest.class);

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request
         * @throws IllegalArgumentException if the request is null
         */
        public WrappedRequest(HttpServletRequest request) {
            super(request);
            String charset = request.getCharacterEncoding();
            try {
                Charset.forName(charset);
            } catch (Throwable t) {
                charset = "UTF-8";
            }
            try {
                String payload = IOUtils.toString(request.getInputStream(), charset);
                this.content = payload.getBytes();
            } catch (IOException e) {
                Logger.error("Unable to extract body payload", e);
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CachedContentInputStream(this.content);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.content);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream));
        }

    }
}
