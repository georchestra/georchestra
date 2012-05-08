geOrchestra installation guide
===============================

Introduction
---------------

This guide is meant to succeed in installing geOrchestra into a libvirt / KVM image,
it has been run into an ubuntu 11.10 64bit host setup, with virt-manager

The war archives used into the VM are those coming from the continuous build:
http://applis-bretagne.fr/hudson/job/georchestra/

The main goal of this exercise was to try a "time attack" setup, my previous record was around a half day.
 
0. begin of the setup around 8:45 PM

1. Create a new vm using the KVM interface (virt-manager)

2. Install a debian squeeze into the newly created guest
administrator: root/georchestra
regular user: georchestra/georchestra

Side note: keyboard is configured on french keymap (azerty)
password translated into qwerty would then be "georchestrq"
 
3. During the guest setup, checkout the pre-generated archives

4. Setup of the guest VM

* Selecting [*] web server during the installation installs apache2, let's switch to nginx

You now have a completely virgin debian squeeze VM. The hard part begins


Installing some requirements
------------------------------


$ apt-get remove --purge apache2*

$ apt-get install nginx

We also would need tomcat, obviously:

$ apt-get install tomcat6

We might also need to use java VM from sun, but ... let's have some fun and try with openjdk for now.

We need a LDAP server

$ apt-get install slapd

Here is the tricky part
asking for the administrator password of the ldap tree: georchestra
It creates a default tree, which we may not be interested in ...

It would be better to have the client tools for ldap:

$ apt-get install ldap-utils

Now, we need to figure out what exactly did the post-setup scripts from debian:

$ vi /etc/ldap/slapd.d/cn=config/olcDatabase={1}hdb.ldif

Okay, it created a default database with a root dn as follows:

dc=nodomain
|-cn=admin,dc=nodomain

Let's modify so that we have dc=georchestra,dc=org as rootDN

($ apt-get install vim if it has not been done yet ;-))

Time to create the default tree:

$ cd
$ mkdir georchestra-ldap
$ cd georchestra-ldap
$ wget http://svn.georchestra.org/georchestra/trunk/samples/georchestra-root.ldif
$ wget http://svn.georchestra.org/georchestra/trunk/samples/georchestra.ldif
(optional: )
$ wget http://svn.georchestra.org/georchestra/trunk/samples/gidnumber-uniqueness.ldif

Different cases are explained into http://svn.georchestra.org/georchestra/trunk/samples/README,
on how to start with slapd and geOrchestra, managing a LDAP tree from the beginning (especially slapd) could be painfull.

Load the root DN (the root of our tree) :

$ ldapadd -Dcn=admin,dc=georchestra,dc=org -f georchestra-root.ldif -x -c -W
Enter LDAP Password: georchestra

Load the sample data (default groups / users) :

$ ldapadd -Dcn=admin,dc=georchestra,dc=org -f georchestra.ldif -x -c -W
...

you should see a lot of "adding new entry ..." in output

Test if everything is ok:

$ ldapsearch -x -bdc=georchestra,dc=org | less

you should have an output with
numResponses: 23
numEntries: 22

PostGreSQL / PostGIS installation
-----------------------------------

$ apt-get install postgresql postgresql-8.4-postgis postgis

Create a database for GeoNetwork

$ su postgres
$ createdb geonetwork
(I cannot remember if postgis is necessary for GeoNetwork, but I guess so, in doubt ...)
$ createlang plpgsql geonetwork
$ psql -f /usr/share/postgresql/8.4/contrib/postgis-1.5/postgis.sql geonetwork
$ psql -f /usr/share/postgresql/8.4/contrib/postgis-1.5/spatial_ref_sys.sql geonetwork

$ createuser www-data
$ psql geonetwork
> ALTER TABLE spatial_ref_sys   OWNER TO "www-data";
> ALTER TABLE geometry_columns  OWNER TO "www-data";
> ALTER TABLE geography_columns OWNER TO "www-data";
> ALTER USER "www-data" WITH PASSWORD 'www-data';

First deploy of the webapps
-----------------------------

