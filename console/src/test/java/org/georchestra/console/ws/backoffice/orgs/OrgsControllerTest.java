package org.georchestra.console.ws.backoffice.orgs;

import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.orgs.OrgExt;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.utils.Validation;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OrgsControllerTest {

    private DelegationEntry mockEntry1;
    private DelegationEntry mockEntry2;
    private Org mockOrg;
    private OrgExt mockOrgExt;
    private OrgsDao mockOrgsDao;
    private DelegationDao delegationDaoMock;

    @Before
    public void grantRight()
    {
        // Set user connected through spring security
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        role.add(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin",null, role);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void updateFromRequestTakePendingIntoAccount() throws JSONException, IOException {
        OrgsDao mockOrgsDao = mock(OrgsDao.class);
        OrgsController toTest = new OrgsController(mockOrgsDao);
        Org mockOrg = mock(Org.class);
        JSONObject jsonInput = new JSONObject();
        jsonInput.put("pending", true);

        toTest.updateFromRequest(mockOrg, jsonInput);

        verify(mockOrg).setPending(true);

        jsonInput = new JSONObject();
        jsonInput.put("pending", false);

        toTest.updateFromRequest(mockOrg, jsonInput);

        verify(mockOrg).setPending(false);
    }

    @Test
    public void deleteOrgDeleteDelegation() throws IOException, SQLException {
        OrgsController toTest = createToTest();
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        toTest.deleteOrg("csc", mockResponse);

        verify(mockOrgsDao).delete(mockOrg);
        verify(mockOrgsDao).delete(mockOrgExt);
        verify(mockEntry1).removeOrg("csc");
        verify(mockEntry2).removeOrg("csc");
        verify(delegationDaoMock).save(mockEntry1);
        verify(delegationDaoMock).save(mockEntry2);
   }

    @Test
    public void updateOrgUpateDelegation() throws IOException, SQLException {
        OrgsController toTest = createToTest();
        when(mockOrg.getId()).thenReturn("c2c42");
        when(mockOrgsDao.reGenerateId("c2c", "csc")).thenReturn("c2c42");
        when(mockEntry1.getOrgs()).thenReturn(new String[] {"momorg"});
        when(mockEntry2.getOrgs()).thenReturn(new String[] {});
        JSONObject reqUsr = new JSONObject().put("shortName", "c2c");
        MockHttpServletRequest request = new MockHttpServletRequest();;
        request.setContent(reqUsr.toString().getBytes());

        toTest.updateOrgInfos("csc", request);

        verify(mockOrgsDao).update(mockOrg);
        verify(mockOrgsDao).update(mockOrgExt);
        verify(mockEntry1).removeOrg("csc");
        verify(mockEntry1).setOrgs(aryEq(new String[] {"momorg", "c2c42"}));
        verify(mockEntry2).removeOrg("csc");
        verify(mockEntry2).setOrgs(aryEq(new String[] {"c2c42"}));
        verify(delegationDaoMock).save(mockEntry1);
        verify(delegationDaoMock).save(mockEntry2);
    }

    private OrgsController createToTest() throws SQLException {
        mockOrgsDao = mock(OrgsDao.class);
        delegationDaoMock = mock(DelegationDao.class);
        mockOrg = mock(Org.class);
        mockOrgExt = mock(OrgExt.class);
        AdvancedDelegationDao advancedDelegationDaoMock = mock(AdvancedDelegationDao.class);
        Validation mockValidation = mock(Validation.class);
        when(mockValidation.validateOrgField(anyString(), any(JSONObject.class))).thenReturn(true);
        OrgsController toTest = new OrgsController(mockOrgsDao);
        toTest.delegationDao = delegationDaoMock;
        toTest.advancedDelegationDao = advancedDelegationDaoMock;
        toTest.validation = mockValidation;
        when(mockOrgsDao.findByCommonName("csc")).thenReturn(mockOrg);
        when(mockOrgsDao.findExtById("csc")).thenReturn(mockOrgExt);
        mockEntry1 = mock(DelegationEntry.class);
        mockEntry2 = mock(DelegationEntry.class);
        Mockito.when(advancedDelegationDaoMock.findByOrg("csc")).thenReturn(Arrays.asList(new DelegationEntry[]{mockEntry1, mockEntry2}));
        return toTest;
    }
}
