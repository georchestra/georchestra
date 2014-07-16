package org.georchestra.dlform ;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;

public class DataUsageTest {
    private DataUsage dataUsageController;

    DataSource ds = Mockito.mock(DataSource.class);
    Connection mockedConnection = Mockito.mock(Connection.class);
    Statement mockedStatement = Mockito.mock(Statement.class);
    ResultSet mockedResultset = Mockito.mock(ResultSet.class);
    ResultSetMetaData mockedRsMd = Mockito.mock(ResultSetMetaData.class);


    @Before
    public void setUp() throws SQLException {
        Mockito.when(ds.getConnection()).thenReturn(mockedConnection);
        Mockito.when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        Mockito.when(mockedStatement.executeQuery("SELECT * FROM downloadform.data_use"))
               .thenReturn(mockedResultset);
        Mockito.when(mockedResultset.getMetaData()).thenReturn(mockedRsMd);

        Mockito.when(mockedResultset.next()).thenReturn(true, true, false);

        Mockito.when(mockedRsMd.getColumnLabel(1)).thenReturn("id");
        Mockito.when(mockedRsMd.getColumnLabel(2)).thenReturn("name");

        Mockito.when(mockedResultset.getString(1)).thenReturn("1", "2");
        Mockito.when(mockedResultset.getString(2)).thenReturn("Gestion domaine public", "Formation");

        dataUsageController = new DataUsage(ds, true);
    }

    @Test
    public void testDeactivatedDataUsage() throws Exception {
        dataUsageController.setActivated(false);
        MockHttpServletResponse mockedResp = new MockHttpServletResponse();

        dataUsageController.handleRequest(null, mockedResp);

        JSONObject ret = new JSONObject(mockedResp.getContentAsString());
        assertTrue(ret.get("status").equals("unavailable"));
        assertTrue(ret.get("reason").equals("downloadform disabled"));
    }


    @Test
    public void testDataUsage() throws Exception {
        dataUsageController.setActivated(true);
        MockHttpServletResponse mockedResp = new MockHttpServletResponse();

        dataUsageController.handleRequest(null, mockedResp);

        JSONObject ret = new JSONObject(mockedResp.getContentAsString());
        JSONObject firstline = (JSONObject) ((JSONArray) ret.get("rows")).get(0);
        JSONObject secondline = (JSONObject) ((JSONArray) ret.get("rows")).get(1);
        assertTrue(firstline.get("id").equals("1"));
        assertTrue(firstline.get("name").equals("Gestion domaine public"));
        assertTrue(secondline.get("id").equals("2"));
        assertTrue(secondline.get("name").equals("Formation"));

    }

    @Test
    public void testDataUsageWithNullResult() throws Exception {
        dataUsageController.setActivated(true);
        MockHttpServletResponse mockedResp = new MockHttpServletResponse();
        Mockito.when(mockedStatement.executeQuery("SELECT * FROM downloadform.data_use"))
        .thenReturn(null);

        dataUsageController.handleRequest(null, mockedResp);

        JSONObject ret = new JSONObject(mockedResp.getContentAsString());
        JSONArray rows = (JSONArray) ret.get("rows");
        assertTrue(rows.length() == 0);
    }
}
