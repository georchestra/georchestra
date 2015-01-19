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
sudo a2dissite default 000-default default-ssl
```

In ```/etc/apache2/sites-available/georchestra```:
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

ProxyRequests Off
ProxyTimeout 999999999

AddType application/vnd.ogc.context+xml .wmc

ErrorDocument 502 /errors/50x.html
ErrorDocument 503 /errors/50x.html
```

* ```proxy.conf```: replace the ```http://my\.sdi\.org``` string with your server address in the following:

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

SetEnvIf Referer "^http://my\.sdi\.org/" mysdi
<Proxy http://localhost:8180/proxy/*>
    Order deny,allow
    Deny from all
    Allow from env=mysdi
</Proxy>
ProxyPass /proxy/ http://localhost:8180/proxy/ 
ProxyPassReverse /proxy/ http://localhost:8180/proxy/
```

* ```cas.conf```:

```
RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/cas/?.*$ 
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/analytics/?.*$ 
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/catalogapp/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/downloadform/?.*$ 
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/extractorapp/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/geonetwork/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/geoserver/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/geofence/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/geowebcache/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/ldapadmin/?.*$
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
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
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/mapfishapp/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
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

* Generate a private key (enter a good passphrase and keep it safe !)
```
sudo openssl genrsa -des3 2048 \
    -out /var/www/georchestra/ssl/georchestra.key
```

Protect it with:
```
sudo chmod 400 /var/www/georchestra/ssl/georchestra.key
```

* Generate a [Certificate Signing Request](http://en.wikipedia.org/wiki/Certificate_signing_request) (CSR) for this key, with eg:
```
sudo openssl req \
    -key /var/www/georchestra/ssl/georchestra.key \
    -subj "/C=FR/ST=None/L=None/O=None/OU=None/CN=vm-georchestra" \
    -newkey rsa:2048 -sha256 \
    -out /var/www/georchestra/ssl/georchestra.csr
```

Be sure to replace the ```/C=FR/ST=None/L=None/O=None/OU=None/CN=vm-georchestra``` string with something more relevant:
 * ```C``` is the 2 letter Country Name code
 * ```ST``` is the State or Province Name
 * ```L``` is the Locality Name (eg, city)
 * ```O``` is the Organization Name (eg, company)
 * ```OU``` is the Organizational Unit (eg, company department)
 * ```CN``` is the Common Name (***your server FQDN***)

* Create an unprotected key:
```
sudo openssl rsa \
    -in /var/www/georchestra/ssl/georchestra.key \
    -out /var/www/georchestra/ssl/georchestra-unprotected.key
```

 * Finally generate a self-signed certificate (CRT):
```
sudo openssl x509 -req \
    -days 365 \
    -in /var/www/georchestra/ssl/georchestra.csr \
    -signkey /var/www/georchestra/ssl/georchestra.key \
    -out /var/www/georchestra/ssl/georchestra.crt
```

* Restart the web server:
```
sudo service apache2 restart
``` 
