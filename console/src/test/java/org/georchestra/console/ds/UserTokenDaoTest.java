/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.georchestra.ds.DataServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;

public class UserTokenDaoTest {

    private UserTokenDao userTokenDao;
    private DataSource dataSource = Mockito.mock(DataSource.class);
    private Connection connection = Mockito.mock(Connection.class);
    private PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

    @Before
    public void setUp() throws Exception {
        userTokenDao = new UserTokenDao();
        userTokenDao.setDataSource(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void insertTokenTest() throws Exception {
        // in the default Mocked object,
        // the insertion in db returns 0, which should
        // throw a DataServiceException.
        try {
            userTokenDao.insertToken("123456", "myToken");
        } catch (Throwable e) {
            System.out.println(e);
            assertTrue(e instanceof DataServiceException);
        }

        // Doing the same call, but mocking the insertion
        // so that it returns 1 line inserted in db
        when(preparedStatement.executeUpdate()).thenReturn(1);
        userTokenDao.insertToken("123456", "myToken");

        boolean nothingRaised = false;
        try {
            userTokenDao.insertToken("123456", "myToken");
            nothingRaised = true;
        } catch (Throwable e) {
        }

        assertTrue(nothingRaised);
    }

    @Test
    public void findUidTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("pmauduit", "fvanderbiest");
        when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        when(rs.getString(DatabaseSchema.ADDITIONAL_INFO)).thenReturn(null, "additionalInfo");
        when(preparedStatement.executeQuery()).thenReturn(rs);

        String ret = userTokenDao.findUidWithoutAdditionalInfo("abcde");

        assertTrue(ret.equals("pmauduit"));
    }

    @Test(expected = NameNotFoundException.class)
    public void findUidUnexpectedAdditionalInfoTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("pmauduit", "fvanderbiest");
        when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        when(rs.getString(DatabaseSchema.ADDITIONAL_INFO)).thenReturn("additionalInfo", "additionalInfo");
        when(preparedStatement.executeQuery()).thenReturn(rs);

        userTokenDao.findUidWithoutAdditionalInfo("abcde");
    }

    @Test
    public void findAdditionalInfoTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("pmauduit", "fvanderbiest");
        when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        when(rs.getString(DatabaseSchema.ADDITIONAL_INFO)).thenReturn("additionalInfo", null);
        when(preparedStatement.executeQuery()).thenReturn(rs);

        String ret = userTokenDao.findAdditionalInfo("pmauduit", "abcde");

        assertTrue(ret.equals("additionalInfo"));
    }

    @Test(expected = NameNotFoundException.class)
    public void findAdditionalInfoWrongUidTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("pmauduit", "fvanderbiest");
        when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        when(rs.getString(DatabaseSchema.ADDITIONAL_INFO)).thenReturn("additionalInfo", null);
        when(preparedStatement.executeQuery()).thenReturn(rs);

        userTokenDao.findAdditionalInfo("wrong_uid", "abcde");
    }

    @Test(expected = NameNotFoundException.class)
    public void findAdditionalInfoMissingInfoTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("pmauduit", "fvanderbiest");
        when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        when(rs.getString(DatabaseSchema.ADDITIONAL_INFO)).thenReturn(null, null);
        when(preparedStatement.executeQuery()).thenReturn(rs);

        userTokenDao.findAdditionalInfo("pmauduit", "abcde");
    }

    @Test
    public void findBeforeDateTest() throws Exception {
        // Testing the regular case
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("1", "2");
        when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        when(rs.getString(DatabaseSchema.ADDITIONAL_INFO)).thenReturn("additionalInfo", null);
        when(preparedStatement.executeQuery()).thenReturn(rs);

        List<Map<String, Object>> ret = userTokenDao.findBeforeDate(new Date());
        assertTrue(ret.get(0).size() == 4);
        assertTrue(ret.get(0).get(DatabaseSchema.UID_COLUMN).equals("1"));
        assertTrue(ret.get(1).get(DatabaseSchema.UID_COLUMN).equals("2"));
        assertTrue(ret.get(0).get(DatabaseSchema.TOKEN_COLUMN).equals("mytoken1"));
        assertTrue(ret.get(1).get(DatabaseSchema.TOKEN_COLUMN).equals("mytoken2"));
        assertTrue(ret.get(0).get(DatabaseSchema.CREATION_DATE_COLUMN).equals(new Timestamp(1234)));
        assertTrue(ret.get(1).get(DatabaseSchema.CREATION_DATE_COLUMN).equals(new Timestamp(5678)));
        assertTrue(ret.get(0).get(DatabaseSchema.ADDITIONAL_INFO).equals("additionalInfo"));
        assertTrue(ret.get(1).get(DatabaseSchema.ADDITIONAL_INFO) == null);

    }

    @Test
    public void existTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("1", "2");
        when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        when(rs.getString(DatabaseSchema.ADDITIONAL_INFO)).thenReturn("additionalInfo", null);
        when(preparedStatement.executeQuery()).thenReturn(rs);

        boolean ret = userTokenDao.exist("42");

        assertTrue(ret);

        // Same test with no result
        when(rs.next()).thenReturn(false);

        ret = userTokenDao.exist("42");

        assertFalse(ret);
    }

    @Test
    public void deleteTest() throws Exception {
        // No element deleted
        try {
            userTokenDao.delete("54");
        } catch (Throwable e) {
            assertTrue(e instanceof DataServiceException);
        }

        // normal case
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean nothingRaised = false;

        try {
            userTokenDao.delete("54");
            nothingRaised = true;
        } catch (Throwable e) {
        }

        assertTrue(nothingRaised);
    }

    @Test
    public void closeConnectionExpeptionDoNotOverrideExecuteException() throws SQLException {
        doThrow(new RuntimeException("sql error, for test")).when(preparedStatement).executeUpdate();
        doThrow(new RuntimeException("already closed, for test")).when(connection).close();
        try {
            userTokenDao.insertToken("123456", "myToken");
        } catch (Exception e) {
            assertEquals("sql error, for test", e.getMessage());
        }
    }
}