At this time of reading, it is not meant to work in a first shot (in a perfect world with an up-to-date configuration profile maybe, but that is not the case for now), but we obviously need to get some debugging log traces to know where to start.

* Upload the previously downloaded wars onto the vm

* Rename each ones by removing the -vmware suffix
(i.e. "ROOT-vmware.war" becomes "ROOT.war", but keep the "-private" one)

* Rename geoserver-vmware.war to geoserver-private.war
(we are doing it so, because usually geoserver runs in its own tomcat, here we are going to deploy it with the other apps, so it needs to be named like the others, so that the security-proxy won't be messed up with ... proxying onto the geoserver)

doc-vmware.war and static-vmware.war can be named doc.war and static.war respectively, since we are going to access it directly (without getting through the security-proxy)

$ cd /var/lib/tomcat6/webapps/
tomcat debian package comes with its own ROOT webapp, but we are not going to use it:
$ rm -rf ROOT/

then copy every war's into the directory



Adapt the vmware configuration profile
----------------------------------------


It's now time to adapt the VM to the vmware configuration profile

Looking at /var/lib/tomcat6/logs/catalina.out:

extractorapp is configured to host its log files into /var/log/tomcat/extractorapp.log, but this directory does not exist. Let's create it (or fix the configuration profile). For now I just created a symbolic link /var/log/tomcat -> /var/log/tomcat6

In fact, by reading the catalina.out log, every webapp will complain for this directory.

$ ln -s /var/log/tomcat6 /var/log/tomcat



Trying to reach the VM for a first time
-------------------------------------------


To make it easier, I put the following line into my /etc/hosts file:

192.168.122.123 vm-georchestra

which corresponds to the IP of my guest virtual machine (for the completely noob in linux, you can check the IP using a tool like ifconfig)


configure nginx
-----------------

One step I did not deal with yet is that we need a web server ; we previously removed apache2 and replaced it with nginx but it is not configured yet

Note: The nginx configuration below has been written during the whole deployment process, so some parameters may sound obscure now but only come to light reading carefully the following guide.

the default nginx package that comes from debian squeeze (is really old, yes), but the default configuration is somehow similar to the apache configuration. Let's jump into /etc/nginx

create a vm-georchestra file into /etc/nginx/sites-available, with the following content:

server {

        listen   80;
        listen   [::]:80 default ipv6only=on;

        server_name  vm-georchestra;

        access_log  /var/log/nginx/vm-georchestra.access.log;

        location / {
                root   /var/www;
                index  index.html index.htm;
        }

        location ~ ^/(analytics|cas|catalogapp|downloadform|mapfishapp|proxy|static|extractorapp|geoserver|geonetwork|doc|j_spring_cas_security_check|j_spring_security_logout)(/?).*$ {
                proxy_pass         http://127.0.0.1:8080$request_uri;
                proxy_redirect     off;

                proxy_set_header   Host             $host;
                proxy_set_header   X-Real-IP        $remote_addr;
                proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
                proxy_max_temp_file_size 0;

                client_max_body_size       20m;
                client_body_buffer_size    128k;

                proxy_connect_timeout      90;
                proxy_send_timeout         90;
                proxy_read_timeout         90;

                proxy_buffer_size          4k;
                proxy_buffers              4 32k;
                proxy_busy_buffers_size    64k;
                proxy_temp_file_write_size 64k;

        }
        # little hack to fix geoserver redirections
        # This may not be needed if geoserver is contained
        # in its own tomcat (which implies that the -private suffix
        # is not needed)
        rewrite ^/geoserver-private/(.*)$ /geoserver/$1 permanent;

        # some basic rewrites
        rewrite ^/analytics$ /analytics/ permanent;
        rewrite ^/catalogapp$ /catalogapp/ permanent;

}
#
# HTTPS server
#
server {
        listen   443;
        server_name  vm-georchestra;

        ssl  on;
        ssl_certificate      cert.pem;
        ssl_certificate_key  cert.key;

        ssl_session_timeout  5m;

        ssl_protocols  SSLv3 TLSv1;
        ssl_ciphers  ALL:!ADH:!EXPORT56:RC4+RSA:+HIGH:+MEDIUM:+LOW:+SSLv3:+EXP;
        ssl_prefer_server_ciphers   on;

        location / {
                proxy_pass         http://127.0.0.1/;
                proxy_redirect     off;

                proxy_set_header   Host             $host;
                proxy_set_header   X-Real-IP        $remote_addr;
                proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
                proxy_max_temp_file_size 0;

                client_max_body_size       20m;
                client_body_buffer_size    128k;

                proxy_connect_timeout      90;
                proxy_send_timeout         90;
                proxy_read_timeout         90;

                proxy_buffer_size          4k;
                proxy_buffers              4 32k;
                proxy_busy_buffers_size    64k;
                proxy_temp_file_write_size 64k;
        }
}

Then remove the symlink to the default configuration provided by debian into /etc/nginx/sites-enabled/ (default, if I remember correctly)

Create a new symlink to the created configuration :
$ ln -s /etc/nginx/sites-available/vm-georchestra /etc/nginx/sites-enabled/vm-georchestra

As you may have noticed, we defined some configuration variables that point out
on SSL materials, that need to be generated. cert.pem and cert.key have to be
created into /etc/nginx ; using signed certificates (that you may have obtained
from a SSL certificate company is beyond the scope of this guide - need to
have a real domain name).

into a temporary directory,

create a SSL key:
$ openssl genrsa -des3 -out myssl.key 1024
prompted for a passphrase, say "georchestra"

create a certificate signing request:
$ openssl req -new -key myssl.key -out myssl.csr
prompted for the previous password

then, reply to all questions with some parameters, the ONLY important one is the:
Common Name (eg, YOUR name) []:
For a webserver, the CN of the certificate SHOULD be the server name you are going to call, i.e. if you intend to "https://vm-georchestra/" then the certificate CN should be "vm-georchestra".

Then, we have to unprotect the key (remember the passphrase you have to enter). In fact, OpenSSL does not allow to create non-protected keys. Unprotecting the previous key is done with the following command:
$ openssl rsa -in myssl.key -out myssl-unprotected.key
(re-prompted for the passphrase)
The final step is to generate the certificate:
$ openssl x509 -req -days 365 -in myssl.csr -signkey myssl.key -out myssl.crt

then, you have it:
$ cp myssl.crt /etc/nginx/cert.pem
$ cp myssl-unprotected.key /etc/nginx/cert.key

You can now relaunch nginx:
$ /etc/init.d/nginx restart


Configure (well, repair) the security-proxy
---------------------------------------------

The security-proxy (ROOT.war) is THE webapp to focus on, without it, or with misconfigurations, nothing could work properly.

first, go to:
$ cd /var/lib/tomcat6/webapps/ROOT/WEB-INF

and have a look at proxy-servlet.xml

The main configuration of the routing is done relying on the following XML statements:

          <property name="targets">
               <map>
<entry key="extractorapp" value="http://localhost:8080/extractorapp-private/" /><entry key="gssec" value="http://localhost:8080/geoserver-security/" /><entry key="mapfishapp" value="http://localhost:8080/mapfishapp-private/" /><entry key="geonetwork" value="http://localhost:8080/geonetwork-private/" /><entry key="catalogapp" value="http://localhost:8080/catalogapp-private/" /><entry key="geoserver" value="http://localhost:8081/geoserver/" />
               </map>
          </property>

Here, the geoserver target is wrong: we chose to put it into the same tomcat as the other apps, so it should be http://localhost:8080/geoserver-private/


Into the file security-proxy.properties, we can figure out that the expected default password for ldap admin is "secret", infortunately we set it at setup of slapd to "georchestra", so let's change it (into the security proxy conf, or into slapd directly, here I chose to modify it into the LDAP server, so that if it is used elsewhere, it would fit with the configuration profile).

$ vi /etc/ldap/slapd.d/cn=config/Database={1}hdb.ldif
change:
olcRootPW:: [...]
to:
olcRootPW: secret

Relaunch the server:
$ /etc/init.d/slapd restart

Try the newly set password:
$ ldapsearch -Dcn=admin,dc=georchestra,dc=org -x -W -bdc=georchestra,dc=org
Enter LDAP Password: secret
[...]

still in proxy-servlet.xml:

line 51: I don't know what this <map>${header.mapping}</map> is about, but since this variable is not referenced into the security.properties file, let's remove it, because it makes the security-proxy startup fail.

In addition, the security-proxy tends to use a host named vm-georchestra, but we defined the guest VM with "georchestra" as hostname, let's hack it adding vm-georchestra to the /etc/hosts file

$ vi /etc/hosts
add:
127.0.0.1 vm-georchestra

Then relaunch tomcat:
$ /etc/init.d/tomcat6 restart



Geonetwork
------------

Somehow the parameters for the database has not been passed correctly with the vmware profile, let's fix it.

Here are some inconsistencies from the vmware configuration profile:

into /var/lib/tomcat6/webapps/geonetwork-private/config.xml:

around line 7:
${dataDir}

just replaced the variable by "data"

around line 56:
    <call name="env" class="org.fao.geonet.guiservices.util.Env">
        <param name="dlform.activated" value="${dlform.activated}" />
        <param name="dlform.pdf_url" value="${dlform.pdf_url}" />
    </call>

just replaced ${dlform.activated} by "false"
and put some junk for the other unresolved variable

around line 178:
     <url>jdbc:postgresql://${psql.host}:${psql.port}/${psql.db}</url>

replaced the <url> content by: jdbc:postgresql://127.0.0.1:5432/geonetwork

around line 411:

     <param name="wfsURL" value="${wfsRegionsCapabilities}" />
        ${wfsRegionsCredentials}

I Cannot remember what I did here, anyway I'm not planning to use the region resolver (it is a service which allows to recenter the map given some keywords are argument, i.e. if I type "morbihan", I can have the geonetwork map recentered on this french administrative boundary. The solution does not provide the data if I remember correctly, so I ignored this configuration step.


around line 196:
            <url>${downloadform.psql.url}</url>

downloadform is a recent development which aims to track downloads from geonetwork download services. Since was not existing when the configuration profile has been written, let's ignore it too.


Cas configuration:
--------------------

into /var/lib/tomcat6/webapps/cas/WEB-INF/cas.properties:


the server.prefix is incorrect, CAS protocol requires SSL communications:

server.prefix=http://localhost:8080/cas

to be modified by:

server.prefix=https://localhost:8443/cas


Little big parenthesis around tomcat and ssl
----------------------------------------------

The previous configuration step leverages another big issue that I somehow left behind before while installing tomcat: We need Java to trust the certificates we are going to generate (so that we would have a tomcat ssl-aware web server). Here is maybe the hardest part (if we forget the OpenLDAP setup) of this guide.

We have 2 solutions, depending on how the front http server (nginx / apache) is configured, and how you decide to suffer (dealing with the SSL certificate from apache / nginx or a regular keytool certificate. Just as a reminder: we decided to use OpenJDK vm, not the one from sun, both are incompatible). So, make sure to use keytool (see below) related to the java version you will be using.

I actually decided to "hack around" with java SSL key management, even if I also generated a SSL certificate for nginx.

Create a connector for tomcat6:

into /var/lib/tomcat6/conf/server.xml:

    <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
        maxThreads="150" scheme="https" secure="true"
        clientAuth="false" sslProtocol="TLS" keystoreFile="/var/lib/tomcat6/ssl/vm-georchestra.jks"
        keystorePass="secret" />

You can set it just after the default 8080 (regular http one).

Generate a java keystore:

$ cd /var/lib/tomcat6
$ mkdir ssl
$ cd ssl
$ keytool -genkey -alias mycert -keyalg RSA -keystore vm-georchestra.jks

A password would be asked, I put "secret" (see configuration of the Connector above)

Now, we want to merge this certificate into the global system truststore ; in order not to taint the default cacerts provided by the package, we are going to make a copy of it:

$ cp /usr/lib/jvm/java-6-openjdk/jre/lib/security/cacerts .

We now export the previously generated certificate:
$ keytool -exportcert -alias mycert -file exported -keystore vm-georchestra.jks

The password (secret) would be asked once again


And we import it into our custom truststore:

$ keytool -importcert -alias localhost -file exported -keystore cacerts

For your information you will be prompted for a password here, the default password for the truststore provided by the original debian package is "changeit"

Now we can modify the java environment variable to use this custom truststore instead of the system one ; add the 3 following lines into the edited file (omit the leading "+") :

$ vi /etc/default/tomcat6

[...]
JAVA_OPTS="-Djava.awt.headless=true -Xmx1536m -XX:+UseConcMarkSweepGC"

+ # truststore tweaks
+ SSL_OPTS="-Djavax.net.ssl.trustStore=/var/lib/tomcat6/ssl/cacerts -Djavax.net.ssl.trustStorePassword=changeit"
+ JAVA_OPTS="${JAVA_OPTS} ${SSL_OPTS}"
[...]

Relaunch tomcat, and try on your host to visit the page:

http://vm-georchestra/geonetwork/

Clicking on "connexion" should redirect you to the cas server, you can then try the following credentials:

testadmin/testadmin
testreviewer/testreviewer
testuser/testuser
...

MapfishApp
--------------

Geonetwork seems to work now, but the other apps are still broken, we will continue with mapfishapp configuration.

in /var/lib/tomcat6/webapps/mapfishapp-private/WEB-INF/ws-servlet.xml, tomcat seems to be complaining around the line #15:

 <map>${credentials}</map>

Something should have been wrong during the configuration profile application. Anyway, I don't know if this variable is still used, let's try to remove the ${credentials} variable (leave <map></map>)

ExtractorApp
--------------

Extractorapp seems to have a more complex problem, tomcat is telling about a "circular reference".

into /var/lib/tomcat6/webapps/extractorapp-private/WEB-INF/extractorapp.properties:

a lot of variables have not been replaced, I used these ones:

maxExtractions=5
remoteReproject=true
useCommandLineGDAL=false
extractionFolderPrefix=extraction-

dlformactivated=false
dlformjdbcurl="/some/junk"

emailfactory=extractorapp.ws.EmailFactoryDefault


Going a bit further ...
------------------------

The default geonetwork map does not display, let's modify it:
$ vi /var/lib/tomcat6/webapps/geonetwork-private/GeoConfig.js

replace accordingly:
Geonetwork.CONFIG.GeoPublisher = {
    // configuration for the base map used in the GeoPublisher interface
    // Map viewer options to use in main map viewer and in editor map viewer
    mapOptions: {
      projection: 'EPSG:900913',
      maxExtent: new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508.34),
      resolutions: [ 156543.03392804097,
                     78271.516964020484,
                     39135.758482010242,
                     19567.879241005121,
                     9783.9396205025605,
                     4891.9698102512803,
                     2445.9849051256401,
                     1222.9924525628201,
                     611.49622628141003,
                     305.74811314070502,
                     152.87405657035251,
                     76.437028285176254,
                     38.218514142588127,
                     19.109257071294063,
                     9.5546285356470317,
                     4.7773142678235159,
                     2.3886571339117579,
                     1.194328566955879,
                     0.59716428347793948,
                     0.29858214173896974 ],
      transitionEffect: 'resize',
      displayOutsideMaxExtent: true,
      units: 'm',
      buffer:0,
      attribution:'<span style="background-color:#fff">data by <a href="http://openstreetmap.org">openstreetmap</a></span>'
    },
    layerFactory: function() {
      return [
        new OpenLayers.Layer.WMS('Baselayer','http://maps.qualitystreetmap.org/tilecache/tilecache.py', {layers:'osm',format: 'image/png' },{tileSize:new OpenLayers.Size(256,256), isBaseLayer: true})
      ];
    }
}


