geOrchestra
===========

geOrchestra is a complete **Spatial Data Infrastructure** solution.

It features a **metadata catalog** (GeoNetwork), an **OGC server** (GeoServer), an **advanced viewer**, an **extractor** and **many more** (security and auth system based on proxy/CAS/LDAP, analytics, admin UIs, ...)

More information in the modules README :
 * [viewer](https://github.com/georchestra/georchestra/blob/master/mapfishapp/README.md) (aka mapfishapp)
 * [extractor](https://github.com/georchestra/georchestra/blob/master/extractorapp/README.md) (aka extractorapp)
 * [simple catalog](https://github.com/georchestra/georchestra/blob/master/catalogapp/README.md) (aka catalogapp)
 * [analytics](https://github.com/georchestra/georchestra/blob/master/analytics/README.md)
 * [downloadform](https://github.com/georchestra/georchestra/blob/master/downloadform/README.md)
 * [ogc-server-statistics](https://github.com/georchestra/georchestra/blob/master/ogc-server-statistics/README.md)
 * [static](https://github.com/georchestra/georchestra/blob/master/static/README.md)


How to build ?
==============

First, install the required packages : 

    $ sudo apt-get install ant openjdk-6-jdk

(Note: GeoServer is known to perform better with Oracle JDK)

Then :

    $ git clone --recursive https://github.com/georchestra/georchestra.git
    $ cd georchestra
    $ ./mvn -Dmaven.test.skip=true -Ptemplate install

How to customize ?
==================
 
Copy the "template" config directory and edit "yourown" to match your needs:

    $ cp -r config/configuration/template config/configuration/yourown
    (edit files in config/configuration/yourown)
    $ ./mvn -Dmaven.test.skip=true -Dserver=yourown -Pyourown install

How to deploy ?
===============

Collect WAR files in a dedicated directory and rename them:

    $ mkdir /tmp/georchestra_deploy_tmp
    $ cp `find ~/.m2/repository/org/georchestra/ -name *-yourown.war` /tmp/georchestra_deploy_tmp
    $ cd /tmp/georchestra_deploy_tmp
    $ cp ~/.m2/repository/org/geonetwork-opensource/geonetwork-main/2.6.4-SNAPSHOT/geonetwork-main-2.6.4-SNAPSHOT-yourown.war geonetwork-private.war
    $ cp ~/.m2/repository/org/georchestra/geoserver-webapp/1.0/geoserver-webapp-1.0-yourown.war geoserver-private.war
    $ mv analytics-1.0-yourown.war analytics-private.war
    $ mv catalogapp-1.0-yourown.war catalogapp-private.war
    $ mv mapfishapp-1.0-yourown.war mapfishapp-private.war
    $ mv cas-server-webapp-1.0-yourown.war cas.war
    $ mv security-proxy-1.0-yourown.war ROOT.war
    $ mv extractorapp-1.0-yourown.war extractorapp-private.war
    $ mv static-1.0-yourown.war static.war
    $ mv downloadform-1.0-yourown.war downloadform-private.war

Copy WAR files in Tomcat webapps dir:

    $ sudo /etc/init.d/tomcat stop
    $ cp -f /tmp/georchestra_deploy_tmp/* /srv/tomcat/webapps
    $ sudo /etc/init.d/tomcat start

