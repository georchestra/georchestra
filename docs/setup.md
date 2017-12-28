# Setting up the middleware

In this section, you will learn how to install and setup the foundations of your geOrchestra SDI, according to the recommended architecture:
![architecture](https://cloud.githubusercontent.com/assets/265319/5538326/ea5a8e32-8ab1-11e4-8d21-00685457a912.png)

The HAProxy / distributed GeoServer setup (in orange) is an alternative setup for high availability or high performance purposes, which is documented in a [dedicated tutorial](tutorials/geoserver_clustering.md).

So, here are the steps:

 * install the dependencies:
```
sudo apt-get install postgresql-9.6-postgis-2.3 slapd ldap-utils apache2 ca-certificates tomcat8 tomcat8-user libgdal1h libgdal-java postfix
```
 
 * [set up the application database](setup/postgresql.md)
 
 * [create the LDAP tree](setup/openldap.md)
 
 * configure the front web server ([apache](setup/apache.md))

 * setup 3 [tomcat instances](setup/tomcat.md):
   * one for proxy+cas, 
   * an other one for GeoServer, 
   * and a last one for the other webapps

 * install the [native libs](setup/native_libs.md)


## Compatibility notes

This guide assumes the OS is **Debian's latest stable** (Stretch) and **Tomcat 8** is deployed.

geOrchestra 17.12 has been tested with tomcat 8 on Debian 9 (Stretch). This is the recommended setup as of today.

geOrchestra 16.12 has been tested on Debian 8 (Jessie) with Tomcat 8.

geOrchestra >= 15.12 deprecates tomcat6 and java 6.

geOrchestra 15.06 has been tested on Debian 8 with Tomcat 6, 7 and 8.
