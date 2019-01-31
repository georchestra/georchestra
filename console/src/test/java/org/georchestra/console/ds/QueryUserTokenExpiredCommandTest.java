package org.georchestra.console.ds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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

import org.georchestra.lib.sqlcommand.DataCommandException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class QueryUserTokenExpiredCommandTest {

    private DataSource mockDS;
    private QueryUserTokenExpiredCommand query;
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet resultSet;

    @Before
    public void setUp() throws Exception {
        mockDS = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        
        when(mockDS.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        
        when(pstmt.executeQuery()).thenReturn(resultSet);

        Mockito.when(resultSet.getString(Mockito.anyString())).thenReturn("uid", "token");
        Mockito.when(resultSet.getTimestamp(Mockito.anyString())).thenReturn(new Timestamp(0));
        Mockito.when(resultSet.next()).thenReturn(true, false);

        
        query = new QueryUserTokenExpiredCommand();
        query.setDataSource(mockDS);
        query.setBeforeDate(new Date());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPrepareStatement() throws SQLException {
        PreparedStatement pstmt = query.prepareStatement(connection);
        // Well, these objects have been mocked, this test
        // just ensures that everything went well.
        assertTrue(pstmt != null);
    }

    @Test
    public void testGetRow() throws SQLException {


        Map<String,Object> ret = query.getRow(resultSet);

        assertEquals(ret.size(), 3);
        assertEquals(ret.get(DatabaseSchema.UID_COLUMN), "uid");
        assertEquals(ret.get(DatabaseSchema.TOKEN_COLUMN), "token");
        assertEquals(0L, ((Timestamp) ret.get(DatabaseSchema.CREATION_DATE_COLUMN)).getTime());

    }

    @Test
    public void testExecuteNoConnection() throws DataCommandException {
        query.setDataSource(null);
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
        query.setDataSource(mockDS);
        query.execute();
        List<Map<String, Object>> ret = query.getResult();

        // We should retrieve the mocked objects
        // [{creation_date=1970-01-01 01:00:00.0, uid=uid, token=token}]
        assertEquals(0L, ((Timestamp) ret.get(0).get(DatabaseSchema.CREATION_DATE_COLUMN)).getTime());
        assertEquals(ret.get(0).get(DatabaseSchema.UID_COLUMN), "uid");
        assertEquals(ret.get(0).get(DatabaseSchema.TOKEN_COLUMN),"token");
        assertEquals(ret.get(0).size(), 3);
    }


}
