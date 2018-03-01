/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.console.ds;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.lib.sqlcommand.DataCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;

/**
 * Maintains the tokens generated when the "Lost password use case" is executed.
 *
 * @author Mauricio Pazos
 *
 */
public class UserTokenDao {

    private static final Log LOG = LogFactory.getLog(UserTokenDao.class.getName());

    @Autowired
    private DataSource dataSource;

    /**
     * Inserts the new association uid-token.
     *
     * @param uid
     *            user identifier
     * @param token
     *            token
     * @throws DataServiceException
     */
    public void insertToken(String uid, String token) throws DataServiceException {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Timestamp currentDay = new Timestamp(date.getTime());

        Map<String, Object> row = new HashMap<String, Object>(3);
        row.put(DatabaseSchema.UID_COLUMN, uid);
        row.put(DatabaseSchema.TOKEN_COLUMN, token);
        row.put(DatabaseSchema.CREATION_DATE_COLUMN, currentDay);

        InsertUserTokenCommand cmd = new InsertUserTokenCommand();
        cmd.setRowValues(row);
        executeCmd(cmd, "Failed to insert the uid,token");
    }

    /**
     * Searches the user_token association which match with the provided token.
     *
     * @param token
     * @return uid
     *
     * @throws DataServiceException
     * @throws NameNotFoundException
     */
    public String findUserByToken(String token) throws DataServiceException, NameNotFoundException {
        QueryByTokenCommand cmd = new QueryByTokenCommand();
        cmd.setToken(token);
        executeCmd(cmd, "UserTokenDao.findUserByToken");

        List<Map<String, Object>> result = cmd.getResult();

        if (result.isEmpty()) {
            throw new NameNotFoundException("the token " + token + " wasn't found.");
        }

        String uid = (String) result.get(0).get(DatabaseSchema.UID_COLUMN);
        return uid;
    }

    public List<Map<String, Object>> findBeforeDate(Date expired) throws DataServiceException {
        QueryUserTokenExpiredCommand cmd = new QueryUserTokenExpiredCommand();
        cmd.setBeforeDate(expired);
        executeCmd(cmd, "UserTokenDao.delete");

        List<Map<String, Object>> result = cmd.getResult();
        return result;
    }

    public boolean exist(String uid) throws DataServiceException {
        QueryByUidCommand cmd = new QueryByUidCommand();
        cmd.setUid(uid);
        executeCmd(cmd, "UserTokenDao.exist");

        List<Map<String, Object>> result = cmd.getResult();
        return !result.isEmpty();
    }

    public void delete(String uid) throws DataServiceException {
        DeleteUserTokenCommand cmd = new DeleteUserTokenCommand();
        cmd.setUid(uid);
        executeCmd(cmd, "UserTokenDao.delete");
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void executeCmd (DataCommand cmd, String logMsg) throws DataServiceException {
        Connection c = null;
        try {
            c = dataSource.getConnection();
            cmd.setConnection(c);
            cmd.execute();
        } catch (Exception e) {
            LOG.error(logMsg, e);
            throw new DataServiceException(e);
        } finally {
            closeQuietly(c);
        }
    }

    private static void closeQuietly(Connection connection)
    {
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (Exception e)
        {
            LOG.error("Unable to close the connection, it doesn't mean that the sql statement failed.");
        }
    }
}