Then save & reload (the page, since you only modified javascript files, no need to restart tomcat)

Now you can see a "waterworld" map of the world on low zoom level (problem unresolved yet ...)


Having the geopublisher working
---------------------------------

Geopublishing is the action to attach and publish the geospatial data to a metadata, so that it is possible to download it from GeoNetwork, and publish them onto the GeoServer provided.

To have it working, you will need to ensure that the geonetwork configuration matches the geoserver one. Have a look at:
/var/lib/tomcat6/geonetwork-private/WEB-INF/geoserver-nodes.xml

it is looking for a geor_pub namespace prefix, but it may be necessary to create it on the geoserver side.

Go to http://vm-georchestra/geoserver/web/ then add a new workspace with the following informations:
namespace prefix: geor_pub
namespace url: http://www.georchestra.org/


Conclusion
------------

I began this full deploy yesterday around 20h30, stopped around 23h00, played around this morning 30 minutes during my travel in train between Chambéry and Grenoble, took another 15mins this afternoon while going back home, and finished it from 20:30 to 22:45.

2:30 + 45 + 2:15 = 5:30 for a full setup, not bad :-). But I guess I have not beaten up my previous score.

To sum up the experience, I used some new pieces of software that I never used with geOrchestra as of today, and which gives some interesting points to this "time attack" :

