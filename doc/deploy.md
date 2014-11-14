# How to deploy ?

There are several procedures, from the most straightforward (see "Quick win" below) to the most sophisticated (see "Automating ...")

## Hardware considerations

geOrchestra runs well on Debian boxes with Tomcat6 (version 7 might hang your geonetwork, see [#418](https://github.com/georchestra/georchestra/issues/418)).
The minimum system requirement is 2 cores and 4Gb RAM. More is better ;-)

## Quick win

Once your system is ready, collect WAR files in a dedicated directory and rename them:

    PROFILE=myprofile
    VERSION=14.06
    mkdir -p /tmp/georchestra_deploy_tmp
    cd /tmp/georchestra_deploy_tmp
    cp `find ~/.m2/repository/ -name "*-${VERSION}-${PROFILE}.war"` ./
    
    mv security-proxy-${VERSION}-${PROFILE}.war ROOT.war
    mv analytics-${VERSION}-${PROFILE}.war analytics.war
    mv cas-server-webapp-${VERSION}-${PROFILE}.war cas.war
    mv catalogapp-${VERSION}-${PROFILE}.war catalogapp.war
    mv downloadform-${VERSION}-${PROFILE}.war downloadform.war
    mv extractorapp-${VERSION}-${PROFILE}.war extractorapp.war
    mv geonetwork-main-${VERSION}-${PROFILE}.war geonetwork.war
    mv geoserver-webapp-${VERSION}-${PROFILE}.war geoserver.war
    mv ldapadmin-${VERSION}-${PROFILE}.war ldapadmin.war
    mv mapfishapp-${VERSION}-${PROFILE}.war mapfishapp.war
    mv header-${VERSION}-${PROFILE}.war header.war

Finally, dispatch geOrchestra webapps into your 3 Tomcat instances:

    sudo service tomcat-proxycas stop
    sudo mv /tmp/georchestra_deploy_tmp/ROOT.war /opt/tomcat-proxycas/webapps
    sudo mv /tmp/georchestra_deploy_tmp/cas.war /opt/tomcat-proxycas/webapps
    sudo service tomcat-proxycas start

    sudo service tomcat-geoserver stop
    sudo mv /tmp/georchestra_deploy_tmp/geoserver.war /opt/tomcat-geoserver/webapps
    sudo service tomcat-geoserver start

    sudo service tomcat-georchestra stop
    sudo mv /tmp/georchestra_deploy_tmp/* /opt/tomcat-georchestra/webapps
    sudo service tomcat-georchestra start



## Symlinks

This requires the build process to happen on the production host.

TODO

## Automating the deploy to remote hosts

TODO

One can fully automate the above steps with custom crafted deployment scripts.  
An [example](server-deploy/linux_deploy_scripts/) is provided in the server-deploy module.