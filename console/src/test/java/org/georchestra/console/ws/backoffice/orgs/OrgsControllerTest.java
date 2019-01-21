package org.georchestra.console.ws.backoffice.orgs;

import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.Org;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

public class OrgsControllerTest {

    @Test
    public void updateFromRequestTakePendingIntoAccount() throws JSONException {
        OrgsDao mockOrgsDao = Mockito.mock(OrgsDao.class);
        OrgsController toTest = new OrgsController(mockOrgsDao);
        Org mockOrg = Mockito.mock(Org.class);
        JSONObject jsonInput = new JSONObject();
        jsonInput.put("pending", true);

        toTest.updateFromRequest(mockOrg, jsonInput);

        Mockito.verify(mockOrg).setPending(true);

        jsonInput = new JSONObject();
        jsonInput.put("pending", false);

        toTest.updateFromRequest(mockOrg, jsonInput);

        Mockito.verify(mockOrg).setPending(false);
    }
}
