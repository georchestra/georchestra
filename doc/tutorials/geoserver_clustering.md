# Setup a cluster of GeoServer for high availability

In order to setup a cluster of GeoServer, we will need separate Virtual Machines (VMs).

For example with IPs :

- **Geoserver main** (*10.1.1.120*)
- **Geoserver LB1** (*10.1.1.121*)
- **Geoserver LB2** (*10.1.1.122*)

Geoserver main will allow to modify datadir (administration), while other will only serve OGC requests.

## Configure the servers

For this explanation, we will have 3 VMS on Debian Jessie configured with tomcat8 listenning on port 8080.

```
echo "deb http://ftp.fr.debian.org/debian jessie contrib non-free" >> /etc/apt/sources.list
echo "deb http://security.debian.org/ jessie/updates contrib non-free" >> /etc/apt/sources.list
apt-get update && apt-get install -y tomcat8 unzip libjai-core-java libjai-imageio-core-java ttf-mscorefonts-installer libgdal1h libgdal-java gdal-bin
wget http://downloads.sourceforge.net/project/libjpeg-turbo/1.4.0/libjpeg-turbo-official_1.4.0_amd64.deb?use_mirror=tenet -O /tmp/libjpeg-turbo-official_1.4.0_amd64.deb
dpkg -i /tmp/libjpeg-turbo-official_1.4.0_amd64.deb
wget https://github.com/bourgesl/marlin-renderer/releases/download/v0.4.5/marlin-0.4.5.jar -O /usr/share/java/marlin.jar
ln -s /usr/share/java/jai_core.jar /var/lib/tomcat8/lib/jai_core.jar
ln -s /usr/share/java/jai_codec.jar /var/lib/tomcat8/lib/jai_codec.jar
ln -s /usr/share/java/mlibwrapper_jai.jar /var/lib/tomcat8/lib/mlibwrapper_jai.jar
ln -s /usr/share/java/jai_imageio.jar /var/lib/tomcat8/lib/jai_imageio.jar
ln -s /usr/share/java/clibwrapper_jiio.jar /var/lib/tomcat8/lib/clibwrapper_jiio.jar
```

Those commands will install TC8 and optimisation for GS like TurboJPEG, Marlin Renderer, JAI , etc...

Then open the */etc/default/tomcat8* file and set **JAVA_OPTS** to : 

```
-Djava.awt.headless=true -XX:+UseConcMarkSweepGC -Xms2G -Xmx2G -XX:PermSize=256m -DGEOSERVER_DATA_DIR=/opt/geoserver_datadir -Dfile.encoding=UTF8 -Djavax.servlet.request.encoding=UTF-8 -Djavax.servlet.response.encoding=UTF-8 -server -XX:+UseParNewGC -XX:ParallelGCThreads=2 -XX:SoftRefLRUPolicyMSPerMB=36000 -XX:NewRatio=2 -XX:+AggressiveOpts -Djava.library.path=/usr/lib/jni:/opt/libjpeg-turbo/lib64
```

After that you'll have to upload your compiled war of GS in tomcat webapp directory, unzip it and replace the GDAL's wars by those we installed :

```
scp <user>@<devserverIP>:<pathtowars>/geoserver.war /var/lib/tomcat8/webapps/geoserver.war
unzip /var/lib/tomcat8/webapps/geoserver.war -d /var/lib/tomcat8/webapps/geoserver
chown -R tomcat8:tomcat8 /var/lib/tomcat8/webapps/geoserver
rm -f /var/lib/tomcat8/webapps/geoserver/WEB-INF/lib/imageio-ext-gdal-bindings-*.jar
cp /usr/share/java/gdal.jar /var/lib/tomcat8/webapps/geoserver/WEB-INF/lib
```

## Create and share the GS datadir

For have synchronized GS servers, we need them to share the same data directory, for that we will setup a NFS shared one.

On the **Main GS** : 

```
apt-get install nfs-common nfs-kernel-server git-core
mkdir /opt/geoserver_datadir
git clone https://github.com/georchestra/geoserver_minimal_datadir.git /opt/geoserver_datadir
chown -R nobody:nogroup /opt/geoserver_datadir
echo '/opt/geoserver_datadir 10.1.1.121(sync,rw,no_subtree_check)' >> /etc/exports
echo '/opt/geoserver_datadir 10.1.1.122(sync,rw,no_subtree_check)' >> /etc/exports
service nfs-kernel-server restart
```

On the **two other** (LB1 & LB2) :

```
mkdir /opt/geoserver_datadir
echo '10.1.1.120:/opt/geoserver_datadir /opt/geoserver_datadir nfs _netdev 0 0' >> /etc/fstab
mount -a
```

Now you should have the same data directory on each server synchronized by NFS.

## Reload the slaves configuration on configuration update

When you'll admin your main geoserver, the slaves have to reload the new configuration, for that on the main GS :

Create the */root/refresh.py* script : 

