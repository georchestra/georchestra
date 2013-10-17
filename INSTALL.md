Install notes for a fresh Debian stable.

LDAP
=====

* install the required packages

        sudo apt-get install slapd ldap-utils

* sample data import

 * getting the data
 
            sudo apt-get install git-core
            git clone git://github.com/georchestra/LDAP.git
            cd LDAP
	
 * inserting the data: follow the instructions in https://github.com/georchestra/LDAP/blob/master/README.md

 * check everything is OK:
 
            ldapsearch -x -bdc=georchestra,dc=org | less

PostGreSQL
==========

* Installation 

        sudo apt-get install postgresql postgresql-9.1-postgis postgis
	
* GeoNetwork database setup

        sudo su postgres
        createdb geonetwork
        psql -f /usr/share/postgresql/9.1/contrib/postgis-1.5/postgis.sql geonetwork
        psql -f /usr/share/postgresql/9.1/contrib/postgis-1.5/spatial_ref_sys.sql geonetwork

        createuser -DPRS www-data

* downloadform and ogcstatistics databases setup

 * downloadform

            createdb downloadform
            wget https://raw.github.com/georchestra/georchestra/master/downloadform/samples/sample.sql -O /tmp/downloadform.sql
            psql -d downloadform -f /tmp/downloadform.sql

 * ogcstatistics

            createdb ogcstatistics
            wget https://raw.github.com/georchestra/georchestra/master/ogc-server-statistics/ogc_statistics_table.sql -O /tmp/ogc_statistics_table.sql
            psql ogcstatistics -f /tmp/ogc_statistics_table.sql

 * ldapadmin

            createdb ldapadmin
            wget https://raw.github.com/georchestra/georchestra/master/ldapadmin/ldapAdminDB.sql -O /tmp/ldapAdminDB.sql
            psql ldapadmin -f /tmp/ldapAdminDB.sql

* Set rights of the www-data user

        echo 'GRANT ALL PRIVILEGES ON DATABASE geonetwork TO "www-data";' | psql -d geonetwork
        echo 'GRANT ALL PRIVILEGES ON SCHEMA public TO "www-data";' | psql -d geonetwork
        echo 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "www-data";' | psql -d geonetwork
        echo 'GRANT ALL PRIVILEGES ON DATABASE downloadform TO "www-data";' | psql -d downloadform
        echo 'GRANT ALL PRIVILEGES ON SCHEMA public TO "www-data";' | psql -d downloadform
        echo 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "www-data";' | psql -d downloadform
        echo 'GRANT ALL PRIVILEGES ON DATABASE ogcstatistics TO "www-data";' | psql -d ogcstatistics
        echo 'GRANT ALL PRIVILEGES ON SCHEMA public TO "www-data";' | psql -d ogcstatistics
        echo 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "www-data";' | psql -d ogcstatistics
        echo 'GRANT ALL PRIVILEGES ON DATABASE ldapadmin TO "www-data";' | psql -d ldapadmin
        echo 'GRANT ALL PRIVILEGES ON SCHEMA public TO "www-data";' | psql -d ldapadmin
        echo 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "www-data";' | psql -d ldapadmin
        exit

Apache
=========

* modules setup

        sudo apt-get install apache2 libapache2-mod-auth-cas
        sudo a2enmod proxy_ajp proxy_connect proxy_http proxy ssl rewrite headers
        sudo service apache2 graceful

