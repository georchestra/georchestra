package com.camptocamp.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Retrieve list of roles
 * 
 * @author jesse.eichar@camptocamp.com
 */
@Controller
@RequestMapping("/roles")
public class Roles {
    protected static final Log logger = LogFactory.getLog(Roles.class.getPackage().getName());

    LdapRolesDao               roleDAO;

    @RequestMapping(method = RequestMethod.GET)
    public void admin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<String> names = roleDAO.getAllContactNames();
        names.add(0, "*");
        response.setContentType("application/json; charset=UTF-8");

        PrintWriter writer = response.getWriter();
        try {
            writer.write("{\"roles\": [");
            boolean comma = false;
            for (String name : names) {
                if (comma) {
                    writer.write(",");
                }
                comma = true;
                writer.write(String.format("{\"cn\": \"%s\"}", name));
            }
            writer.write("]}");
        } finally {
            writer.close();
        }
    }

    public void setRoleDAO(LdapRolesDao roleDAO) {
        this.roleDAO = roleDAO;
    }

}
