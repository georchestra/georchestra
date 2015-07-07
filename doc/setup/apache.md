# Setting up Apache


## Modules setup

```
sudo a2enmod proxy proxy_http ssl rewrite headers deflate
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

In ```/etc/apache2/sites-available/georchestra.conf```:
```
<VirtualHost *:80>
    ServerName georchestra.mydomain.org
    DocumentRoot /var/www/georchestra/htdocs
    LogLevel warn
    ErrorLog /var/www/georchestra/logs/error.log
    CustomLog /var/www/georchestra/logs/access.log "combined"
    Include /var/www/georchestra/conf/*.conf
    ServerSignature Off
</VirtualHost>
<VirtualHost *:443>
    ServerName georchestra.mydomain.org
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
127.0.0.1       georchestra.mydomain.org
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

AddOutputFilterByType DEFLATE text/html text/plain text/xml application/xml text/css text/javascript application/javascript

ProxyRequests Off
ProxyTimeout 86400

AddType application/vnd.ogc.context+xml .wmc

ErrorDocument 502 /errors/50x.html
ErrorDocument 503 /errors/50x.html
```

* ```proxy.conf```: replace the ```http://my\.sdi\.org``` string with your server address in the following:

```
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
    Require all granted
</Proxy>
ProxyPass /_static/ http://localhost:8180/_static/
ProxyPassReverse /_static/ http://localhost:8180/_static/

SetEnvIf Referer "^http://my\.sdi\.org/" mysdi
<Proxy http://localhost:8180/proxy/*>
    Require env mysdi
</Proxy>
ProxyPass /proxy/ http://localhost:8180/proxy/ 
ProxyPassReverse /proxy/ http://localhost:8180/proxy/
```

* ```cas.conf```:

The cas module should be accessed only through https.
```
RewriteRule ^/cas$ /cas/ [R]

<Proxy http://localhost:8180/cas/*>
    Require all granted
</Proxy>
ProxyPass /cas/ http://localhost:8180/cas/ 
ProxyPassReverse /cas/ http://localhost:8180/cas/

RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/cas/?.*$ 
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
```

For the other ones, pick only those you need, depending on the modules you plan to install:

* ```analytics.conf```:

```
RewriteRule ^/analytics$ /analytics/ [R]
<Proxy http://localhost:8180/analytics/*>
    Require all granted
</Proxy>
ProxyPass /analytics/ http://localhost:8180/analytics/ 
ProxyPassReverse /analytics/ http://localhost:8180/analytics/
```

In addition, if you would like to encrypt all communications to the analytics webapp, you have to add the following:
```
RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/analytics/?.*$ 
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
```

Or if you prefer to access analytics through http only, choose the following:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/analytics/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```

* ```catalogapp.conf```:

```
RewriteRule ^/catalogapp$ /catalogapp/ [R]
<Proxy http://localhost:8180/catalogapp/*>
    Require all granted
</Proxy>
ProxyPass /catalogapp/ http://localhost:8180/catalogapp/ 
ProxyPassReverse /catalogapp/ http://localhost:8180/catalogapp/
```

And if your SDI is primarily accessed through unsecured http:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/catalogapp/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```

* ```downloadform.conf```:

```
RewriteRule ^/downloadform$ /downloadform/ [R]
<Proxy http://localhost:8180/downloadform/*>
    Require all granted
</Proxy>
ProxyPass /downloadform/ http://localhost:8180/downloadform/ 
ProxyPassReverse /downloadform/ http://localhost:8180/downloadform/
```

In addition, if you would like to encrypt all communications to the downloadform webapp, you have to add the following:
```
RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/downloadform/?.*$ 
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
```

Or if you prefer to access downloadform through http only, choose the following:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/downloadform/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```


* ```extractorapp.conf```:

```
RewriteRule ^/extractorapp$ /extractorapp/ [R]
RewriteRule ^/extractorapp/admin$ /extractorapp/admin/ [R]
<Proxy http://localhost:8180/extractorapp/*>
    Require all granted
</Proxy>
ProxyPass /extractorapp/ http://localhost:8180/extractorapp/ 
ProxyPassReverse /extractorapp/ http://localhost:8180/extractorapp/
```

And if your SDI is primarily accessed through unsecured http:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/extractorapp/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```

* ```geonetwork.conf```:

```
RewriteRule ^/geonetwork$ /geonetwork/ [R]
<Proxy http://localhost:8180/geonetwork/*>
    Require all granted
</Proxy>
ProxyPass /geonetwork/ http://localhost:8180/geonetwork/ 
ProxyPassReverse /geonetwork/ http://localhost:8180/geonetwork/
```

And if your SDI is primarily accessed through unsecured http:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/geonetwork/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```

* ```geoserver.conf```:

```
RewriteRule ^/geoserver$ /geoserver/ [R]
<Proxy http://localhost:8180/geoserver/*>
    Require all granted
</Proxy>
<Location /geoserver>
  Header set Access-Control-Allow-Origin "*"
  Header set Access-Control-Allow-Headers "Origin, X-Requested-With, Content-Type, Accept"
</Location>
ProxyPass /geoserver/ http://localhost:8180/geoserver/ 
ProxyPassReverse /geoserver/ http://localhost:8180/geoserver/
```

And if your SDI is primarily accessed through unsecured http:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/geoserver/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```

* ```geofence.conf```:

```
RewriteRule ^/geofence$ /geofence/ [R]
<Proxy http://localhost:8180/geofence/*>
    Require all granted
</Proxy>
ProxyPass /geofence/ http://localhost:8180/geofence/ 
ProxyPassReverse /geofence/ http://localhost:8180/geofence/
```

And if your SDI is primarily accessed through unsecured http:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/geofence/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```

* ```geowebcache.conf```:

```
RewriteRule ^/geowebcache$ /geowebcache/ [R]
<Proxy http://localhost:8180/geowebcache/*>
    Require all granted
</Proxy>
ProxyPass /geowebcache/ http://localhost:8180/geowebcache/ 
ProxyPassReverse /geowebcache/ http://localhost:8180/geowebcache/
```

And if your SDI is primarily accessed through unsecured http:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/geowebcache/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```

* ```header.conf```:

```
RewriteRule ^/header$ /header/ [R]
<Proxy http://localhost:8180/header/*>
    Require all granted
</Proxy>
ProxyPass /header/ http://localhost:8180/header/
ProxyPassReverse /header/ http://localhost:8180/header/
```

Note that the Header module may be accessed through http or https, we're not forcing anything here.


* ```ldapadmin.conf```:

Since the ldapadmin webapp handles user accounts, all communications should be encrypted here.
```
RewriteRule ^/ldapadmin$ /ldapadmin/ [R]
RewriteRule ^/ldapadmin/privateui$ /ldapadmin/privateui/ [R]

<Proxy http://localhost:8180/ldapadmin/*>
    Require all granted
</Proxy>
ProxyPass /ldapadmin/ http://localhost:8180/ldapadmin/
ProxyPassReverse /ldapadmin/ http://localhost:8180/ldapadmin/

RewriteCond %{HTTPS} off
RewriteCond %{REQUEST_URI} ^/ldapadmin/?.*$
RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]
```

* ```mapfishapp.conf```:

```
RewriteRule ^/mapfishapp$ /mapfishapp/ [R]
<Proxy http://localhost:8180/mapfishapp/*>
    Require all granted
</Proxy>
ProxyPass /mapfishapp/ http://localhost:8180/mapfishapp/ 
ProxyPassReverse /mapfishapp/ http://localhost:8180/mapfishapp/
```

And if your SDI is primarily accessed through unsecured http:
```
RewriteCond %{HTTPS} on
RewriteCond %{REQUEST_URI} ^/mapfishapp/?.*$ 
RewriteRule ^/(.*)$ http://%{SERVER_NAME}/$1 [R=301,L]
```


## SSL certificate

The SSL certificate is absolutely required, at least for the CAS module, if not for the whole SDI.

* Generate a private key (enter a good passphrase and keep it safe !)
```
sudo openssl genrsa -des3 \
    -out /var/www/georchestra/ssl/georchestra.key 2048
```

Protect it with:
```
sudo chmod 400 /var/www/georchestra/ssl/georchestra.key
```

* Generate a [Certificate Signing Request](http://en.wikipedia.org/wiki/Certificate_signing_request) (CSR) for this key, with eg:
```
sudo openssl req \
    -key /var/www/georchestra/ssl/georchestra.key \
    -subj "/C=FR/ST=None/L=None/O=None/OU=None/CN=georchestra.mydomain.org" \
    -newkey rsa:2048 -sha256 \
    -out /var/www/georchestra/ssl/georchestra.csr
```

Be sure to replace the ```/C=FR/ST=None/L=None/O=None/OU=None/CN=georchestra.mydomain.org``` string with something more relevant:
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
