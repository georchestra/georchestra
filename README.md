geOrchestra
===========

geOrchestra is a complete **Spatial Data Infrastructure** solution.

It features a **metadata catalog** (GeoNetwork 2.10), an **OGC server** (GeoServer 2.3.2), an **advanced viewer and editor**, an **extractor** and **many more** (security and auth system based on proxy/CAS/LDAP, analytics, admin UIs, ...)

More information in the modules README:
 * [catalog](https://github.com/georchestra/geonetwork/blob/georchestra-29/README.md) (aka GeoNetwork)
 * [viewer](mapfishapp/README.md) (aka mapfishapp)
 * [extractor](extractorapp/README.md) (aka extractorapp)
 * [simple catalog](catalogapp/README.md) (aka catalogapp)
 * [analytics](analytics/README.md)
 * [ldapadmin](ldapadmin/README.md)
 * [downloadform](downloadform/README.md)
 * [ogc-server-statistics](ogc-server-statistics/README.md)
 * [header](header/README.md)

See also the [release notes](RELEASE_NOTES.md).


How to build ?
==============

First, install the required packages: 

    sudo apt-get install ant ant-optional openjdk-7-jdk python-virtualenv

Notes: 
 * openjdk-6-jdk works too 
 * GeoServer is [known](http://research.geodan.nl/2012/10/openjdk7-vs-oracle-jdk7-with-geoserver/) to perform better with Oracle JDK.

Then clone the repository (either the stable branch or master if you're feeling lucky):

    git clone -b 13.09 --recursive https://github.com/georchestra/georchestra.git

...and build:

    cd georchestra
    ./mvn -Dmaven.test.skip=true -Ptemplate install


How to customize ?
==================

For testing purposes:

    cd config/configurations
    cp -r template myprofile

You can then edit files in myprofile to match your needs.

Finally, to build geOrchestra with your own configuration profile:

    ./mvn -Dmaven.test.skip=true -Dserver=myprofile install

Note: if you're planning to use geOrchestra on the long term, you're better off forking the [georchestra/template](https://github.com/georchestra/template) configuration repository into a private git repository.
This way, you'll be able to merge into your branch the changes from upstream.

Example workflow:

    cd config/configurations
    git clone git@github.com:georchestra/template.git myprofile
    cd myprofile
    git remote rename origin upstream
    (feel free to add a new origin to a private server)

Do whatever updates you want in the master branch, and regularly merge the upstream changes:

    git checkout master
    git fetch upstream
    git merge upstream/master

Note: merge upstream/master into your config if you're using geOrchestra master, or upstream/13.09 if you're using geOrchestra stable.

Read more about the [configuration process](config/README.md).


How to install ?
===============

geOrchestra runs well on Debian boxes with Tomcat6 (version 7 might hang your geonetwork, see #418).
An example setup on one Tomcat is described [here](INSTALL.md).

Once the system is ready, collect WAR files in a dedicated directory and rename them:

    PROFILE=myprofile
    VERSION=13.09
    mkdir -p /tmp/georchestra_deploy_tmp
    cd /tmp/georchestra_deploy_tmp
    cp `find ~/.m2/repository/ -name "*-${VERSION}-${PROFILE}.war"` ./
    
    mv security-proxy-${VERSION}-${PROFILE}.war ROOT.war
    mv analytics-${VERSION}-${PROFILE}.war analytics-private.war
    mv cas-server-webapp-${VERSION}-${PROFILE}.war cas.war
    mv catalogapp-${VERSION}-${PROFILE}.war catalogapp-private.war
    mv downloadform-${VERSION}-${PROFILE}.war downloadform-private.war
    mv extractorapp-${VERSION}-${PROFILE}.war extractorapp-private.war
    mv geonetwork-main-${VERSION}-${PROFILE}.war geonetwork-private.war
    mv geoserver-webapp-${VERSION}-${PROFILE}.war geoserver-private.war
    mv ldapadmin-${VERSION}-${PROFILE}.war ldapadmin-private.war
    mv mapfishapp-${VERSION}-${PROFILE}.war mapfishapp-private.war
    mv header-${VERSION}-${PROFILE}.war header-private.war

Copy WAR files in Tomcat webapps dir:

    sudo service tomcat6 stop
    sudo cp -f /tmp/georchestra_deploy_tmp/* /var/lib/tomcat6/webapps
    sudo service tomcat6 start

This is the basic idea, but one can use more advanced deploy scripts. An example is provided 
[here](server-deploy/linux_deploy_scripts/Readme.md).

Note: it is also possible to split the webapps across several Tomcat instances. 
The recommended setup is to have at least 2 tomcats, with one entirely dedicated to GeoServer.
