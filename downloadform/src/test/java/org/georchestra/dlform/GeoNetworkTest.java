package org.georchestra.dlform;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class GeoNetworkTest extends AbstractApplicationTest {

    @Before
    public void setUp() throws Exception {
        ctrl = new GeoNetwork(null, false);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public final void testGeoNetworkInvalidForm() throws Exception {
        HttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ctrl.setActivated(true);
        DataSource ds = Mockito.mock(DataSource.class);
        Connection c  = Mockito.mock(Connection.class);
        PreparedStatement st = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(ds.getConnection()).thenReturn(c);
        Mockito.when(c.prepareStatement(Mockito.anyString(), Mockito.anyInt())).thenReturn(st);
        Mockito.when(st.getGeneratedKeys()).thenReturn(rs);
        Mockito.when(rs.getInt(1)).thenReturn(1);
        ctrl.setDataSource(ds);

        ctrl.handleRequest(req, resp);

        JSONObject ret = new JSONObject(resp.getContentAsString());

        assertTrue(ret.getString("msg").equals("invalid form"));
        assertFalse(ret.getBoolean("success"));
    }

    @Test
    public final void testGeoNetworkValidForm() throws Exception {
        HttpServletRequest req = generateLegitRequest();

        MockHttpServletResponse resp = new MockHttpServletResponse();
        ctrl.setActivated(true);
        DataSource ds = Mockito.mock(DataSource.class);
        Connection c  = Mockito.mock(Connection.class);
        PreparedStatement st = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(ds.getConnection()).thenReturn(c);
        Mockito.when(c.prepareStatement(Mockito.anyString(), Mockito.anyInt())).thenReturn(st);
        Mockito.when(st.getGeneratedKeys()).thenReturn(rs);
        Mockito.when(rs.getInt(1)).thenReturn(1);
        Mockito.when(c.prepareStatement(Mockito.anyString())).thenReturn(st);
        ctrl.setDataSource(ds);

        ctrl.handleRequest(req, resp);

        JSONObject ret = new JSONObject(resp.getContentAsString());

        assertTrue(ret.getBoolean("success"));
        assertTrue(ret.getString("msg").equals("Successfully added the record in database."));
    }

    @Test
    public final void testGeoNetworkNoConnection() throws Exception {
        HttpServletRequest req = generateLegitRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ctrl.setActivated(true);
        DataSource ds = Mockito.mock(DataSource.class);
        Mockito.when(ds.getConnection()).thenReturn(null);
        ctrl.setDataSource(ds);

        ctrl.handleRequest(req, resp);

        JSONObject ret = new JSONObject(resp.getContentAsString());

        assertTrue(ret.getString("error").equals("Unable to handle request: java.lang.NullPointerException"));
        assertTrue(resp.getStatus() == 500);
    }
}
