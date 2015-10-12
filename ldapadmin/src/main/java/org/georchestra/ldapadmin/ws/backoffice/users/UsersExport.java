package org.georchestra.ldapadmin.ws.backoffice.users;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.NotFoundException;
import org.georchestra.ldapadmin.dto.Account;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UsersExport {

    private AccountDao accountDao;

    private static final Log LOG = LogFactory.getLog(UsersController.class.getName());

    @Autowired
    public UsersExport(AccountDao dao) {
        this.accountDao = dao;
    }

    @RequestMapping(value = "/private/users.csv", method = RequestMethod.POST,
            produces = "text/csv; charset=utf-8")
    @ResponseBody
    public String getUsersAsCsv(@RequestBody String users) throws Exception {
        JSONArray jsonUsers = new JSONArray(users);
        return jsonUsers.toString(4);
    }

    @RequestMapping(value = "private/users.vcf", method = RequestMethod.POST,
            produces = "text/x-vcard; charset=utf-8")
    @ResponseBody
    public String getUsersAsVcard(@RequestBody String users) throws Exception {
        JSONArray usersAr = new JSONArray(users);
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < usersAr.length(); ++ i) {
            try {
                Account a = accountDao.findByUID(usersAr.getString(i));
                ret.append(a.toVcf());
            } catch (NotFoundException e) {
                LOG.error(String.format("User [%s] not found, skipping", usersAr.getString(i)), e);
            }
        }

        return ret.toString();
    }


}
