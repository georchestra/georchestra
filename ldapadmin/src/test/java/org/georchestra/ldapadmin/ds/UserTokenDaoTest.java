package org.georchestra.ldapadmin.ds;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UserTokenDaoTest {

    private UserTokenDao userTokenDao;
    private DataSource dataSource = Mockito.mock(DataSource.class);
    private Connection connection = Mockito.mock(Connection.class);
    private PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

    @Before
    public void setUp() throws Exception {
        userTokenDao = new UserTokenDao();
        userTokenDao.setDataSource(dataSource);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
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
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        userTokenDao.insertToken("123456", "myToken");

        boolean nothingRaised = false;
        try {
            userTokenDao.insertToken("123456", "myToken");
            nothingRaised = true;
        } catch (Throwable e) {}

        assertTrue(nothingRaised);
    }

    @Test
    public void findUserByTokenTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true, true, false);
        Mockito.when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("pmauduit", "fvanderbiest");
        Mockito.when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        Mockito.when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        Mockito.when(preparedStatement.executeQuery()).thenReturn(rs);

        String ret = userTokenDao.findUserByToken("abcde");

        assertTrue(ret.equals("pmauduit"));

    }

    @Test
    public void findBeforeDateTest() throws Exception {
        // Testing the regular case
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true, true, false);
        Mockito.when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("1", "2");
        Mockito.when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        Mockito.when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        Mockito.when(preparedStatement.executeQuery()).thenReturn(rs);

        List<Map<String, Object>> ret = userTokenDao.findBeforeDate(new Date());
        assertTrue(ret.get(0).size() == 3);
        assertTrue(ret.get(0).get(DatabaseSchema.UID_COLUMN).equals("1"));
        assertTrue(ret.get(1).get(DatabaseSchema.UID_COLUMN).equals("2"));
        assertTrue(ret.get(0).get(DatabaseSchema.TOKEN_COLUMN).equals("mytoken1"));
        assertTrue(ret.get(1).get(DatabaseSchema.TOKEN_COLUMN).equals("mytoken2"));
        assertTrue(ret.get(0).get(DatabaseSchema.CREATION_DATE_COLUMN).equals(new Timestamp(1234)));
        assertTrue(ret.get(1).get(DatabaseSchema.CREATION_DATE_COLUMN).equals(new Timestamp(5678)));


    }

    @Test
    public void existTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true, true, false);
        Mockito.when(rs.getString(DatabaseSchema.UID_COLUMN)).thenReturn("1", "2");
        Mockito.when(rs.getString(DatabaseSchema.TOKEN_COLUMN)).thenReturn("mytoken1", "mytoken2");
        Mockito.when(rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN)).thenReturn(new Timestamp(1234), new Timestamp(5678));
        Mockito.when(preparedStatement.executeQuery()).thenReturn(rs);

        boolean ret = userTokenDao.exist("42");

        assertTrue(ret);

        // Same test with no result
        Mockito.when(rs.next()).thenReturn(false);

        ret = userTokenDao.exist("42");

        assertFalse(ret);
    }

    @Test
    public void deleteTest() throws Exception {
        // No element deleted
        try {
            userTokenDao.delete("54");
        } catch (Throwable e) {
            assertTrue (e instanceof DataServiceException);
        }

        // normal case
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean nothingRaised = false;

        try {
            userTokenDao.delete("54");
            nothingRaised = true;
        } catch (Throwable e) {}

        assertTrue(nothingRaised);
    }
}
