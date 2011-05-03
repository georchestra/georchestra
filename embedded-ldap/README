An embedded LDAP for use for testing.  It makes it easy to setup an LDAP. The file default.ldif will automatically be loaded.  Also the hostname of the current machine will be checked so if there is a default_hostname.ldif that file will be loaded into the LDAP instead.

The LDAP will maintain state between runs so to re-import the ldif you will need to do a 


mvn clean

before starting the server again

Server can be started using the command:

mvn jetty:run


To start in the background use:

mvn jetty:run & 

and to stop use:

mvn jetty:stop



The connection credentials are:
ldap://localhost:10389
user = uid=admin,ou=system
password = secret

and the jetty is started on port 9091. There is a little service that verifies if the servlet is running correctly