* VirtualHost setup

        cd /etc/apache2/sites-available
        sudo a2dissite default default-ssl
        sudo nano georchestra

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   	<VirtualHost *:80>
		 ServerName vm-georchestra
		 DocumentRoot /var/www/georchestra/htdocs
		 LogLevel warn
		 ErrorLog /var/www/georchestra/logs/error.log
		 CustomLog /var/www/georchestra/logs/access.log "combined"
		 Include /var/www/georchestra/conf/*.conf
		 ServerSignature Off
	</VirtualHost>
	<VirtualHost *:443>
		 ServerName vm-georchestra
		 DocumentRoot /var/www/georchestra/htdocs
		 LogLevel warn
		 ErrorLog /var/www/georchestra/logs/error.log
		 CustomLog /var/www/georchestra/logs/access.log "combined"
		 Include /var/www/georchestra/conf/*.conf
		 SSLEngine On
		 SSLCertificateFile /var/www/georchestra/ssl/georchestra.crt
		 SSLCertificateKeyFile /var/www/georchestra/ssl/georchestra-unprotected.key
		 SSLCACertificateFile /etc/ssl/certs/ca-certificates.crt
		 ServerSignature Off
	</VirtualHost>
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        sudo a2ensite georchestra
   
* web directories for geOrchestra

        cd /var/www
        sudo mkdir georchestra
        cd georchestra
        sudo mkdir conf htdocs logs ssl

    Debian apache user is www-data

        sudo id www-data

    we have to grant write on logs to www-data:

        sudo chgrp www-data logs/
        sudo chmod g+w logs/

* Apache config

        cd conf/
        sudo nano /var/www/georchestra/conf/proxypass.conf
        
    should have something like:
        
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    <IfModule !mod_proxy.c>
        LoadModule proxy_module /usr/lib/apache2/modules/mod_proxy.so
    </IfModule>
    <IfModule !mod_proxy_http.c>
        LoadModule proxy_http_module /usr/lib/apache2/modules/mod_proxy_http.so
    </IfModule>

    <Proxy *>
        Order deny,allow
        Allow from all
    </Proxy>

    RewriteLog /tmp/rewrite.log
    RewriteLogLevel 3

    SetEnv no-gzip on
    ProxyTimeout 999999999

    RequestHeader unset sec-username
    RequestHeader unset sec-roles

    RewriteEngine On
    RewriteRule ^/analytics$ /analytics/ [R]
    RewriteRule ^/cas$ /cas/ [R]
    RewriteRule ^/catalogapp$ /catalogapp/ [R]
    RewriteRule ^/downloadform$ /downloadform/ [R]
    RewriteRule ^/extractorapp$ /extractorapp/ [R]
    RewriteRule ^/extractorapp$ /extractorapp/ [R]
    RewriteRule ^/geonetwork$ /geonetwork/ [R]
    RewriteRule ^/geoserver2/(.*)$ /geoserver/$1 [R]
    RewriteRule ^/geoserver$ /geoserver/ [R]
    RewriteRule ^/geowebcache$ /geowebcache/ [R]
    RewriteRule ^/ldapadmin$ /ldapadmin/ [R]
    RewriteRule ^/mapfishapp$ /mapfishapp/ [R]
    RewriteRule ^/proxy$ /proxy/ [R]
    RewriteRule ^/static$ /static/ [R]

    ProxyPass /analytics/ ajp://localhost:8009/analytics/ 
    ProxyPassReverse /analytics/ ajp://localhost:8009/analytics/

    ProxyPass /cas/ ajp://localhost:8009/cas/ 
    ProxyPassReverse /cas/ ajp://localhost:8009/cas/

    ProxyPass /casfailed.jsp ajp://localhost:8009/casfailed.jsp 
    ProxyPassReverse /casfailed.jsp ajp://localhost:8009/casfailed.jsp

    ProxyPass /catalogapp/ ajp://localhost:8009/catalogapp/ 
    ProxyPassReverse /catalogapp/ ajp://localhost:8009/catalogapp/

    ProxyPass /downloadform/ ajp://localhost:8009/downloadform/ 
    ProxyPassReverse /downloadform/ ajp://localhost:8009/downloadform/

    ProxyPass /extractorapp/ ajp://localhost:8009/extractorapp/ 
    ProxyPassReverse /extractorapp/ ajp://localhost:8009/extractorapp/

    ProxyPass /geonetwork/ ajp://localhost:8009/geonetwork/ 
    ProxyPassReverse /geonetwork/ ajp://localhost:8009/geonetwork/

    ProxyPass /geonetwork-private/ ajp://localhost:8009/geonetwork-private/ 
    ProxyPassReverse /geonetwork-private/ ajp://localhost:8009/geonetwork-private/

    ProxyPass /geoserver/ ajp://localhost:8009/geoserver/ 
    ProxyPassReverse /geoserver/ ajp://localhost:8009/geoserver/

    ProxyPass /geowebcache/ ajp://localhost:8009/geowebcache/ 
    ProxyPassReverse /geowebcache/ ajp://localhost:8009/geowebcache/

    ProxyPass /j_spring_cas_security_check ajp://localhost:8009/j_spring_cas_security_check 
    ProxyPassReverse /j_spring_cas_security_check ajp://localhost:8009/j_spring_cas_security_check

    ProxyPass /j_spring_security_logout ajp://localhost:8009/j_spring_security_logout 
    ProxyPassReverse /j_spring_security_logout ajp://localhost:8009/j_spring_security_logout

    ProxyPass /ldapadmin/ ajp://localhost:8009/ldapadmin/
    ProxyPassReverse /ldapadmin/ ajp://localhost:8009/ldapadmin/

    ProxyPass /mapfishapp/ ajp://localhost:8009/mapfishapp/ 
    ProxyPassReverse /mapfishapp/ ajp://localhost:8009/mapfishapp/

    ProxyPass /proxy/ ajp://localhost:8009/proxy/ 
    ProxyPassReverse /proxy/ ajp://localhost:8009/proxy/

    ProxyPass /static/ ajp://localhost:8009/static/ 
    ProxyPassReverse /static/ ajp://localhost:8009/static/


    AddType application/vnd.ogc.context+xml .wmc
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 
Apache - SSL certificate
-----------------------

* private key generation (enter a passphrase)

        cd /var/www/georchestra/ssl
        sudo openssl genrsa -des3 -out georchestra.key 1024

* certificate generated for this key

        sudo openssl req -new -key georchestra.key -out georchestra.csr

* fill the form without providing a password

        Common Name (eg, YOUR name) []: put your server name (eg: vm-georchestra)

* create an unprotected key

        sudo openssl rsa -in georchestra.key -out georchestra-unprotected.key
        sudo openssl x509 -req -days 365 -in georchestra.csr -signkey georchestra.key -out georchestra.crt

* restart apache

        sudo service apache2 graceful
        
* update your hosts

        sudo nano /etc/hosts


        127.0.0.1       vm-georchestra

* testing

  * http://vm-georchestra
  * https://vm-georchestra

Tomcat
=========

Install Tomcat from package
---------------------------

This one Tomcat instance installation is for test purpose. When running a real-world SDI, you will need to use various Tomcat instances.

    sudo apt-get install tomcat6

Remove any webapp

	sudo rm -rf /var/lib/tomcat6/webapps/*
	
Create a directory for tomcat6 java preferences (to avoid a `WARNING: Couldn't flush user prefs: java.util.prefs.BackingStoreException: Couldn't get file lock.` error)

	sudo mkdir /usr/share/tomcat6/.java
	sudo chown tomcat6:tomcat6 /usr/share/tomcat6/.java


Environment variables
----------------------

In case of a 32G RAM server, add the following options at the end of the configuration file:

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS \
              -Dsun.java2d.opengl=true \
              -Djava.awt.headless=true \
              -Xms4G \
              -Xmx28G \
              -XX:MaxPermSize=256m "
```

Some geOrchestra applications will require you to add more JAVA_OPTS, read below...

Keystore/Trustore
-------------------

* Keystore creation (change the "mdpstore" password)

        cd /etc/tomcat6/
        sudo keytool -genkey -alias georchestra_localhost -keystore keystore -storepass mdpstore -keypass mdpstore -keyalg RSA -keysize 2048

    Put "localhost" in "first name and second name" since sec-proxy and CAS are on the same tomcat

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Quels sont vos prénom et nom ?
      [Unknown] :  localhost
    Quel est le nom de votre unité organisationnelle ?
      [Unknown] :
    Quelle est le nom de votre organisation ?
      [Unknown] :
    Quel est le nom de votre ville de résidence ?
      [Unknown] :
    Quel est le nom de votre état ou province ?
      [Unknown] :
    Quel est le code de pays ? deux lettres pour cette unit? ?
      [Unknown] :
    Est-ce CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown ?
      [non] :  oui
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
        keytool -keystore keystore -list
       
* truststore config

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=/etc/tomcat6/keystore -Djavax.net.ssl.trustStorePassword=mdpstore"
```

* connectors config

```
sudo nano /etc/tomcat6/server.xml
```

```
<Connector port="8080" protocol="HTTP/1.1"
   connectionTimeout="20000"
   URIEncoding="UTF-8"
   redirectPort="8443" />
```

```
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
   URIEncoding="UTF-8"
   maxThreads="150" scheme="https" secure="true"
   clientAuth="false"
   keystoreFile="/etc/tomcat6/keystore"
   keystorePass="mdpstore"
   compression="on"
   compressionMinSize="2048"
   noCompressionUserAgents="gozilla, traviata"
   compressableMimeType="text/html,text/xml,text/javascript,application/x-javascript,application/javascript,text/css" />
```

```
<Connector URIEncoding="UTF-8"
   port="8009"
   protocol="AJP/1.3"
   connectionTimeout="20000"
   redirectPort="8443" />
```
    
* Tomcat restart
 
        sudo service tomcat6 restart
    


GeoServer
=========

* Tomcat

Required JAVA_OPTS for GeoServer :

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF8 \
    -DGEOSERVER_DATA_DIR=/path/to/geoserver/data/dir \
    -DGEOWEBCACHE_CACHE_DIR=/path/to/geowebcache/cache/dir \
    -server \
    -XX:-UseParallelGC \
    -XX:SoftRefLRUPolicyMSPerMB=36000 \
    -XX:NewRatio=2 \
    -XX:+AggressiveOpts"
```

* Fonts

GeoServer uses the fonts available to the JVM for WMS styling.
You may have to install the "core fonts for the web" on your server if you need them.

	sudo apt-get install ttf-mscorefonts-installer

Restart your geoserver tomcat and check on /geoserver/web/?wicket:bookmarkablePage=:org.geoserver.web.admin.JVMFontsPage that these are loaded.


GeoNetwork
==========

Be sure to include those options in your tomcat JAVA_OPTS setup:

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -Dgeonetwork.dir=/path/to/geonetwork-data-dir \
    -Dgeonetwork[-private].schema.dir=/path/to/tomcat/webapps/geonetwork[-private]/WEB-INF/data/config/schema_plugins \
    -Dgeonetwork.jeeves.configuration.overrides.file=/path/to/tomcat/webapps/geonetwork[-private]/WEB-INF/config-overrides-georchestra.xml
```

... where brackets indicate optional strings, depending on your setup.


Extractorapp
============

Again, it is required to include custom options in your tomcat JAVA_OPTS setup:

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -Dorg.geotools.referencing.forceXY=true \
    -Dgeobretagne_production=true \
    -Dextractor.storage.dir=/path/to/temporary/extracts/
```

Note: if the epsg-extension module is installed, one can manage custom EPSG codes by adding:

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -DCUSTOM_EPSG_FILE=file://$CATALINA_BASE/conf/epsg.properties
```

... in which a sample epsg.properties file can be found [here](https://github.com/georchestra/georchestra/blob/master/server-deploy-support/src/main/resources/c2c/tomcat/conf/epsg.properties)

GDAL for GeoServer, Extractorapp & Mapfishapp
=============================================

Extractorapp **requires** GDAL and GDAL Java bindings libraries installed on the server.

GeoServer uses them to access more data formats, read http://docs.geoserver.org/latest/en/user/data/raster/gdal.html

Mapfishapp also optionally uses them for the file upload functionality, that allows to upload a vectorial data file to mapfishapp in order to display it as a layer. This functionnality in Mapfishapp relies normally on GeoTools, however, the supported file formats are limited (at 2013-10-17: shp, mif, gml and kml). If GDAL and GDAL Java bindings libraries are installed, the number of supported file formats is increased. This would give access, for example, to extra formats such as GPX and TAB.

The key element for calling the GDAL native library from mapfishapp is the **imageio-ext library** (see https://github.com/geosolutions-it/imageio-ext/wiki). It relies on:
 * jar files, that are included at build by maven,
 * a GDAL Java binding library, based on the JNI framework,
 * and obviously the GDAL library.

The latter can be installed, on Debian-based distributions, with the libgdal1 package:

    sudo apt-get install libgdal1

Some more work is needed for installing the GDAL Java binding library, as there is still no deb package for it (note that packages exist for ruby and perl bindings, hopefully the Java's one will be released soon - see a recent proposal http://ftp-master.debian.org/new/gdal_1.10.0-0%7Eexp3.html).

To quickly install the GDAL Java binding library on the server, download and extract the library and its data (see http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.7/native/gdal/ for the adequate distribution). 
Example for Debian Squeeze on amd64:

    wget http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.7/native/gdal/linux/gdal192-Ubuntu11-gcc4.5.2-x86_64.tar.gz -O /var/sig/gdal/NativeLibs/gdal_libs.tgz
    cd /var/sig/gdal/NativeLibs/ && tar xvzf gdal_libs.tgz
    
    wget http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.7/native/gdal/gdal-data.zip -O /var/sig/gdal/data.zip
    cd /var/sig/gdal/ && unzip data.zip

Next, you have to:
 - include the newly created directory /var/sig/gdal/NativeLibs/ in the `LD_LIBRARY_PATH` environment variable
 - create a GDAL_DATA environment variable (eg: export GDAL_DATA="/var/sig/gdal/data")

```
sudo nano /etc/default/tomcat6
```

```
LD_LIBRARY_PATH=/lib:/usr/lib/:/var/sig/gdal/NativeLibs/:$LD_LIBRARY_PATH
```

Another way to install the GDAL Java binding is building it from sources. See http://trac.osgeo.org/gdal/wiki/GdalOgrInJavaBuildInstructionsUnix.
