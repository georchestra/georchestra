package extractorapp.ws.extractor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import extractorapp.ws.extractor.task.ExtractionManager;

/**
 * This is a bean that starts a timer in the startup method.  When the timer task is run (this)
 * all files in the archive storage directory are checked and the expired elements are deleted.
 * 
 * @author jeichar
 */
public class ExpiredArchiveDaemon extends TimerTask implements FilenameFilter {

    private static final Log    LOG = LogFactory.getLog(ExpiredArchiveDaemon.class.getPackage().getName());
    private static final long   SECOND = 1000;
    private static final long   MINUTE = 60 * SECOND;
    private static final long   DAYS   = 24 * MINUTE;

    private long                period = 10 * MINUTE;
    private long                expiry = 10 * DAYS;
    private ExtractionManager extractionManager;

    /**
     * This is the init-method in the spring configuration file so it
     * is called by spring when the bean is configured.
     */
    public void startup() {
        LOG.info(getClass().getName() + " starting up with an interval of " + (period/MINUTE)  + " minutes and expiry of "+(expiry/DAYS)+" days");
        Timer timer = new Timer(getClass().getSimpleName(), true);
        timer.scheduleAtFixedRate(this, period, period);
    }

    @Override
    public void run() {
        
        LOG.debug(getClass().getName() + " performing sweep");
        File storageFile = FileUtils.storageFile("");

        if(!storageFile.exists()) return;

        extractionManager.cleanExpiredTasks(expiry);
        
        for (File f : storageFile.listFiles(this)) {
            if (f.lastModified() > expiry+System.currentTimeMillis()) {
                if (f.delete()) {
                    LOG.info("Deleted expired archive: " + f.getName());
                } else {
                    LOG.warn("Unable to delete expired archive: " + f.getName());
                }
            }
        }
    }

    /**
     * The number of days after which the archive is considered expired. Defaults
     * to 10
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

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(ExtractorController.EXTRACTION_ZIP_EXT);
    }

	public void setExtractionManager(ExtractionManager extractionManager) {
		this.extractionManager = extractionManager;
	}

}
