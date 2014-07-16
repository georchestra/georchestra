package org.georchestra.extractorapp.ws.acceptance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.extractorapp.ws.extractor.task.ExtractionTask;

public class CheckFormAcceptance {

    private BasicDataSource basicDataSource;
    private boolean activated = false;

    private static final Log LOG = LogFactory.getLog(ExtractionTask.class
            .getPackage().getName());

    private final static String CHECK_FORM_ACCEPTANCE_QUERY = "SELECT "
            + "           COUNT(id) "
            + "FROM "
            + "           downloadform.extractorapp_log "
            + "WHERE "
            + "           (username = null AND sessionid = ?) OR (username = ?  AND username IS NOT null) "
            + "AND " + "           json_spec = ?;";

    public CheckFormAcceptance(boolean _activated, String jdbcUrl) {

        activated = _activated;

        if (activated) {
            basicDataSource = new BasicDataSource();

            basicDataSource.setDriverClassName("org.postgresql.Driver");

            basicDataSource.setTestOnBorrow(true);

            basicDataSource.setPoolPreparedStatements(true);
            basicDataSource.setMaxOpenPreparedStatements(-1);

            basicDataSource.setDefaultReadOnly(false);
            basicDataSource.setDefaultAutoCommit(false);

            basicDataSource.setUrl(jdbcUrl);
        }
    }

    public boolean isFormAccepted(String session, String username,
            String jsonSpec) {

        if (activated == false)
            return true;

        Connection connection = null;
        PreparedStatement checkformentryst = null;
        ResultSet rs = null;

        try {
            connection = basicDataSource.getConnection();

            checkformentryst = connection
                    .prepareStatement(CHECK_FORM_ACCEPTANCE_QUERY);
            // The available sessionid is not a stable identifier across
            // security-proxified webapps,
            // and it is not available in case of anonymous extraction requests.
            // As a result, it is not used to check if the user actually
            // validated the form.
            checkformentryst.setString(1, session);
            checkformentryst.setString(2, username);
            // Extra \n to be removed with the trim() call
            checkformentryst.setString(3, jsonSpec.trim());

            rs = checkformentryst.executeQuery();

            rs.next();
            int numResults = rs.getInt(1);

            return (numResults > 0);

        } catch (Exception e) {
            LOG.error("Error occured while trying to check form validation", e);
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    LOG.error(
                            "Error occured while trying to close the resultset: ",
                            e);
                }
            }
            if (checkformentryst != null) {
                try {
                    checkformentryst.close();
                } catch (Exception e) {
                    LOG.error(
                            "Error occured while trying to close the SQL statement: ",
                            e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    LOG.error(
                            "Error occured while trying to close the SQL connection: ",
                            e);
                }
            }
        }
    }

}
