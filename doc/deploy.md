How to deploy ?
===============

geOrchestra runs well on Debian boxes with Tomcat6 (version 7 might hang your geonetwork, see #418).
The minimum system requirement is 2 cores and 4Gb RAM. More is better ;-)

To install the required packages and setup the system, follow the [install guide](INSTALL.md) (based on a unique tomcat instance).

Once the system is ready, collect WAR files in a dedicated directory and rename them:

    PROFILE=myprofile
    VERSION=14.06
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

This is the basic idea, but one can fully automate this step with custom crafted deployment scripts. 
An [example](server-deploy/linux_deploy_scripts/) is provided in the server-deploy module.
