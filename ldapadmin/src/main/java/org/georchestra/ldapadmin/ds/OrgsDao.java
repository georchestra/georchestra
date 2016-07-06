package org.georchestra.ldapadmin.ds;


import org.georchestra.ldapadmin.dto.Org;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import javax.naming.directory.*;
import javax.naming.NamingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class OrgsDao {

    private LdapTemplate ldapTemplate;
    private Name orgsSearchBaseDN;
    private Name userSearchBaseDN;
    private String basePath;


    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setOrgsSearchBaseDN(String orgsSearchBaseDN) {
        this.orgsSearchBaseDN = LdapNameBuilder.newInstance(orgsSearchBaseDN).build();
    }

    public void setUserSearchBaseDN(String userSearchBaseDN) {
        this.userSearchBaseDN = LdapNameBuilder.newInstance(userSearchBaseDN).build();
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getBasePath() {
        return basePath;
    }


    public List<Org> findAll(){
        EqualsFilter filter = new EqualsFilter("objectClass", "groupOfMembers");
        return ldapTemplate.search(this.orgsSearchBaseDN, filter.encode(), new OrgsDao.OrgAttributesMapper());
    }

    public Org findByCommonName(String commonName) {
        Name dn = LdapNameBuilder.newInstance(this.orgsSearchBaseDN).add("cn", commonName).build();
        return this.ldapTemplate.lookup(dn, new OrgsDao.OrgAttributesMapper());
    }

    public Org findForUser(String user) throws DataServiceException {

        Name userDn = LdapNameBuilder.newInstance(this.userSearchBaseDN).add("uid", user).build();

        AndFilter filter  = new AndFilter();
        filter.and(new EqualsFilter("member", userDn.toString()));
        filter.and(new EqualsFilter("objectClass", "groupOfMembers"));
        List<Org> res = ldapTemplate.search(this.orgsSearchBaseDN, filter.encode(), new OrgsDao.OrgAttributesMapper());
        if(res.size() > 1)
            throw new DataServiceException("Multiple org for one user");
        if(res.size() == 1)
            return res.get(0);
        else
            return null;

    }

    public List<String> findUsers(final String org) throws DataServiceException {

        Filter filter = new EqualsFilter("cn", org);
        List<List<String>> res = ldapTemplate.search(this.orgsSearchBaseDN, filter.encode(),  new AttributesMapper<List<String>>() {
            public List<String> mapFromAttributes(Attributes attrs) throws NamingException {

                Attribute member = attrs.get("member");
                List<String> res = new LinkedList<String>();
                for (Enumeration vals = member.getAll(); vals.hasMoreElements();)
                    res.add((String) ((Attribute) vals.nextElement()).get());
                return res;
            }
         });

        if(res.size() > 1)
            throw new DataServiceException("Multiple org for one user");
        if(res.size() == 1)
            return res.get(0);
        else
            return null;

    }


    public void insert(Org org){
        this.ldapTemplate.bind(buildOrgDN(org.getId()), null, buildAttributes(org));
    }

    public void addUser(String organization, String user){
        DirContextOperations context = ldapTemplate.lookupContext(buildOrgDN(organization));
        context.addAttributeValue("member", buildUserDN(user), false);
        this.ldapTemplate.modifyAttributes(context);
    }

    public void removeUser(String organization, String user){
        DirContextOperations ctx = ldapTemplate.lookupContext(buildOrgDN(organization));
        ctx.removeAttributeValue("member", buildUserDN(user).toString());
        this.ldapTemplate.modifyAttributes(ctx);
    }

    private Name buildUserDN(String id){
        return LdapNameBuilder.newInstance(this.userSearchBaseDN + "," + this.basePath).add("uid", id).build();
    }

    private Name buildOrgDN(String id){
        return LdapNameBuilder.newInstance(this.orgsSearchBaseDN).add("cn", id).build();
    }

    private Attributes buildAttributes(Org org) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        ocattr.add("top");
        ocattr.add("groupOfMembers");

        attrs.put(ocattr);
        attrs.put("cn", org.getId());
        attrs.put("o", org.getName());
        attrs.put("ou", org.getShortName());
        attrs.put("description", org.getCities());
        attrs.put("businessCategory", org.getStatus());

        return attrs;
    }


    private class OrgAttributesMapper implements AttributesMapper<Org> {

        public Org mapFromAttributes(Attributes attrs) throws NamingException {
            Org org = new Org();
            org.setId(asString(attrs.get("cn")));
            org.setName(asString(attrs.get("o")));
            org.setShortName(asString(attrs.get("ou")));
            org.setCities(Arrays.asList(asString(attrs.get("description")).split(",")));
            org.setStatus(asString(attrs.get("businessCategory")));
            org.setMembers(asListString(attrs.get("member")));
            return org;
        }

        public String asString(Attribute att) throws NamingException {
            if(att == null)
                return null;
            else
                return (String) att.get();
        }

        public List<String> asListString(Attribute att) throws NamingException {
            if(att == null)
                return null;

            List<String> res = new LinkedList<String>();
            for(int i=0; i< att.size();i++)
                res.add((String) att.get(i));

            return res;
        }
    }
}
