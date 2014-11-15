# How to deploy ?

There are several procedures, from the most straightforward (see "Quick win" below) to the most sophisticated (see "Automating ...")

## OS and Hardware considerations


## Quick win

Once your system is ready, collect WAR files in a dedicated directory and rename them:

    PROFILE=myprofile
    VERSION=14.06
    
    mkdir -p /tmp/georchestra
    cd /tmp/georchestra
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
    sudo mv /tmp/georchestra/ROOT.war /opt/tomcat-proxycas/webapps
    sudo mv /tmp/georchestra/cas.war /opt/tomcat-proxycas/webapps
    sudo service tomcat-proxycas start

    sudo service tomcat-geoserver stop
    sudo mv /tmp/georchestra/geoserver.war /opt/tomcat-geoserver/webapps
    sudo service tomcat-geoserver start

    sudo service tomcat-georchestra stop
    sudo mv /tmp/georchestra/* /opt/tomcat-georchestra/webapps
    sudo service tomcat-georchestra start



## Symlinks

Instead of copying the webapps to the tomcat instances, it is possible to symlink them.
Tomcat is smart enough to detect when the link target is updated, and redeploy it on the fly.

This however requires the build process to happen on the production host (or a NFS mount).

    PROFILE=myprofile
    VERSION=14.06

    sudo service tomcat-proxycas stop
    cd /opt/tomcat-proxycas/webapps
    sudo ln -s ~/.m2/repository/org/georchestra/security-proxy/${VERSION}-SNAPSHOT/security-proxy-${VERSION}-SNAPSHOT-${PROFILE}.war ROOT.war
    sudo ln -s ~/.m2/repository/org/georchestra/cas-server-webapp/${VERSION}-SNAPSHOT/cas-server-webapp-${VERSION}-SNAPSHOT-${PROFILE}.war cas.war
    sudo service tomcat-proxycas start

    sudo service tomcat-geoserver stop
    cd /opt/tomcat-geoserver/webapps
    sudo ln -s ~/.m2/repository/org/georchestra/geoserver-webapp/${VERSION}-SNAPSHOT/geoserver-webapp-${VERSION}-SNAPSHOT-${PROFILE}.war geoserver.war
    sudo service tomcat-geoserver start
    
    sudo service tomcat-georchestra stop
    cd /opt/tomcat-georchestra/webapps
    sudo ln -s ~/.m2/repository/org/georchestra/analytics/${VERSION}-SNAPSHOT/analytics-${VERSION}-SNAPSHOT-${PROFILE}.war analytics.war
    sudo ln -s ~/.m2/repository/org/georchestra/catalogapp/${VERSION}-SNAPSHOT/catalogapp-${VERSION}-SNAPSHOT-${PROFILE}.war catalogapp.war
    sudo ln -s ~/.m2/repository/org/georchestra/downloadform/${VERSION}-SNAPSHOT/downloadform-${VERSION}-SNAPSHOT-${PROFILE}.war downloadform.war
    sudo ln -s ~/.m2/repository/org/georchestra/extractorapp/${VERSION}-SNAPSHOT/extractorapp-${VERSION}-SNAPSHOT-${PROFILE}.war extractorapp.war
    sudo ln -s ~/.m2/repository/org/geonetwork-opensource/geonetwork-main/${VERSION}-SNAPSHOT/geonetwork-main-${VERSION}-SNAPSHOT-${PROFILE}.war geonetwork.war
    sudo ln -s ~/.m2/repository/org/georchestra/ldapadmin/${VERSION}-SNAPSHOT/ldapadmin-${VERSION}-SNAPSHOT-${PROFILE}.war ldapadmin.war
    sudo ln -s ~/.m2/repository/org/georchestra/mapfishapp/${VERSION}-SNAPSHOT/mapfishapp-${VERSION}-SNAPSHOT-${PROFILE}.war mapfishapp.war
    sudo ln -s ~/.m2/repository/org/georchestra/header/${VERSION}-SNAPSHOT/header-${VERSION}-SNAPSHOT-${PROFILE}.war header.war
    sudo service tomcat-georchestra start


TODO

## Automating the deploy to remote hosts

TODO

One can fully automate the above steps with custom crafted deployment scripts.  
An [example](server-deploy/linux_deploy_scripts/) is provided in the server-deploy module.