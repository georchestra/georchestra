# Setting up Apache


## Modules setup

```
sudo a2enmod proxy_connect proxy_http proxy ssl rewrite headers
sudo service apache2 restart
```


## Directory structure

```
sudo mkdir -p /var/www/georchestra/htdocs
sudo mkdir /var/www/georchestra/conf 
sudo mkdir /var/www/georchestra/logs 
sudo mkdir /var/www/georchestra/ssl
```

Debian apache user being www-data, we have to grant write on logs to www-data:
```
sudo chgrp www-data /var/www/georchestra/logs/
sudo chmod g+w /var/www/georchestra/logs/
```

We publish sample files for your htdocs folder in the [georchestra/htdocs](https://github.com/georchestra/htdocs) repository:
```
cd /var/www/georchestra/
sudo git clone https://github.com/georchestra/htdocs.git
```
It is recommended to edit them to match your setup.



## VirtualHosts

Let's first deactivate the default virtualhosts, and create ours:
```
sudo a2dissite default default-ssl
```

In /etc/apache2/sites-available/georchestra.conf:
```
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
```


Update your hosts files (in /etc/hosts):
```
127.0.0.1       vm-georchestra
```

Once this is done, enable the georchestra site with:
```
sudo a2ensite georchestra
```


## Configuration

In ```/var/www/georchestra/conf/```, we will create several files, which will be loaded by both virtualhosts.
In these files, we will make the assumption that the tomcat hosting the security proxy listens on port 8180. You may need to adapt this to your setup.

Three of these files are required:

* ```global.conf```:

```
<IfModule !mod_proxy.c>
    LoadModule proxy_module /usr/lib/apache2/modules/mod_proxy.so
</IfModule>
<IfModule !mod_proxy_http.c>
    LoadModule proxy_http_module /usr/lib/apache2/modules/mod_proxy_http.so
</IfModule>

RewriteEngine On

SetEnv no-gzip on
ProxyTimeout 999999999

AddType application/vnd.ogc.context+xml .wmc

ErrorDocument 502 /errors/50x.html
ErrorDocument 503 /errors/50x.html
```

* ```proxy.conf```:

```
RewriteRule ^/proxy$ /proxy/ [R]

ProxyPass /casfailed.jsp http://localhost:8180/casfailed.jsp
ProxyPassReverse /casfailed.jsp http://localhost:8180/casfailed.jsp

ProxyPass /j_spring_cas_security_check http://localhost:8180/j_spring_cas_security_check 
ProxyPassReverse /j_spring_cas_security_check http://localhost:8180/j_spring_cas_security_check

ProxyPass /j_spring_security_logout http://localhost:8180/j_spring_security_logout 
ProxyPassReverse /j_spring_security_logout http://localhost:8180/j_spring_security_logout

ProxyPass /gateway http://localhost:8180/gateway
ProxyPassReverse /gateway http://localhost:8180/gateway

ProxyPass /testPage http://localhost:8180/testPage
ProxyPassReverse /testPage http://localhost:8180/testPage

<Proxy http://localhost:8180/_static/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /_static/ http://localhost:8180/_static/
ProxyPassReverse /_static/ http://localhost:8180/_static/

<Proxy http://localhost:8180/proxy/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /proxy/ http://localhost:8180/proxy/ 
ProxyPassReverse /proxy/ http://localhost:8180/proxy/
```

* ```cas.conf```:

```
RewriteRule ^/cas$ /cas/ [R]
<Proxy http://localhost:8180/cas/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /cas/ http://localhost:8180/cas/ 
ProxyPassReverse /cas/ http://localhost:8180/cas/
```

For the other ones, pick only those you need, depending on the modules you plan to install:

* ```analytics.conf```:

```
RewriteRule ^/analytics$ /analytics/ [R]
<Proxy http://localhost:8180/analytics/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /analytics/ http://localhost:8180/analytics/ 
ProxyPassReverse /analytics/ http://localhost:8180/analytics/
```

* ```catalogapp.conf```:

```
RewriteRule ^/catalogapp$ /catalogapp/ [R]
<Proxy http://localhost:8180/catalogapp/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /catalogapp/ http://localhost:8180/catalogapp/ 
ProxyPassReverse /catalogapp/ http://localhost:8180/catalogapp/
```

* ```downloadform.conf```:

```
RewriteRule ^/downloadform$ /downloadform/ [R]
<Proxy http://localhost:8180/downloadform/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /downloadform/ http://localhost:8180/downloadform/ 
ProxyPassReverse /downloadform/ http://localhost:8180/downloadform/
```

* ```extractorapp.conf```:

```
RewriteRule ^/extractorapp$ /extractorapp/ [R]
RewriteRule ^/extractorapp/admin$ /extractorapp/admin/ [R]
<Proxy http://localhost:8180/extractorapp/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /extractorapp/ http://localhost:8180/extractorapp/ 
ProxyPassReverse /extractorapp/ http://localhost:8180/extractorapp/
```

* ```geonetwork.conf```:

```
RewriteRule ^/geonetwork$ /geonetwork/ [R]
<Proxy http://localhost:8180/geonetwork/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /geonetwork/ http://localhost:8180/geonetwork/ 
ProxyPassReverse /geonetwork/ http://localhost:8180/geonetwork/
```

* ```geoserver.conf```:

```
RewriteRule ^/geoserver$ /geoserver/ [R]
<Proxy http://localhost:8180/geoserver/*>
    Order deny,allow
    Allow from all
</Proxy>
<Location /geoserver>
  Header set Access-Control-Allow-Origin "*"
  Header set Access-Control-Allow-Headers "Origin, X-Requested-With, Content-Type, Accept"
</Location>
ProxyPass /geoserver/ http://localhost:8180/geoserver/ 
ProxyPassReverse /geoserver/ http://localhost:8180/geoserver/
```

* ```geofence.conf```:

```
RewriteRule ^/geofence$ /geofence/ [R]
<Proxy http://localhost:8180/geofence/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /geofence/ http://localhost:8180/geofence/ 
ProxyPassReverse /geofence/ http://localhost:8180/geofence/
```

* ```geowebcache.conf```:

```
RewriteRule ^/geowebcache$ /geowebcache/ [R]
<Proxy http://localhost:8180/geowebcache/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /geowebcache/ http://localhost:8180/geowebcache/ 
ProxyPassReverse /geowebcache/ http://localhost:8180/geowebcache/
```

* ```header.conf```:

```
RewriteRule ^/header$ /header/ [R]
<Proxy http://localhost:8180/header/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /header/ http://localhost:8180/header/
ProxyPassReverse /header/ http://localhost:8180/header/
```

* ```ldapadmin.conf```:

```
RewriteRule ^/ldapadmin$ /ldapadmin/ [R]
RewriteRule ^/ldapadmin/privateui$ /ldapadmin/privateui/ [R]
<Proxy http://localhost:8180/ldapadmin/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /ldapadmin/ http://localhost:8180/ldapadmin/
ProxyPassReverse /ldapadmin/ http://localhost:8180/ldapadmin/
```

* ```mapfishapp.conf```:

```
RewriteRule ^/mapfishapp$ /mapfishapp/ [R]
<Proxy http://localhost:8180/mapfishapp/*>
    Order deny,allow
    Allow from all
</Proxy>
ProxyPass /mapfishapp/ http://localhost:8180/mapfishapp/ 
ProxyPassReverse /mapfishapp/ http://localhost:8180/mapfishapp/
```


## SSL certificate

The SSL certificate is absolutely required, at least for the CAS module, if not for the whole SDI.

* private key generation (enter a passphrase)
```
cd /var/www/georchestra/ssl
sudo openssl genrsa -des3 -out georchestra.key 1024
```

* certificate generated for this key
```
sudo openssl req -new -key georchestra.key -out georchestra.csr
```

Fill the form without providing a password, and when asked for the Common Name, fill the server FQDN (here: vm-georchestra)
```
Common Name (eg, YOUR name) []: put your server name (eg: vm-georchestra)
```

* create an unprotected key
```
sudo openssl rsa -in georchestra.key -out georchestra-unprotected.key
sudo openssl x509 -req -days 365 -in georchestra.csr -signkey georchestra.key -out georchestra.crt
```

* restart apache
```
sudo service apache2 restart
``` 
