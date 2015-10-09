package org.georchestra.ldapadmin.ws.backoffice.users;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UsersExport {

    private AccountDao accountDao;
    private UserRule userRule;

    @Autowired
    public UsersExport(AccountDao dao, UserRule userRule) {
        this.accountDao = dao;
        this.userRule = userRule;
    }
    
    @RequestMapping(value = "/users.csv", method = RequestMethod.POST, 
            produces = "text/csv; charset=utf-8")
    public String getUsersAsCsv(@RequestBody String users) throws Exception {
        JSONArray jsonUsers = new JSONArray(users);
        return jsonUsers.toString(4);
    }
    
    @RequestMapping(value = "/users.vcf", method = RequestMethod.POST, 
            produces = "text/x-vcard; charset=utf-8")
    public String getUsersAsVcard(@RequestBody String users) throws Exception {
        return "";
    }
    
}
