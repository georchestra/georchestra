/*
 * Copyright (C) 2009 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.extractorapp.ws.extractor.task.ExtractionManager;

import javax.annotation.PostConstruct;

/**
 * This is a bean that starts a timer in the startup method. When the timer task
 * is run (this) all files in the archive storage directory are checked and the
 * expired elements are deleted.
 *
 * @author jeichar
 */
public class ExpiredArchiveDaemon extends TimerTask implements FilenameFilter {

    private static final Log LOG = LogFactory.getLog(ExpiredArchiveDaemon.class.getPackage().getName());
    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAYS = 24 * HOUR;

    private long period = 10 * MINUTE;
    private long expiry = 10 * DAYS;
    private ExtractionManager extractionManager;

    /**
     * This is the init-method in the spring configuration file so it is called by
     * spring when the bean is configured.
     */
    @PostConstruct
    public void startup() {
        LOG.info(getClass().getName() + " starting up with an interval of " + (period / MINUTE)
                + " minutes and expiry of " + (expiry / DAYS) + " days");
        Timer timer = new Timer(getClass().getSimpleName(), true);
        timer.scheduleAtFixedRate(this, period, period);
    }

    @Override
    public void run() {

        LOG.debug(getClass().getName() + " performing sweep");
        File storageFile = FileUtils.storageFile("");

        if (!storageFile.exists())
            return;

        extractionManager.cleanExpiredTasks(expiry);

        for (File f : storageFile.listFiles(this)) {
            if (f.lastModified() + expiry < System.currentTimeMillis()) {
                if (f.delete()) {
                    LOG.info("Deleted expired archive: " + f.getName());
                } else {
                    LOG.warn("Unable to delete expired archive: " + f.getName());
                }
            }
        }
    }

    /**
     * The number of days after which the archive is considered expired. Defaults to
     * 10
     */
    public void setExpiry(long expiry) {
        this.expiry = expiry * DAYS;
    }

    public long getExpiry() {
        return this.expiry / DAYS;
    }

    /**
     * The number of minutes between sweeps checking the expiration of the files
     * Default is 10 minutes
     */
    public void setPeriod(long period) {
        this.period = period * MINUTE;
    }

    public long getPeriod() {
        return this.period / MINUTE;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(ExtractorController.EXTRACTION_ZIP_EXT);
    }

    public void setExtractionManager(ExtractionManager extractionManager) {
        this.extractionManager = extractionManager;
    }

}