* Using nginx as the front web server is possible (Fabrice did it a bit before me)

* Even if compiling the trunk with OpenJDK does not seem to work yet (not tried though), it seems working like a charm at runtime with the underlying java vm.

* Installing the whole solution is not so difficult, if we have knowledge of how to manage an OpenLDAP server, and to deal with java SSL certificates.

* The vmware configuration profile does need an update !



Still a bit further: downloadform & ogcstatistics
---------------------------------------------------

ogcstatistics
---------------

OGC statistics is a kind of "plugin" (strictly speaking, a log4j module) which, once attached to the security-proxy, logs every OGC requests to a postgresql database. It is bundled with the security-proxy vmware webapp, but not activated by default. 

Here are the steps to activate this specific logging:

$ su - postgres
$ createdb ogcstatistics
$ wget http://svn.georchestra.org/georchestra/trunk/ogc-server-statistics/ogc_statistics_table.sql
$ psql ogcstatistics < ogc_statistics_table.sql

Then edit the file in /var/lib/tomcat6/webapps/ROOT/WEB-INF/classes/log4j.properties so that it looks like:

[...]
log4j.rootCategory=INFO, R, OGCSTATISTICS
[...]
# OGC services statistics
log4j.appender.OGCSTATISTICS=com.camptocamp.ogcservstatistics.log4j.OGCServicesAppender
log4j.appender.OGCSTATISTICS.activated=true

