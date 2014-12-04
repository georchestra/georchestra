package org.georchestra.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.util.Assert;

import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * Remove the x-forwarded-* headers for selected servers.
 *
 * @author Jesse on 12/2/2014.
 */
public class RemoveXForwardedHeaders implements HeaderFilter {
    protected static final Log logger = LogFactory.getLog(Proxy.class);
    static final String HOST = "x-forwarded-host";
    static final String PORT = "x-forwarded-port";
    static final String PROTOCOL = "x-forwarded-proto";
    static final String FOR = "x-forwarded-for";

    private List<Pattern> includes = Lists.newArrayList();
    private List<Pattern> excludes = Lists.newArrayList();

    @PostConstruct
    @VisibleForTesting
    void checkConfiguration() {
        Assert.isTrue(includes.isEmpty() || excludes.isEmpty(), "Only includes or excludes can be defined not both.");
    }

    @Override
    public boolean filter(String headerName, HttpServletRequest originalRequest, HttpRequestBase proxyRequest) {
        if (!headerName.equalsIgnoreCase(HOST) && !headerName.equalsIgnoreCase(PORT)
                && !headerName.equalsIgnoreCase(PROTOCOL) && !headerName.equalsIgnoreCase(FOR)) {
            return false;
        }

        final String url = originalRequest.getRequestURL().toString();
        boolean removeHeader = false;
        if (!includes.isEmpty()) {
            logger.debug("Checking requestURL: '"+url+"' against include patterns: " + this.includes);
            removeHeader = false;
            for (Pattern include : includes) {
                if (include.matcher(url).matches()) {
                    removeHeader = true;
                    break;
                }
            }
        } else if (!excludes.isEmpty()) {
            logger.debug("Checking requestURL: '"+url+"' against exclude patterns: " + this.excludes);
            removeHeader = true;
            for (Pattern exclude : excludes) {
                if (exclude.matcher(url).matches()) {
                    removeHeader = false;
                }
            }
        }

        if (removeHeader) {
            logger.debug("Removing header: " + headerName);
        } else {
            logger.debug("Keeping header: " + headerName);
        }
        return removeHeader;
    }

    public void setIncludes(List<String> includes) {
        for (String include : includes) {
            if (!include.startsWith("(?")) {
                include = "(?i)" + include;
            }
            this.includes.add(Pattern.compile(include));
        }
    }

    public void setExcludes(List<String> excludes) {
        for (String exclude : excludes) {
            if (!exclude.startsWith("(?")) {
                exclude = "(?i)" + exclude;
            }
            this.excludes.add(Pattern.compile(exclude));
        }
    }
}
