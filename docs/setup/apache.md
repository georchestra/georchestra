# Setting up Apache

We assume here that SSL is used for the whole SDI.  
Having a mixed SSL/non-SSL setup should work, but will require several changes in your configuration.


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
    RewriteCond %{HTTPS} off 
    RewriteRule (.*) https://%{HTTP_HOST}%{REQUEST_URI} 
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
    # alternate setup with letsencrypt:
    #SSLCertificateFile /etc/letsencrypt/live/mysdi.org/cert.pem
    #SSLCertificateKeyFile /etc/letsencrypt/live/mysdi.org/privkey.pem
    #SSLCertificateChainFile /etc/letsencrypt/live/mysdi.org/fullchain.pem
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

SetEnvIf Referer "^https://my\.sdi\.org/" mysdi
<Proxy http://localhost:8180/proxy/*>
    Require env mysdi
</Proxy>
ProxyPass /proxy/ http://localhost:8180/proxy/ 
ProxyPassReverse /proxy/ http://localhost:8180/proxy/
```

* ```cas.conf```:

The cas module should be accessed only through https.
```

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
<Proxy http://localhost:8180/analytics/*>
    Require all granted
</Proxy>
ProxyPass /analytics/ http://localhost:8180/analytics/ 
ProxyPassReverse /analytics/ http://localhost:8180/analytics/
```


* ```catalogapp.conf```:

```
<Proxy http://localhost:8180/catalogapp/*>
    Require all granted
</Proxy>
ProxyPass /catalogapp/ http://localhost:8180/catalogapp/ 
ProxyPassReverse /catalogapp/ http://localhost:8180/catalogapp/
```


* ```downloadform.conf```:

```
<Proxy http://localhost:8180/downloadform/*>
    Require all granted
</Proxy>
ProxyPass /downloadform/ http://localhost:8180/downloadform/ 
ProxyPassReverse /downloadform/ http://localhost:8180/downloadform/
```


* ```extractorapp.conf```:

```
<Proxy http://localhost:8180/extractorapp/*>
    Require all granted
</Proxy>
ProxyPass /extractorapp/ http://localhost:8180/extractorapp/ 
ProxyPassReverse /extractorapp/ http://localhost:8180/extractorapp/
```


* ```geonetwork.conf```:

```
<Proxy http://localhost:8180/geonetwork/*>
    Require all granted
</Proxy>
ProxyPass /geonetwork/ http://localhost:8180/geonetwork/ 
ProxyPassReverse /geonetwork/ http://localhost:8180/geonetwork/
```


* ```geoserver.conf```:

```
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


* ```geofence.conf```:

```
<Proxy http://localhost:8180/geofence/*>
    Require all granted
</Proxy>
ProxyPass /geofence/ http://localhost:8180/geofence/ 
ProxyPassReverse /geofence/ http://localhost:8180/geofence/
```


* ```geowebcache.conf```:

```
<Proxy http://localhost:8180/geowebcache/*>
    Require all granted
</Proxy>
ProxyPass /geowebcache/ http://localhost:8180/geowebcache/ 
ProxyPassReverse /geowebcache/ http://localhost:8180/geowebcache/
```


* ```header.conf```:

```
<Proxy http://localhost:8180/header/*>
    Require all granted
</Proxy>
ProxyPass /header/ http://localhost:8180/header/
ProxyPassReverse /header/ http://localhost:8180/header/
```


* ```ldapadmin.conf```:

```

<Proxy http://localhost:8180/ldapadmin/*>
    Require all granted
</Proxy>
ProxyPass /ldapadmin/ http://localhost:8180/ldapadmin/
ProxyPassReverse /ldapadmin/ http://localhost:8180/ldapadmin/
```

* ```mapfishapp.conf```:

```
<Proxy http://localhost:8180/mapfishapp/*>
    Require all granted
</Proxy>
ProxyPass /mapfishapp/ http://localhost:8180/mapfishapp/ 
ProxyPassReverse /mapfishapp/ http://localhost:8180/mapfishapp/
```


## SSL certificate

An SSL certificate is absolutely required, at least for the CAS module.
We recommend using a certificate issued by [Let's Encrypt](https://letsencrypt.org/), but one may also use a [self-signed certificate](../tutorials/self-signed-certificate.md).
