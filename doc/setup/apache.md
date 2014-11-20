# Setting up Apache


* modules setup

        sudo a2enmod proxy_connect proxy_http proxy ssl rewrite headers
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

* Error documents (useful when tomcat restarts for maintenance)

        mkdir -p /var/www/georchestra/htdocs/errors
        wget http://sdi.georchestra.org/errors/50x.html -O /var/www/georchestra/htdocs/errors/50x.html


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

    RewriteLog /tmp/rewrite.log
    RewriteLogLevel 3

    SetEnv no-gzip on
    ProxyTimeout 999999999
    
    AddType application/vnd.ogc.context+xml .wmc

    RewriteEngine On
    RewriteRule ^/analytics$ /analytics/ [R]
    RewriteRule ^/cas$ /cas/ [R]
    RewriteRule ^/catalogapp$ /catalogapp/ [R]
    RewriteRule ^/downloadform$ /downloadform/ [R]
    RewriteRule ^/extractorapp$ /extractorapp/ [R]
    RewriteRule ^/extractorapp/admin$ /extractorapp/admin/ [R]
    RewriteRule ^/geonetwork$ /geonetwork/ [R]
    RewriteRule ^/geoserver$ /geoserver/ [R]
    RewriteRule ^/geofence$ /geofence/ [R]
    RewriteRule ^/geowebcache$ /geowebcache/ [R]
    RewriteRule ^/header$ /header/ [R]
    RewriteRule ^/ldapadmin$ /ldapadmin/ [R]
    RewriteRule ^/ldapadmin/privateui$ /ldapadmin/privateui/ [R]
    RewriteRule ^/mapfishapp$ /mapfishapp/ [R]
    RewriteRule ^/proxy$ /proxy/ [R]
    
    ErrorDocument 502 /errors/50x.html
    ErrorDocument 503 /errors/50x.html

    ProxyPass /casfailed.jsp ajp://localhost:8009/casfailed.jsp 
    ProxyPassReverse /casfailed.jsp ajp://localhost:8009/casfailed.jsp

    ProxyPass /j_spring_cas_security_check ajp://localhost:8009/j_spring_cas_security_check 
    ProxyPassReverse /j_spring_cas_security_check ajp://localhost:8009/j_spring_cas_security_check

    ProxyPass /j_spring_security_logout ajp://localhost:8009/j_spring_security_logout 
    ProxyPassReverse /j_spring_security_logout ajp://localhost:8009/j_spring_security_logout

    <Proxy ajp://localhost:8009/analytics/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /analytics/ ajp://localhost:8009/analytics/ 
    ProxyPassReverse /analytics/ ajp://localhost:8009/analytics/

    <Proxy ajp://localhost:8009/cas/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /cas/ ajp://localhost:8009/cas/ 
    ProxyPassReverse /cas/ ajp://localhost:8009/cas/

    <Proxy ajp://localhost:8009/catalogapp/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /catalogapp/ ajp://localhost:8009/catalogapp/ 
    ProxyPassReverse /catalogapp/ ajp://localhost:8009/catalogapp/

    <Proxy ajp://localhost:8009/downloadform/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /downloadform/ ajp://localhost:8009/downloadform/ 
    ProxyPassReverse /downloadform/ ajp://localhost:8009/downloadform/

    <Proxy ajp://localhost:8009/extractorapp/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /extractorapp/ ajp://localhost:8009/extractorapp/ 
    ProxyPassReverse /extractorapp/ ajp://localhost:8009/extractorapp/

    <Proxy ajp://localhost:8009/geonetwork/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /geonetwork/ ajp://localhost:8009/geonetwork/ 
    ProxyPassReverse /geonetwork/ ajp://localhost:8009/geonetwork/

    <Proxy ajp://localhost:8009/geonetwork-private/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /geonetwork-private/ ajp://localhost:8009/geonetwork-private/ 
    ProxyPassReverse /geonetwork-private/ ajp://localhost:8009/geonetwork-private/

    <Proxy ajp://localhost:8009/geoserver/*>
        Order deny,allow
        Allow from all
    </Proxy>
    <Location /geoserver>
      Header set Access-Control-Allow-Origin "*"
      Header set Access-Control-Allow-Headers "Origin, X-Requested-With, Content-Type, Accept"
      ProxyPass /geoserver/ ajp://localhost:8009/geoserver/ 
      ProxyPassReverse /geoserver/ ajp://localhost:8009/geoserver/
    </Location>

    <Proxy ajp://localhost:8009/geowebcache/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /geowebcache/ ajp://localhost:8009/geowebcache/ 
    ProxyPassReverse /geowebcache/ ajp://localhost:8009/geowebcache/


    <Proxy ajp://localhost:8009/geofence/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /geofence/ ajp://localhost:8009/geofence/ 
    ProxyPassReverse /geofence/ ajp://localhost:8009/geofence/


    <Proxy ajp://localhost:8009/ldapadmin/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /ldapadmin/ ajp://localhost:8009/ldapadmin/
    ProxyPassReverse /ldapadmin/ ajp://localhost:8009/ldapadmin/

    <Proxy ajp://localhost:8009/mapfishapp/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /mapfishapp/ ajp://localhost:8009/mapfishapp/ 
    ProxyPassReverse /mapfishapp/ ajp://localhost:8009/mapfishapp/

    <Proxy ajp://localhost:8009/proxy/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /proxy/ ajp://localhost:8009/proxy/ 
    ProxyPassReverse /proxy/ ajp://localhost:8009/proxy/

    <Proxy ajp://localhost:8009/header/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /header/ ajp://localhost:8009/header/
    ProxyPassReverse /header/ ajp://localhost:8009/header/

    <Proxy ajp://localhost:8009/_static/*>
        Order deny,allow
        Allow from all
    </Proxy>
    ProxyPass /_static/ ajp://localhost:8009/_static/
    ProxyPassReverse /_static/ ajp://localhost:8009/_static/

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 
## SSL certificate


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