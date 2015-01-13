package org.georchestra.ldapadmin.ds;

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
import org.springframework.beans.factory.annotation.Autowired;

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
        Connection c = null;
        try {
            InsertUserTokenCommand cmd = new InsertUserTokenCommand();
            c = dataSource.getConnection();
            cmd.setConnection(c);

            Map<String, Object> row = new HashMap<String, Object>(3);
            row.put(DatabaseSchema.UID_COLUMN, uid);
            row.put(DatabaseSchema.TOKEN_COLUMN, token);

            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            Timestamp currentDay = new Timestamp(date.getTime());
            row.put(DatabaseSchema.CREATION_DATE_COLUMN, currentDay);

            cmd.setRowValues(row);
            cmd.execute();
        } catch (Exception e) {
            LOG.error("Failed to insert the uid,token", e);
            throw new DataServiceException(e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    LOG.error("Unable to close the connection to the database.");
                    throw new DataServiceException(e);
                }
            }
        }
    }

    /**
     * Searches the user_token association which match with the provided token.
     *
     * @param token
     * @return uid
     *
     * @throws DataServiceException
     * @throws NotFoundException
     */
    public String findUserByToken(String token) throws DataServiceException, NotFoundException {
        Connection c = null;
        try {
            c = dataSource.getConnection();
            QueryByTokenCommand cmd = new QueryByTokenCommand();

            cmd.setConnection(c);

            cmd.setToken(token);
            cmd.execute();

            List<Map<String, Object>> result = cmd.getResult();

            if (result.isEmpty()) {
                throw new NotFoundException("the token " + token + " wasn't found.");
            }

            String uid = (String) result.get(0).get(DatabaseSchema.UID_COLUMN);

            return uid;

        } catch (Exception e) {
            throw new DataServiceException(e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    LOG.error("Unable to close the connection to the database.");
                    throw new DataServiceException(e);
                }
            }
        }
    }

    public List<Map<String, Object>> findBeforeDate(Date expired) throws DataServiceException {
        Connection c = null;
        try {
            QueryUserTokenExpiredCommand cmd = new QueryUserTokenExpiredCommand();

            c = dataSource.getConnection();
            cmd.setConnection(c);

            cmd.setBeforeDate(expired);
            cmd.execute();

            List<Map<String, Object>> result = cmd.getResult();

            return result;

        } catch (Exception e) {
            throw new DataServiceException(e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    LOG.error("Unable to close the connection to the database.");
                    throw new DataServiceException(e);
                }
            }
        }
    }

    public boolean exist(String uid) throws DataServiceException {
        Connection c = null;
        try {
            QueryByUidCommand cmd = new QueryByUidCommand();
            c = dataSource.getConnection();
            cmd.setConnection(c);

            cmd.setUid(uid);
            cmd.execute();

            List<Map<String, Object>> result = cmd.getResult();

            return !result.isEmpty();

        } catch (Exception e) {
            throw new DataServiceException(e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    LOG.error("Unable to close the connection to the database.");
                    throw new DataServiceException(e);
                }
            }
        }

    }

    public void delete(String uid) throws DataServiceException {
        Connection c = null;
        try {
            DeleteUserTokenCommand cmd = new DeleteUserTokenCommand();
            c = dataSource.getConnection();
            cmd.setConnection(c);

            cmd.setUid(uid);

            cmd.execute();

        } catch (Exception e) {
            LOG.error("Failed to insert the uid,token", e);
            throw new DataServiceException(e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    LOG.error("Unable to close the connection to the database.");
                    throw new DataServiceException(e);
                }
            }
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
