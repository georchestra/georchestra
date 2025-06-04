/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.security;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.util.Assert;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

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

        final String url = proxyRequest.getURI().toString();
        boolean removeHeader = false;
        if (!includes.isEmpty()) {
            logger.debug("Checking requestURL: '" + url + "' against include patterns: " + this.includes);
            removeHeader = false;
            for (Pattern include : includes) {
                if (include.matcher(url).matches()) {
                    removeHeader = true;
                    break;
                }
            }
        } else if (!excludes.isEmpty()) {
            logger.debug("Checking requestURL: '" + url + "' against exclude patterns: " + this.excludes);
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
        this.includes = extractPatternFromStrings(includes);
    }

    public List<Pattern> getIncludes() {
        return this.includes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = extractPatternFromStrings(excludes);
    }

    private List<Pattern> extractPatternFromStrings(List<String> patterns) {
        return patterns.stream().map(s -> s.split(",")).flatMap(Arrays::stream).map(pattern -> {
            if (!pattern.startsWith("(?")) {
                pattern = "(?i)" + pattern;
            }
            return Pattern.compile(pattern);
        }).collect(Collectors.toList());
    }
}
