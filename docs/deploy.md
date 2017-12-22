# How to deploy ?

Note that all webapps are not required.  
You could just start with the viewer alone or the viewer and proxy + cas if you need an authentication system. Or viewer + cas + proxy + geoserver ... and add other modules later when required.

## Quick win

Once your system is ready, collect WAR files in a dedicated directory and rename them:

    PROFILE=myprofile
    VERSION=15.12
    
    mkdir -p /tmp/georchestra
    cd /tmp/georchestra
    cp `find ~/.m2/repository/ -name "*-${VERSION}-${PROFILE}.war"` ./
    cp ~/.m2/repository/org/geonetwork-opensource/web-app/3.0.4-SNAPSHOT/web-app-3.0.4-SNAPSHOT.war ./
    
    mv security-proxy-${VERSION}-${PROFILE}.war ROOT.war
    mv analytics-${VERSION}-${PROFILE}.war analytics.war
    mv cas-server-webapp-${VERSION}-${PROFILE}.war cas.war
    mv downloadform-${VERSION}-${PROFILE}.war downloadform.war
    mv extractorapp-${VERSION}-${PROFILE}.war extractorapp.war
    mv geoserver-webapp-${VERSION}-${PROFILE}.war geoserver.war
    mv ldapadmin-${VERSION}-${PROFILE}.war ldapadmin.war
    mv mapfishapp-${VERSION}-${PROFILE}.war mapfishapp.war
    mv header-${VERSION}-${PROFILE}.war header.war
    mv web-app-3.0.4-SNAPSHOT.war geonetwork.war

Optionally, if you do not plan to use GeoServer's integrated GeoWebCache, you can deploy a standalone version:

    mv geowebcache-webapp-${VERSION}-${PROFILE}.war geowebcache.war

Finally, dispatch geOrchestra webapps into your 3 Tomcat instances:

    sudo service tomcat-proxycas stop
    sudo mv /tmp/georchestra/ROOT.war /var/lib/tomcat-proxycas/webapps
    sudo mv /tmp/georchestra/cas.war /var/lib/tomcat-proxycas/webapps
    sudo service tomcat-proxycas start

    sudo service tomcat-geoserver0 stop
    sudo mv /tmp/georchestra/geoserver.war /var/lib/tomcat-geoserver0/webapps
    sudo service tomcat-geoserver0 start

    sudo service tomcat-georchestra stop
    sudo mv /tmp/georchestra/* /var/lib/tomcat-georchestra/webapps
    sudo service tomcat-georchestra start



## Symlinks

Instead of copying the webapps to the tomcat instances, it is possible to symlink them.
Tomcat is smart enough to detect when the link target is updated, and redeploy it on the fly.

However, this requires the build process to happen on the production host (or a NFS mount).

    PROFILE=myprofile
    VERSION=14.06

    sudo service tomcat-proxycas stop
    cd /var/lib/tomcat-proxycas/webapps
    sudo ln -s ~/.m2/repository/org/georchestra/security-proxy/${VERSION}-SNAPSHOT/security-proxy-${VERSION}-SNAPSHOT-${PROFILE}.war ROOT.war
    sudo ln -s ~/.m2/repository/org/georchestra/cas-server-webapp/${VERSION}-SNAPSHOT/cas-server-webapp-${VERSION}-SNAPSHOT-${PROFILE}.war cas.war
    sudo service tomcat-proxycas start

    sudo service tomcat-geoserver stop
    cd /var/lib/tomcat-geoserver/webapps
    sudo ln -s ~/.m2/repository/org/georchestra/geoserver-webapp/${VERSION}-SNAPSHOT/geoserver-webapp-${VERSION}-SNAPSHOT-${PROFILE}.war geoserver.war
    sudo service tomcat-geoserver start
    
    sudo service tomcat-georchestra stop
    cd /var/lib/tomcat-georchestra/webapps
    sudo ln -s ~/.m2/repository/org/georchestra/analytics/${VERSION}-SNAPSHOT/analytics-${VERSION}-SNAPSHOT-${PROFILE}.war analytics.war
    sudo ln -s ~/.m2/repository/org/georchestra/downloadform/${VERSION}-SNAPSHOT/downloadform-${VERSION}-SNAPSHOT-${PROFILE}.war downloadform.war
    sudo ln -s ~/.m2/repository/org/georchestra/extractorapp/${VERSION}-SNAPSHOT/extractorapp-${VERSION}-SNAPSHOT-${PROFILE}.war extractorapp.war
    sudo ln -s ~/.m2/repository/org/geonetwork-opensource/web-app/3.0.4-SNAPSHOT/web-app-3.0.4-SNAPSHOT.war geonetwork.war
    sudo ln -s ~/.m2/repository/org/georchestra/ldapadmin/${VERSION}-SNAPSHOT/ldapadmin-${VERSION}-SNAPSHOT-${PROFILE}.war ldapadmin.war
    sudo ln -s ~/.m2/repository/org/georchestra/mapfishapp/${VERSION}-SNAPSHOT/mapfishapp-${VERSION}-SNAPSHOT-${PROFILE}.war mapfishapp.war
    sudo ln -s ~/.m2/repository/org/georchestra/header/${VERSION}-SNAPSHOT/header-${VERSION}-SNAPSHOT-${PROFILE}.war header.war
    sudo ln -s ~/.m2/repository/org/georchestra/geowebcache-webapp/${VERSION}-SNAPSHOT/geowebcache-webapp-${VERSION}-SNAPSHOT-${PROFILE}.war geowebcache.war
    sudo service tomcat-georchestra start

# What's next ?

You should first [check](check.md) the webapps are working as expected.
Once you've checked, there's a quick [post-deploy configuration step](post-deploy_config.md) that is highly recommended.
