# Setting up the middleware

geOrchestra has been extensively tested on Debian with Tomcat6 (version 7 is not yet compatible, see [#504](https://github.com/georchestra/georchestra/issues/504)).
As a result, this guide assumes the OS is Debian's latest stable (Wheezy).


Here are the steps:

 * install the dependencies:
```
sudo apt-get install slapd ldap-utils git-core postgresql postgresql-9.1-postgis postgis apache2 tomcat6 libgdal1
```
 
 * [set up the application database](setup/postgresql.md)
 
 * [create the LDAP tree](setup/openlap.md)
 
 * configure the front web server:
 
    * either [with apache](setup/apache.md) 
    * or [with nginx]() (TODO)
    
 * setup tomcat instances
    
    * either a [single tomcat instance](setup/tomcat-single.md) (not recommended)
    * or [several of them](setup/tomcat.md) (in this guide, we assume 3 instances)
    
 * install the [native libs](setup/native_libs.md)
