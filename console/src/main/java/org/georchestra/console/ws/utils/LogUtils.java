package org.georchestra.console.ws.utils;

import java.util.Date;

import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.model.AdminLogEntry;
import org.georchestra.console.model.AdminLogType;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogUtils {
    @Autowired
    private AdminLogDao logDao;

    public void setLogDao(AdminLogDao logDao) {
        this.logDao = logDao;
    }

    private static final Log LOG = LogFactory.getLog(LogUtils.class.getName());

    /**
     * Create log to save and display.
     * 
     * @target String to identify org's
     * @type type AdminLogType of log event
     * @param values String that represent changed attributes
     */
    public AdminLogEntry createLog(String target, AdminLogType type, String values) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        AdminLogEntry log = new AdminLogEntry();

        if (auth != null && auth.getName() != null && target != null) {
            String admin = auth.getName();
            // case where we don't need to log changes
            if (values == null || values.isEmpty()) {
                log = new AdminLogEntry(admin, target, type, new Date());
            } else {
                log = new AdminLogEntry(admin, target, type, new Date(), values);
            }
            if (logDao != null) {
                try {
                    logDao.save(log);
                } catch (DataIntegrityViolationException divex) {
                    // Value could be to large for field size
                    LOG.error("Could not save changed values for admin log, reset value : " + values, divex);
                    JSONObject errorsjson = new JSONObject();
                    errorsjson.put("error",
                            "Error while inserting admin log in database, see admin log file for more information");
                    log.setChanged(errorsjson.toString());
                    logDao.save(log);
                }

            }
        } else {
            LOG.info("Authentification Security Context is null.");
            log = null;
        }
        return log;
    }

    /**
     * Return JSONObject from informations to be save as log detail into database.
     * This allow to modify details before create log.
     * 
     * @param attributeName String
     * @param oldValue      String
     * @param newValue      String
     * @param type          AdminLogType
     * @return JSONObject
     */
    public JSONObject getLogDetails(String attributeName, String oldValue, String newValue, AdminLogType type) {
        JSONObject details = new JSONObject();
        details.put("field", attributeName != null ? attributeName : "");
        details.put("old", oldValue != null ? oldValue : "");
        details.put("new", newValue != null ? newValue : "");
        details.put("type", type != null ? type.toString() : "");
        return details;
    }

    /**
     * Full creation by log details creation and log creation directly.
     * 
     * @param target        String
     * @param attributeName String
     * @param oldValue      String
     * @param newValue      String
     * @param type          AdminLogType that could be any type of log, use to
     */
    public void createAndLogDetails(String target, String attributeName, String oldValue, String newValue,
            AdminLogType type) {
        JSONObject details = getLogDetails(attributeName, oldValue, newValue, type);
        this.createLog(target, type, details.toString());
    }
}
