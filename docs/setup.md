# Setting up the middleware

geOrchestra has been validated to run against Debian Buster, but this version of Debian does not officially include a Java 8 environment, which is the
current version of Java supported by geOrchestra. This will then require to install it from the AdoptOpenJDK packages repository (see below).

In this section, you will learn how to install and setup the foundations of your geOrchestra SDI, according to the recommended architecture:
![architecture](https://cloud.githubusercontent.com/assets/265319/5538326/ea5a8e32-8ab1-11e4-8d21-00685457a912.png)

The HAProxy / distributed GeoServer setup (in orange) is an alternative setup for high availability or high performance purposes, which is documented in a [dedicated tutorial](tutorials/geoserver_clustering.md).


So, here are the steps:

 * install the dependencies:
```
sudo apt install postgresql-11-postgis-2.5 slapd ldap-utils apache2 ca-certificates tomcat9 tomcat9-user libgdal-java postfix git
```
 * Install Java8 from adoptopenJDK:
 ```
apt install -y software-properties-common gpg
wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
sudo add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
apt update
apt install adoptopenjdk-8-hotspot/buster
update-java-alternatives -s adoptopenjdk-8-hotspot-amd64
 ```

by default, `tomcat9` will try to find a JVM which follows the debian conventions, which is not the case of the AdoptOpenJDK packages. To make sure That
the previously installed version is in use, we will have to modify the `/etc/default/tomcat9` file:

```
JAVA_HOME=/usr/lib/jvm/adoptopenjdk-8-hotspot-amd64
```

 * [set up the application database](setup/postgresql.md)

 * [create the LDAP tree](setup/openldap.md)

 * configure the front web server ([apache](setup/apache.md))

 * setup 3 [tomcat instances](setup/tomcat.md):
   * one for proxy+cas,
   * an other one for GeoServer,
   * and a last one for the other webapps

 * install the [native libs](setup/native_libs.md)

 * [Deploy the webapps](setup/deploy_wars.md) into each tomcat instances

## Compatibility notes

This guide assumes the OS is **Debian's latest stable** (Buster) and **Tomcat 9** is deployed.

geOrchestra 20.0.x and 20.1.x have been tested with tomcat 9 on Debian 10 (Buster). This is the recommended setup as of today.

geOrchestra 17.12 has been tested with tomcat 8 on Debian 9 (Stretch).

geOrchestra 16.12 has been tested on Debian 8 (Jessie) with Tomcat 8.

geOrchestra >= 15.12 deprecates tomcat6 and java 6.

geOrchestra 15.06 has been tested on Debian 8 with Tomcat 6, 7 and 8.
