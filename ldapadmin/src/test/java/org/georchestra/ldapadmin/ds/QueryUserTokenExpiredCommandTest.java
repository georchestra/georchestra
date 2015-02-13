package org.georchestra.ldapadmin.ds;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.georchestra.lib.sqlcommand.DataCommandException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class QueryUserTokenExpiredCommandTest {

    QueryUserTokenExpiredCommand query = new QueryUserTokenExpiredCommand();
    Connection c = Mockito.mock(Connection.class);
    PreparedStatement pstmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    @Before
    public void setUp() throws Exception {
        query.setConnection(c);
        Mockito.when(c.prepareStatement(Mockito.anyString())).thenReturn(pstmt);
        query.setBeforeDate(new Date());
        Mockito.when(pstmt.executeQuery()).thenReturn(resultSet);
        Map<String,Object> map = new HashMap<String,Object>();
        Mockito.when(resultSet.getString(Mockito.anyString())).thenReturn("uid", "token");
        Mockito.when(resultSet.getTimestamp(Mockito.anyString())).thenReturn(new Timestamp(0));
        Mockito.when(resultSet.next()).thenReturn(true, false);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPrepareStatement() throws SQLException {
        PreparedStatement pstmt = query.prepareStatement();
        // Well, these objects have been mocked, this test
        // just ensures that everything went well.
        assertTrue(pstmt != null);
    }

    @Test
    public void testGetRow() throws SQLException {


        Map<String,Object> ret = query.getRow(resultSet);

        assertTrue(ret.size() == 3);
        assertTrue(ret.get(DatabaseSchema.UID_COLUMN).equals("uid"));
        assertTrue(ret.get(DatabaseSchema.TOKEN_COLUMN).equals("token"));
        assertTrue(ret.get(DatabaseSchema.CREATION_DATE_COLUMN).toString().equals("1970-01-01 01:00:00.0"));

    }

    @Test
    public void testExecuteNoConnection() throws DataCommandException {
        query.setConnection(null);
        try {
            query.execute();
        } catch (Throwable e) {
            // Note: if launched under Eclipse, do not forget
            // to add "-ea" as VM arguments in launch / debug parameters,
            // to activate builtin java assertions.
            assertTrue(e instanceof AssertionError);
        }
    }

    @Test
    public void testExecuteAndGetResult() throws DataCommandException {
        query.setConnection(c);
        query.execute();
        List<Map<String, Object>> ret = query.getResult();

        // We should retrieve the mocked objects
        // [{creation_date=1970-01-01 01:00:00.0, uid=uid, token=token}]
        assertTrue(ret.get(0).get(DatabaseSchema.CREATION_DATE_COLUMN).toString().equals("1970-01-01 01:00:00.0"));
        assertTrue(ret.get(0).get(DatabaseSchema.UID_COLUMN).equals("uid"));
        assertTrue(ret.get(0).get(DatabaseSchema.TOKEN_COLUMN).equals("token"));
        assertTrue(ret.get(0).size() == 3);
    }


}