log4j.appender.OGCSTATISTICS.jdbcURL=jdbc:postgresql://localhost:5432/ogcstatistics
log4j.appender.OGCSTATISTICS.databaseUser=www-data
log4j.appender.OGCSTATISTICS.databasePassword=www-data
[...]

And restart tomcat

Deploying the analytics webapp
-------------------------------

In order to be able to analyze the logs, a new webapp has been developped, called analytics. Let's deploy it with the other webapps.

$ cd
$ wget http://applis-bretagne.fr/hudson/job/georchestra//lastSuccessfulBuild/artifact/analytics/target/analytics-private-vmware.war

$ cd /var/lib/tomcat6/webapps/ROOT/WEB-INF/

$ vi proxy-servlet.xml

And add a target for analytics:

[...]
          <property name="targets">
               <map>
<entry key="extractorapp" value="http://localhost:8080/extractorapp-private/" /><entry key="gssec" value="http://localhost:8080/geoserver-security/" /><entry key="mapfishapp" value="http://localhost:8080/mapfishapp-private/" /><entry key="geonetwork" value="http://localhost:8080/geonetwork-private/" /><entry key="catalogapp" value="http://localhost:8080/catalogapp-private/" /><entry key="geoserver" value="http://localhost:8080/geoserver-private/" /><entry key="analytics" value="http://localhost:8080/analytics-private/" />
               </map>
          </property>
