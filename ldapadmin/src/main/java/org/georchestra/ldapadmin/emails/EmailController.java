package org.georchestra.ldapadmin.emails;

import org.georchestra.ldapadmin.dao.EmailDao;
import org.georchestra.ldapadmin.model.EmailEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@Controller
public class EmailController {

    @Autowired
    private EmailDao repository;

    @Autowired
	private DriverManagerDataSource dataSource;

	@Autowired
	private JpaTransactionManager tm;


    @RequestMapping(value="/{sender}/emails", method= RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String emailsList(@PathVariable String sender) {

        List<EmailEntry> emails = repository.findBySender(UUID.fromString(sender));

        return "{ 'count': " + emails.size() + " }";
    }

}
