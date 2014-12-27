# Setting up the middleware

In this section, you will learn how to install and setup the foundations of your geOrchestra SDI, according to the recommended architecture:
![architecture](https://cloud.githubusercontent.com/assets/265319/5455391/6d1b2d30-853b-11e4-9310-cd59d819afe8.png)

The HAProxy / distributed GeoServer setup (in orange) is an alternative setup for high availability or high performance purposes, which will be documented later on.


So, here are the steps:

 * first, install the dependencies:
```
sudo apt-get install postgresql-9.4-postgis-2.1 slapd ldap-utils apache2 tomcat6 tomcat6-user libgdal1h libgdal-java
```
 
 * [set up the application database](setup/postgresql.md)
 
 * [create the LDAP tree](setup/openldap.md)
 
 * configure the front web server ([apache](setup/apache.md))

 * setup 3 [tomcat instances](setup/tomcat.md):
   * one for proxy+cas, 
   * an other one for GeoServer, 
   * and a last one for the other webapps

 * install the [native libs](setup/native_libs.md)

Note: geOrchestra has been extensively tested on Debian 6 and 7 with Tomcat6 (Tomcat version 7 is not yet compatible, see [#504](https://github.com/georchestra/georchestra/issues/504)). This guide assumes the OS is Debian 8 (Jessie).
