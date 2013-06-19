geOrchestra
===========

geOrchestra is a complete **Spatial Data Infrastructure** solution.

It features a **metadata catalog** (GeoNetwork 2.10), an **OGC server** (GeoServer 2.3.2), an **advanced viewer**, an **extractor** and **many more** (security and auth system based on proxy/CAS/LDAP, analytics, admin UIs, ...)

More information in the modules README:
 * [catalog](https://github.com/georchestra/geonetwork/blob/georchestra-29/README.md) (aka GeoNetwork)
 * [viewer](https://github.com/georchestra/georchestra/blob/master/mapfishapp/README.md) (aka mapfishapp)
 * [extractor](https://github.com/georchestra/georchestra/blob/master/extractorapp/README.md) (aka extractorapp)
 * [simple catalog](https://github.com/georchestra/georchestra/blob/master/catalogapp/README.md) (aka catalogapp)
 * [analytics](https://github.com/georchestra/georchestra/blob/master/analytics/README.md)
 * [downloadform](https://github.com/georchestra/georchestra/blob/master/downloadform/README.md)
 * [ogc-server-statistics](https://github.com/georchestra/georchestra/blob/master/ogc-server-statistics/README.md)
 * [static](https://github.com/georchestra/georchestra/blob/master/static/README.md)


How to build ?
==============

First, install the required packages: 

    sudo apt-get install ant ant-optional openjdk-7-jdk

Notes: 
 * openjdk-6-jdk works too 
 * GeoServer is known to perform better with Oracle JDK

Then:

    git clone --recursive https://github.com/georchestra/georchestra.git
    cd georchestra
    ./mvn -Dmaven.test.skip=true -Ptemplate install

How to customize ?
==================
 
Copy the "template" config directory (or fork the [georchestra/template](https://github.com/georchestra/template) repository) and edit "yourown" to match your needs:

    PROFILE=yourown
    cp -r config/configurations/template config/configurations/${PROFILE}
       (edit files in config/configuration/yourown)
    ./mvn -Dmaven.test.skip=true -Dserver=${PROFILE} install

[Read more](https://github.com/georchestra/georchestra/blob/master/config/README.md) about the configuration process

How to deploy ?
===============

Collect WAR files in a dedicated directory and rename them:

    PROFILE=yourown
    mkdir /tmp/georchestra_deploy_tmp
    cd /tmp/georchestra_deploy_tmp
    cp `find ~/.m2/repository/ -name *-13.02-${PROFILE}.war` ./
    
    mv security-proxy-13.02-${PROFILE}.war ROOT.war
    mv analytics-13.02-${PROFILE}.war analytics-private.war
    mv cas-server-webapp-13.02-${PROFILE}.war cas.war
    mv catalogapp-13.02-${PROFILE}.war catalogapp-private.war
    mv downloadform-13.02-${PROFILE}.war downloadform-private.war
    mv extractorapp-13.02-${PROFILE}.war extractorapp-private.war
    mv geonetwork-main-13.02-${PROFILE}.war geonetwork-private.war
    mv geoserver-webapp-13.02-${PROFILE}.war geoserver-private.war
    mv mapfishapp-13.02-${PROFILE}.war mapfishapp-private.war
    mv static-13.02-${PROFILE}.war static-private.war

Copy WAR files in Tomcat webapps dir:

    sudo /etc/init.d/tomcat stop
    cp -f /tmp/georchestra_deploy_tmp/* /srv/tomcat/webapps
    sudo /etc/init.d/tomcat start

This is the basic idea, but one can use more advanced deploy scripts. An example is provided 
[here](https://github.com/georchestra/georchestra/blob/master/server-deploy/linux_deploy_scripts/Readme.md).