```
#!/usr/bin/env python
#-*- coding: utf-8 -*-

"""This is a script that we use to reload geoserver catalogs when load balancing                                                                                                                                                              them"""

"""
Copyright 2014 Camptocamp. All rights reserved.

Redistribution and use in source and binary forms, with or without modification,                                                                                                                                                              are
permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, thi                                                                                                                                                             s list of
      conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright notice,                                                                                                                                                              this list
      of conditions and the following disclaimer in the documentation and/or oth                                                                                                                                                             er materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY CAMPTOCAMP ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABI                                                                                                                                                             LITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL CAMPTOCAMP OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,                                                                                                                                                              OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE                                                                                                                                                              GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSE                                                                                                                                                             D AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDI                                                                                                                                                             NG
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVE                                                                                                                                                             N IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those                                                                                                                                                              of the
authors and should not be interpreted as representing official policies, either                                                                                                                                                              expressed
or implied, of Camptocamp.
"""

config = {
    'geoserver': [
        '10.1.1.121:8080',
        '10.1.1.122:8080',
        #'', # next one
    ]
}

# DO NOT MODIFY ANYTHING BELOW THIS #
# (unless you know what you're doing)

import cgi
import sys, os
import urllib2

#curl -H "sec-roles:ROLE_ADMINISTRATOR" -H 'sec-username:fake_user' -v -XPOST ht                                                                                                                                                             tp://127.0.0.1:8181/geoserver/rest/reload?recurse=true

headers = {
    "sec-roles": "ROLE_ADMINISTRATOR",
    "sec-username": "fake_user",
}

failures = []

for host in config['geoserver']:
    req = urllib2.Request("http://"+host+"/geoserver/rest/reload?recurse=true",                                                                                                                                                              "nothing", headers)
    try:
        r = urllib2.urlopen(req)
    except urllib2.URLError as e:
        failures.append((host, e.reason))


if len(failures) == 0:
    print "Status: 200 OK"
    print ""
    print "Reload OK"
else:
    print "Status: 500 Internal Server Error"
    print ""
    for f in failures:
        print "Reload failed for host %s, reason is '%s'" %  f
```

This script has been developped by **fvanderbiest** and the original one is available here : https://gist.github.com/fvanderbiest/f5d5e467c7ca004ce73b

Then let say to debian to call it when configuration is updated :

```
apt-get install incron
echo 'root' > /etc/incron.allow
incrontab -e
```

In the file openned by the icrontab -e command put : 

```
/opt/geoserver_datadir/global.xml IN_MODIFY /root/refresh.py
```

And restart the incron service :

```
service incron restart
```

## Setup the load-balancing with Nginx

For the load balancing, we'll use Nginx : 

```
apt-get install nginx
```

And open the */etc/nginx/sites-available/default* file and replace content with :

```
upstream gs_servers{
	ip_hash;
	server 127.0.0.1:8080;
	server 10.1.1.121:8080;
	server 10.1.1.122:8080;
}

server {
	listen 80;
	server_name georchestra.mydomain.org;
	
	access_log /etc/nginx/logs/geoserver-access.log combined;
	error_log /etc/nginx/logs/geoserver-error.log;
	
	location ~ ^/(geoserver/web|geoserver/rest)(/?).*$ {
		proxy_pass http://127.0.0.1:8080$request_uri;
		proxy_redirect off;
		
		proxy_set_header Host $host;
		proxy_set_header X-Real-IP $remote_addr;
		proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
		proxy_max_temp_file_size 0;
		client_max_body_size 500M;
		client_body_buffer_size 128k;
		proxy_connect_timeout 90;
		proxy_send_timeout 90;
		proxy_read_timeout 90;
		proxy_buffer_size 4k;
		proxy_buffers 4 32k;
		proxy_busy_buffers_size 64k;
		proxy_temp_file_write_size 64k;
	}
	
	location ~ ^/(geoserver)(/?).*$ {
		proxy_pass http://gs_servers$request_uri;
		proxy_redirect off;
		
		proxy_set_header Host $host;
		proxy_set_header X-Real-IP $remote_addr;
		proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
		proxy_max_temp_file_size 0;
		client_max_body_size 500M;
		client_body_buffer_size 128k;
		proxy_connect_timeout 90;
		proxy_send_timeout 90;
		proxy_read_timeout 90;
		proxy_buffer_size 4k;
		proxy_buffers 4 32k;
		proxy_busy_buffers_size 64k;
		proxy_temp_file_write_size 64k;
	}
}
```

Note : In this example, nginx use the **ip_hash** method to load balance trafic, so a single user will always be redirected on the same server, but you can use others method.

Available methods are : 

**round-robin** -> requests to the application servers are distributed in a round-robin fashion

**least-connected** -> next request is assigned to the server with the least number of active connections

**ip-hash** -> a hash-function is used to determine what server should be selected for the next request (based on the client’s IP address)

Now you can fill your GenerateConfig.groovy with : 

```
<entry key="geoserver" value="http://10.1.1.120:80/geoserver/" />
```

for the proxy geoserver's key.

Enjoy the new High Availability and better performance with load balancing !
