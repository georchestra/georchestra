# Setting up the middleware

In this section, you will learn how to install and setup the foundations of your geOrchestra SDI, according to the recommended architecture:
![architecture](https://cloud.githubusercontent.com/assets/265319/5538326/ea5a8e32-8ab1-11e4-8d21-00685457a912.png)

The HAProxy / distributed GeoServer setup (in orange) is an alternative setup for high availability or high performance purposes, which will be documented later on.


So, here are the steps:

 * first, install the dependencies:
```
sudo apt-get install postgresql-9.4-postgis-2.1 slapd ldap-utils apache2 ca-certificates tomcat8 tomcat8-user libgdal1h libgdal-java postfix
```
 
 * [set up the application database](setup/postgresql.md)
 
 * [create the LDAP tree](setup/openldap.md)
 
 * configure the front web server ([apache](setup/apache.md))

 * setup 3 [tomcat instances](setup/tomcat.md):
   * one for proxy+cas, 
   * an other one for GeoServer, 
   * and a last one for the other webapps

 * install the [native libs](setup/native_libs.md)

Notes: 
 
 * geOrchestra >= 15.06 has been tested on Debian 8 with Tomcat 6, 7 and 8. This guide assumes Tomcat 8 is used and the OS is Debian's latest stable (Jessie).
 * geOrchestra <= 14.12 has been extensively tested on Debian 6 and 7 with Tomcat 6. It is known not to work with Tomcat > 6.
