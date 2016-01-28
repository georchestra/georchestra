package org.georchestra.cas.ldap;

import org.ldaptive.*;

public class WaitForLdap {

    private String ldapUrl;
    private String connectTimeout;
    private String useStartTLS;
    private String username;
    private String password;
    private String groupSearchBaseDn;

    public void test() {
        ConnectionConfig connConfig = new ConnectionConfig(this.getLdapUrl());
        connConfig.setUseStartTLS("true".equals(this.getUseStartTLS()));
        connConfig.setConnectionInitializer(
                new BindConnectionInitializer(
                        this.getUsername(), new Credential(this.getPassword())));
        ConnectionFactory cf = new DefaultConnectionFactory(connConfig);
        while(true) {
            try {
                SearchExecutor executor = new SearchExecutor();
                executor.setBaseDn(this.getGroupSearchBaseDn());
                SearchResult result = executor.search(cf, "(uid=*)").getResult();
                System.out.println("--------------------------------> LDAP OK <------------------------------------");
                break;
            } catch (LdapException e) {
                System.out.println("-------------------------> LDAP Not Ready waiting... <-------------------------");
                try { Thread.sleep(1000l); } catch (InterruptedException e1) {}
            }
        }
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getConnectTimeout() {
        return connectTimeout;
    }

    public void setUseStartTLS(String useStartTLS) {
        this.useStartTLS = useStartTLS;
    }

    public String getUseStartTLS() {
        return useStartTLS;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setGroupSearchBaseDn(String groupSearchBaseDn) {
        this.groupSearchBaseDn = groupSearchBaseDn;
    }

    public String getGroupSearchBaseDn() {
        return groupSearchBaseDn;
    }
}
