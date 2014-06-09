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
import org.springframework.mock.web.MockHttpServletResponse;

public class ExtractorAppTest extends AbstractApplicationTest {


    @Before
    public void setUp() throws Exception {
        ctrl  = new ExtractorApp(null, true);
    }

    @After
    public void tearDown() throws Exception {
    }



    @Test
    public final void testIsInvalid() throws Exception {
        HttpServletRequest req = generateLegitRequest();
        DownloadQuery dq = ctrl.initializeVariables(req);

        // Request should be considered valid
        assertFalse("Expected a valid query", ctrl.isInvalid(dq));

        // The same with an invalid parameter
        Mockito.when(req.getParameter("json_spec")).thenReturn(null);
        dq = ctrl.initializeVariables(req);

        // Should be considered invalid
        assertTrue("Expected a invalid query", ctrl.isInvalid(dq));

        // This one also
        Mockito.when(req.getParameter("sessionid")).thenReturn(null);
        dq = ctrl.initializeVariables(req);
        assertTrue("Expected a invalid query", ctrl.isInvalid(dq));

    }

    /**
     * Tests when the controller is activated but there is no connection to the database.
     * @throws Exception
     *
     */
    @Test
    public final void testHandleRequestNoDbConnection() throws Exception {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ctrl.setActivated(true);
        DataSource ds = Mockito.mock(DataSource.class);
        ctrl.setDataSource(ds);

        ctrl.handleRequest(req, resp);

        JSONObject ret = new JSONObject(resp.getContentAsString());

        assertTrue(ret.get("error").equals("Unable to handle request: java.lang.RuntimeException: "
                + "could not get a connection to the database"));

    }
    /**
     * Tests when the controller is activated but the posted form is invalid.
     * @throws Exception
     *
     */
    @Test
    public final void testHandleRequestInvalidForm() throws Exception {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ctrl.setActivated(true);
        DataSource ds = Mockito.mock(DataSource.class);
        Mockito.when(ds.getConnection()).thenReturn(Mockito.mock(Connection.class));
        ctrl.setDataSource(ds);

        ctrl.handleRequest(req, resp);

        JSONObject ret = new JSONObject(resp.getContentAsString());

        assertFalse(ret.getBoolean("success"));
        assertTrue(ret.getString("msg").equals("invalid form"));
    }


    /**
     * General case:
     * - Form is valid
     * - The code is able to write into the database
     */
    @Test
    public final void testHandleRequest() throws Exception {
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
        // insert data usage info
        Mockito.when(c.prepareStatement(Mockito.anyString())).thenReturn(st);
        ctrl.setDataSource(ds);

        ctrl.handleRequest(req, resp);

        JSONObject ret = new JSONObject(resp.getContentAsString());

        assertTrue(ret.getBoolean("success"));
        assertTrue(ret.getString("msg").equals("Successfully added the record in database."));
    }


}