[...]

same with the nginx configuration:

$ vi /etc/nginx/sites-available/vm-georchestra

[...]  
    location ~ ^/(analytics|cas|catalogapp|mapfishapp|proxy|static|extractorapp|geoserver|geonetwork|doc|j_spring_cas_security_check|j_spring_security_logout)(/?).*$
[...]

ensure to have the "analytics" entry defined in the regexp

$ /etc/init.d/nginx reload


$ /etc/init.d/tomcat6 stop
$ cd
$ cp analytics-private-vmware.war /var/lib/tomcat6/webapps/analytics-private.war
$ /etc/init.d/tomcat6 start


Activating downloadform
-------------------------

downloadform is a webapp which aims to keep track of what is downloaded by the user, forcing them to accept a usage policy before proceeding, it has been introduced into the solution later on.

You have to follow the same steps as before in order to add it

$ cd
$ wget http://applis-bretagne.fr/hudson/job/georchestra//lastSuccessfulBuild/artifact/downloadform/target/

stopping tomcat
$ /etc/init.d/tomcat6 stop

$ cp downloadform-1.0-vmware.war /var/lib/tomcat6/webapps/downloadform-private.war

Registering the new webapp into the security-proxy:

[...]
          <property name="targets">
               <map>
<entry key="extractorapp" value="http://localhost:8080/extractorapp-private/" /><entry key="gssec" value="http://localhost:8080/geoserver-security/" /><entry key="mapfishapp" value="http://localhost:8080/mapfishapp-private/" /><entry key="geonetwork" value="http://localhost:8080/geonetwork-private/" /><entry key="catalogapp" value="http://localhost:8080/catalogapp-private/" /><entry key="geoserver" value="http://localhost:8080/geoserver-private/" /><entry key="analytics" value="http://localhost:8080/analytics-private/" /><entry key="downloadform" value="http://localhost:8080/downloadform-private/" />
               </map>
          </property>
