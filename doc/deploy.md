# How to deploy ?

There are several procedures, from the most straightforward (see "Quick win" below) to the most sophisticated (see "Automating ...")

Note that all webapps are not required.  
You could just start with the viewer alone or the viewer and proxy + cas if you need an authentication system. Or viewer + cas + proxy + geoserver ... and add other modules later when required.

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

However, this requires the build process to happen on the production host (or a NFS mount).

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


## Automating the deploy to remote hosts

geOrchestra has a module named  ```server-deploy``` which allows a "deploy" user to push the artifacts to one or several remote servers (copy over ssh).
This is extremely handy when your SDI is spread over a large number of servers.

### Initial setup

First, you need to make sure that the Unix user which is used to deploy is allowed to access all your servers.  
Check that:
 - the "deploy" user has an account on all the SDI servers,
 - his public key is copied in every server's /home/deploy/.ssh/authorized_keys

Once this is done, check that your deploy user can ssh into the different servers without having to provide a password.
A typicall issue is when the .ssh/authorized_keys file permissions are incorrect.

This user must also have write access to your tomcats webapp directory, and he should be able to call ```sudo /etc/init.d/tomcat-* start|stop``` on every machine.


Next step is customizing the ```~/myprofile/DeployScript.groovy``` file in your profile (which stems from [the one provided by the template configuration](https://github.com/georchestra/template/blob/stable/DeployScript.groovy)).  
This file has lots of comments explaining the different steps. After reading them, you should update the script to your particular setup (several SSH instances, tomcat paths, etc).


Finally, you need to create a ```/home/deploy/.m2/settings.xml``` file like this one: 
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<servers>
		<server>
			<id>sdi.georchestra.org</id>
			<username>deploy</username>
			<privateKey>/home/deploy/.ssh/id_rsa</privateKey>
		</server>
		<server>
			<id>test.georchestra.org</id>
			<username>deploy</username>
			<privateKey>/home/deploy/.ssh/id_rsa</privateKey>
		</server>
	</servers>
</settings>
```
... in which the ```id``` fields are the server domain names you want to deploy to.

### Deploying

Being the ```deploy``` user, it's as easy as:
```
cd server-deploy
../mvn -Pupgrade -Dserver=myprofile -Dmaven.test.skip=true -Dnon-interactive=true
```

At the end of the procedure, you should see something like this in your console:

```
[INFO] ------------------------------------------------------------------------
[INFO] Building Modules deployment 14.06-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-dependency-plugin:2.2:unpack (unpack-config) @ server-deploy ---
[INFO] Configured Artifact: org.georchestra:config:myprofile:14.06-SNAPSHOT:jar
[INFO] Unpacking /home/deploy/.m2/repository/org/georchestra/config/14.06-SNAPSHOT/config-14.06-SNAPSHOT-myprofile.jar to /var/cache/deploy/stable/checkout/server-deploy/target/conf with includes  and excludes
[INFO]
[INFO] --- maven-dependency-plugin:2.2:copy-dependencies (default) @ server-deploy ---
[INFO] Copying cas-server-webapp-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/cas-server-webapp-14.06-SNAPSHOT-myprofile.war
[INFO] Copying catalogapp-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/catalogapp-14.06-SNAPSHOT-myprofile.war
[INFO] Copying geofence-webapp-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/geofence-webapp-14.06-SNAPSHOT-myprofile.war
[INFO] Copying header-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/header-14.06-SNAPSHOT-myprofile.war
[INFO] Copying extractorapp-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/extractorapp-14.06-SNAPSHOT-myprofile.war
[INFO] Copying analytics-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/analytics-14.06-SNAPSHOT-myprofile.war
[INFO] Copying geonetwork-main-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/geonetwork-main-14.06-SNAPSHOT-myprofile.war
[INFO] Copying ldapadmin-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/ldapadmin-14.06-SNAPSHOT-myprofile.war
[INFO] Copying security-proxy-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/security-proxy-14.06-SNAPSHOT-myprofile.war
[INFO] Copying downloadform-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/downloadform-14.06-SNAPSHOT-myprofile.war
[INFO] Copying mapfishapp-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/mapfishapp-14.06-SNAPSHOT-myprofile.war
[INFO] Copying geoserver-webapp-14.06-SNAPSHOT-myprofile.war to /var/cache/deploy/stable/checkout/server-deploy/target/wars/geoserver-webapp-14.06-SNAPSHOT-myprofile.war
[INFO]
[INFO] --- gmaven-plugin:1.0:execute (default) @ server-deploy ---
non-interactive is enabled so using system property 'passphrase' as the privateKey passphrase
[INFO]  executing mkdir -p /tmp/georchestra_deploy_tmp
[INFO]  scp target/wars/ldapadmin-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/ldapadmin.war
[INFO]  scp complete: 18.2MB copied
[INFO]  scp target/wars/extractorapp-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/extractorapp.war
[INFO]  scp complete: 89MB copied
[INFO]  scp target/wars/geofence-webapp-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/geofence.war
[INFO]  scp complete: 65MB copied
[INFO]  scp target/wars/mapfishapp-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/mapfishapp.war
[INFO]  scp complete: 63MB copied
[INFO]  scp target/wars/analytics-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/analytics.war
[INFO]  scp complete: 51.8MB copied
[INFO]  scp target/wars/geonetwork-main-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/geonetwork.war
[INFO]  scp complete: 125.4MB copied
[INFO]  scp target/wars/header-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/header.war
[INFO]  scp complete: 3.6MB copied
[INFO]  executing sudo /etc/init.d/tomcat-georchestra stop
[INFO]  executing rm -rf /srv/tomcat/georchestra/webapps/ldapadmin
[INFO]  executing rm -f /srv/tomcat/georchestra/webapps/ldapadmin.war   
[INFO]  executing rm -rf /srv/tomcat/georchestra/webapps/extractorapp   
[INFO]  executing rm -f /srv/tomcat/georchestra/webapps/extractorapp.war
[INFO]  executing rm -rf /srv/tomcat/georchestra/webapps/geofence
[INFO]  executing rm -f /srv/tomcat/georchestra/webapps/geofence.war
[INFO]  executing rm -rf /srv/tomcat/georchestra/webapps/mapfishapp
[INFO]  executing rm -f /srv/tomcat/georchestra/webapps/mapfishapp.war  
[INFO]  executing rm -rf /srv/tomcat/georchestra/webapps/analytics
[INFO]  executing rm -f /srv/tomcat/georchestra/webapps/analytics.war   
[INFO]  executing rm -rf /srv/tomcat/georchestra/webapps/geonetwork
[INFO]  executing rm -f /srv/tomcat/georchestra/webapps/geonetwork.war  
[INFO]  executing rm -rf /srv/tomcat/georchestra/webapps/header
[INFO]  executing rm -f /srv/tomcat/georchestra/webapps/header.war
[INFO]  executing cp /tmp/georchestra_deploy_tmp/* /srv/tomcat/georchestra/webapps
[INFO]  executing sudo /etc/init.d/tomcat-georchestra start
[INFO]  executing rm -rf /tmp/georchestra_deploy_tmp
[INFO]  executing mkdir -p /tmp/georchestra_deploy_tmp
[INFO]  scp target/wars/security-proxy-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/ROOT.war
[INFO]  scp complete: 8.2MB copied
[INFO]  scp target/wars/cas-server-webapp-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/cas.war
[INFO]  scp complete: 12.5MB copied
[INFO]  executing sudo /etc/init.d/tomcat-proxycas stop
[INFO]  executing rm -rf /srv/tomcat/proxycas/webapps/ROOT
[INFO]  executing rm -f /srv/tomcat/proxycas/webapps/ROOT.war
[INFO]  executing rm -rf /srv/tomcat/proxycas/webapps/cas
[INFO]  executing rm -f /srv/tomcat/proxycas/webapps/cas.war
[INFO]  executing cp /tmp/georchestra_deploy_tmp/* /srv/tomcat/proxycas/webapps
[INFO]  executing sudo /etc/init.d/tomcat-proxycas start
[INFO]  executing rm -rf /tmp/georchestra_deploy_tmp
non-interactive is enabled so using system property 'passphrase' as the privateKey passphrase
[INFO]  executing mkdir -p /tmp/georchestra_deploy_tmp
[INFO]  scp target/wars/geoserver-webapp-14.06-SNAPSHOT-myprofile.war deploy@sdi.georchestra.org:/tmp/georchestra_deploy_tmp/geoserver.war
[INFO]  scp complete: 135.7MB copied
[INFO]  executing sudo /etc/init.d/tomcat-geoserver0 stop
[INFO]  executing rm -rf /srv/tomcat/geoserver0/webapps/geoserver
[INFO]  executing rm -f /srv/tomcat/geoserver0/webapps/geoserver.war
[INFO]  executing cp /tmp/georchestra_deploy_tmp/* /srv/tomcat/geoserver0/webapps
[INFO]  executing sudo /etc/init.d/tomcat-geoserver0 start
[INFO]  executing rm -rf /tmp/georchestra_deploy_tmp
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1:27.317s
[INFO] Finished at: Wed Apr 09 15:13:15 CEST 2014
[INFO] Final Memory: 23M/377M
[INFO] ------------------------------------------------------------------------
```