[...]

Updating the nginx configuration:

$ vi /etc/nginx/sites-available/vm-georchestra

[...]  
    location ~ ^/(analytics|cas|catalogapp|downloadform|mapfishapp|proxy|static|extractorapp|geoserver|geonetwork|doc|j_spring_cas_security_check|j_spring_security_logout)(/?).*$
[...]

This webapp needs a bit more configuration into extractorapp and geonetwork:

into /var/lib/tomcat6/webapps/geonetwork-private/WEB-INF/config.xml around line #51:

                       <call name="env" class="org.fao.geonet.guiservices.util.Env">
                                <param name="dlform.activated" value="true" />
                                <param name="dlform.pdf_url" value="/static/non-existing.pdf" />
                        </call>

Note: You can modify pdf_url to point on an existing document, but well, I'm not a lawyer, so I'll leave it for now.

around line #196:
                <driver>org.postgresql.Driver</driver>
                                <!--   
                                        jdbc:postgresql:database
                                        jdbc:postgresql://host/database
                                        jdbc:postgresql://host:port/database
                                -->
                <url>jdbc:postgresql://127.0.0.1:5432/downloadform</url>




into /var/lib/tomcat6/webapps/extractorapp-private/WEB-INF/extractorapp.properties around line #23:

dlformactivated=true
dlformjdbcurl=jdbc:postgresql://www-data:www-data@127.0.0.1:5432/downloadform

Then create the database:
$ su - postgres
$ wget http://svn.georchestra.org/georchestra/trunk/downloadform/samples/sample.sql
$ createdb downloadform
$ psql downloadform < sample.sql
$ rm sample.sql

There is an issue with the previous script, some versions of postgresql does not seem to address correctly the foreign key constraints with inherited tables. You may have to drop the integrity constraint

$ psql downloadform
> set search_path = download, public ;
> alter table logtable_datause drop constraint fk_logtable_id ;

Then restart tomcat (as root)

$ /etc/init.d/tomcat6 start

If you deployed the previous analytics webapp, you should now be able to get tracks of downloads from geonetwork, visiting http://vm-georchestra/analytics/ if logged as testadmin user. 

ultimate conclusion
-----------------------

Maybe the installation process is far from perfect, if you still have some questions, feel free to join geOrchestra groups on google, and ask.

http://groups.google.com/group/georchestra-dev
http://groups.google.com/group/georchestra

These are mainly french-speaking but you can also write in english.


                    - Pierre Mauduit <pmauduit AT qualitystreetmap DOT org>
