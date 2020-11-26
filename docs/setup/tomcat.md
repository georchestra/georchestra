# Tomcat

We need 3 tomcat instances:
 * one for the proxy and cas webapps
 * an other one for geoserver
 * the last one for the other webapps

## Prerequisites

```
sudo apt install -y tomcat9 tomcat9-user
```

We will deactivate the default tomcat instance, just to be sure:

```
sudo service tomcat9 stop
sudo systemctl disable tomcat9
```

## Keystore

To create a keystore containing a newly generated key, enter the following:
```
sudo keytool -genkey \
    -alias georchestra_localhost \
    -keystore /etc/tomcat9/keystore \
    -storepass STOREPASSWORD \
    -keypass STOREPASSWORD \
    -keyalg RSA \
    -keysize 2048 \
    -dname "CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=FR"
```
... where ```STOREPASSWORD``` is a password you choose, and the ```dname``` string is customized.

### CA certificates

If the geOrchestra webapps have to communicate with remote HTTPS services, our keystore/trustore has to include the remote CA certificates.

This will be the case, when e.g.:

 * the proxy has to relay an https service
 * geonetwork will harvest remote https nodes
 * geoserver will proxy remote https ogc services

In order to trust the default system certificates from the `cacerts` package into our previously created keystore:

```
sudo keytool -importkeystore \
    -srckeystore /etc/ssl/certs/java/cacerts \
    -destkeystore /etc/tomcat9/keystore
```

The password of the srckeystore is "changeit" by default.

### SSL

To import the certificate used by `Apache2` into our keystore, we shall issue the following command:
```
keytool -import -alias cert_ssl -file /var/www/georchestra/ssl/georchestra.crt -keystore /etc/tomcat9/keystore
```

If you have a Let's Encrypt certificate using Certbot the file to import is : /etc/letsencrypt/live/[your_sdi_domain_name]/cert.pem. But
since the LetsEncrypt root certificates were already trusted previously, it should not be necessary to do so.

### LDAP SSL

In case the LDAP connection uses SSL (which is not the default in the geOrchestra template configuration), its certificate must be added to the keystore.

Here's how:

First get the public key.
```
echo "" | openssl s_client -connect LDAPHOST:LDAPPORT -showcerts 2>/dev/null | openssl x509 -out /tmp/certfile.txt
```
... and then add it to the keystore:
```
sudo keytool -import -alias cert_ldap -file /tmp/certfile.txt -keystore /etc/tomcat9/keystore
```

### Finally,

verify the list of keys in keystore:
```
keytool -keystore /etc/tomcat9/keystore -list
```

## geOrchestra datadir

geOrchestra gets its configuration settings from specific directory, the "georchestra datadir", which is is usually located in `/etc/georchestra`.

To bootstrap a default datadir:
```
sudo git clone -b BRANCH_NAME https://github.com/georchestra/datadir.git /etc/georchestra
```
... where BRANCH_NAME matches the geOrchestra release name (or `master` for development purposes)

## Tomcat proxycas

### Create the instance

We will now create an instance named ```tomcat9-proxycas```:

```
sudo tomcat9-instance-create -p 8180 -c 8105 /var/lib/tomcat9-proxycas
```

As indicated in the parameters from the previous command-line, 8180 will be the HTTP port and 8105 the stop port.


Then:
```
sudo cp -r /usr/share/tomcat9 /usr/share/tomcat9-proxycas
sudo mkdir /var/lib/tomcat9-proxycas/conf/policy.d
sudo touch /var/lib/tomcat9-proxycas/conf/policy.d/empty.policy
sudo rm -rf /var/lib/tomcat9-proxycas/logs
sudo ln -s /var/log/tomcat9 /var/lib/tomcat9-proxycas/logs
sudo mkdir -p /var/lib/tomcat9-proxycas/conf/Catalina/localhost
sudo chown -R tomcat:tomcat /var/lib/tomcat9-proxycas
sudo cp /lib/systemd/system/tomcat9.service /lib/systemd/system/tomcat9-proxycas.service
sudo cp /usr/libexec/tomcat9/tomcat-start.sh /usr/libexec/tomcat9/tomcat-proxycas-start.sh
sudo cp /etc/default/tomcat9 /etc/default/tomcat9-proxycas
```

Finally, edit the `/lib/systemd/system/tomcat9-proxycas` script and adapt to the created instance:

```diff
--- tomcat9.service	2019-06-13 21:26:12.000000000 +0000
+++ tomcat9-proxycas.service	2020-01-20 13:19:10.728000000 +0000
@@ -11,14 +11,14 @@

 # Configuration
 Environment="CATALINA_HOME=/usr/share/tomcat9"
-Environment="CATALINA_BASE=/var/lib/tomcat9"
+Environment="CATALINA_BASE=/var/lib/tomcat9-proxycas"
 Environment="CATALINA_TMPDIR=/tmp"
 Environment="JAVA_OPTS=-Djava.awt.headless=true"

 # Lifecycle
 Type=simple
 ExecStartPre=+/usr/libexec/tomcat9/tomcat-update-policy.sh
-ExecStart=/bin/sh /usr/libexec/tomcat9/tomcat-start.sh
+ExecStart=/bin/sh /usr/libexec/tomcat9/tomcat-proxycas-start.sh
 SuccessExitStatus=143
 Restart=on-abort

@@ -35,7 +35,7 @@
 CacheDirectoryMode=750
 ProtectSystem=strict
 ReadWritePaths=/etc/tomcat9/Catalina/
-ReadWritePaths=/var/lib/tomcat9/webapps/
+ReadWritePaths=/var/lib/tomcat9-proxycas/
 ReadWritePaths=/var/log/tomcat9/
 RequiresMountsFor=/var/log/tomcat9

```

And reload the `systemd` configuration:

```
sudo systemctl daemon-reload
```

### Customize the Java runtime and options

In ```/etc/default/tomcat9-proxycas```, we will adapt the JAVA_HOME & JAVA_OPTS environment variables to suit our needs:

```
JAVA_HOME=/usr/lib/jvm/adoptopenjdk-8-hotspot-amd64
```

And later add these lines (change the ```STOREPASSWORD``` string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeorchestra.datadir=/etc/georchestra"

JAVA_OPTS="$JAVA_OPTS \
              -Xms1G \
              -Xmx1G"

JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat9/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"
```

If your connection needs to pass through a proxy, you should also add the relevant options, as follows:

```
JAVA_OPTS="$JAVA_OPTS \
              -Dhttp.proxyHost=proxy.mycompany.com \
              -Dhttp.proxyPort=XXXX \
              -Dhttps.proxyHost=proxy.mycompany.com \
              -Dhttps.proxyPort=XXXX"
```

Finally, make sure the variables are exported at the end of the script:

```
export JAVA_HOME
export JAVA_OPTS
```

### Configure connectors

In ```/var/lib/tomcat-proxycas/conf/server.xml```, find the place where the HTTP connector is defined, and check that the port is correctly set:
```
    <Connector port="8180" protocol="HTTP/1.1"
               connectionTimeout="20000"
               URIEncoding="UTF-8"
               redirectPort="8443"
                />
```

... which should have been done by the `tomcat9-instance-create` script.

### Start the instance

Finally, we make the instance start by default with the OS:

```
systemctl enable tomcat9-proxycas
```

and check if we can now start it:

```
service tomcat9-proxycas start
```

In case of issues, one can consult the logs using the following command:

```
journalctl -xe
```

## Tomcat geOrchestra

### Create the instance

The same procedure as before is followed, we will only adapt names and ports.
```
sudo tomcat9-instance-create -p 8280 -c 8205 /var/lib/tomcat9-georchestra
sudo cp -r /usr/share/tomcat9 /usr/share/tomcat9-georchestra
sudo mkdir /var/lib/tomcat9-georchestra/conf/policy.d
sudo touch /var/lib/tomcat9-georchestra/conf/policy.d/empty.policy
sudo rm -rf /var/lib/tomcat9-georchestra/logs
sudo ln -s /var/log/tomcat9 /var/lib/tomcat9-georchestra/logs
sudo mkdir -p /var/lib/tomcat9-georchestra/conf/Catalina/localhost
sudo chown -R tomcat:tomcat /var/lib/tomcat9-georchestra
sudo cp /lib/systemd/system/tomcat9.service /lib/systemd/system/tomcat9-georchestra.service
sudo cp /usr/libexec/tomcat9/tomcat-start.sh /usr/libexec/tomcat9/tomcat-georchestra-start.sh
sudo cp /etc/default/tomcat9 /etc/default/tomcat9-georchestra
```

Adapt the script in libexec, so that it will source the expected file in `/etc/default`:

```
# Load the service settings
. /etc/default/tomcat9-georchestra
```

Finally, we need to adapt the systemd unit file for this instance:

```diff
--- tomcat9.service	2019-06-13 21:26:12.000000000 +0000
+++ tomcat9-georchestra.service	2020-01-20 13:34:21.760000000 +0000
@@ -11,14 +11,14 @@

 # Configuration
 Environment="CATALINA_HOME=/usr/share/tomcat9"
-Environment="CATALINA_BASE=/var/lib/tomcat9"
+Environment="CATALINA_BASE=/var/lib/tomcat9-georchestra"
 Environment="CATALINA_TMPDIR=/tmp"
 Environment="JAVA_OPTS=-Djava.awt.headless=true"

 # Lifecycle
 Type=simple
 ExecStartPre=+/usr/libexec/tomcat9/tomcat-update-policy.sh
-ExecStart=/bin/sh /usr/libexec/tomcat9/tomcat-start.sh
+ExecStart=/bin/sh /usr/libexec/tomcat9/tomcat-georchestra-start.sh
 SuccessExitStatus=143
 Restart=on-abort

@@ -35,7 +35,7 @@
 CacheDirectoryMode=750
 ProtectSystem=strict
 ReadWritePaths=/etc/tomcat9/Catalina/
-ReadWritePaths=/var/lib/tomcat9/webapps/
+ReadWritePaths=/var/lib/tomcat9-georchestra/
+ReadWritePaths=/opt/geonetwork_data_dir/
+ReadWritePaths=/opt/extracts/
 ReadWritePaths=/var/log/tomcat9/
 RequiresMountsFor=/var/log/tomcat9

```

Note: the `ReadWritePaths` parameters above should be set in the tomcat unit files accordingly. Here we assumed that
GeoNetwork (which requires RW access to /opt/geonetwork_data_dir) and extractorapp (/opt/extracts) will be deployed
in this instance.

Reload the `systemd` configuration:

```
sudo systemctl daemon-reload
sudo systemctl enable tomcat9-georchestra
```

### Customize Java runtime & options

In ```/etc/default/tomcat9-georchestra```, we need to adapt the JAVA_HOME & JAVA_OPTS variables as well:
```
JAVA_HOME=/usr/lib/jvm/adoptopenjdk-8-hotspot-amd64
```

And later add these lines (change the ```STOREPASSWORD``` string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeorchestra.datadir=/etc/georchestra"

JAVA_OPTS="$JAVA_OPTS \
              -Xms6G \
              -Xmx6G"

JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat9/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"

JAVA_OPTS="$JAVA_OPTS \
              -Djava.util.prefs.userRoot=/tmp \
              -Djava.util.prefs.systemRoot=/tmp"
```
This allocates 6GB of your server RAM to all geOrchestra webapps (except proxy, cas and geoserver, which are located in other tomcat instances).

#### GeoNetwork 3.x (geOrchestra 15.12 and above)

If GeoNetwork 3.x is deployed, some extra java environment variables will be
required, because almost everything related to the configuration and the
geOrchestra integration has been exported outside of the webapp, into the geOrchestra datadir.

GeoNetwork also comes with its own "datadir", to store specific materials related to its own operation (to store uploaded content for example). One
can create the directory using the following commands:

```
sudo mkdir /opt/geonetwork_data_dir
sudo chown -R tomcat /opt/geonetwork_data_dir
```

Customize the `/etc/georchestra/geonetwork/geonetwork.properties`, so that the
`geonetwork.dir` reflects the path created during the previous step, e.g. `/opt/geonetwork_data_dir`.

Then ensure your tomcat has the following environment variable set:

```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeonetwork.jeeves.configuration.overrides.file=/etc/georchestra/geonetwork/config/config-overrides-georchestra.xml"
```

Note: You can also override every geonetwork sub-data-directories by modifying
the `/etc/georchestra/geonetwork/geonetwork.properties` file for convenience.

#### Viewer

If the "mapfishapp" viewer application is deployed:
```
JAVA_OPTS="$JAVA_OPTS \
               -Dmapfish-print-config=/etc/georchestra/mapfishapp/print/config.yaml \
               -Dorg.geotools.referencing.forceXY=true"
```

#### Extractor

If the extractor application is deployed:
```
JAVA_OPTS="$JAVA_OPTS \
               -Dorg.geotools.referencing.forceXY=true \
               -Dextractor.storage.dir=/path/to/temporary/extracts/"
```
... where ```/path/to/temporary/extracts/``` is a directory owned by tomcat in a dedicated server partition.

If one of geonetwork or extractorapp is deployed:
```
JAVA_OPTS="$JAVA_OPTS \
               -Djava.util.prefs.userRoot=/var/lib/tomcat9-georchestra/temp \
               -Djava.util.prefs.systemRoot=/var/lib/tomcat9-georchestra/temp"
```

If your connection to the internet requires to pass through a proxy, you should also add the ```-Dhttp.proxy*``` options here.

Finally, export both variables:

```
export JAVA_HOME
export JAVA_OPTS
```

### Configure connector

Normally, the `tomcat9-instance-create` should have correctly set the listening port accordingly ; but we want to check the connector parameters:

In ```/var/lib/tomcat9-georchestra/conf/server.xml```:

```
    <Connector port="8280" protocol="HTTP/1.1"
               connectionTimeout="20000"
               URIEncoding="UTF-8"
               scheme="https"
               proxyName="georchestra.mydomain.org"
               proxyPort="443"
               redirectPort="8443" />
```
Set proxyName to your service's fqdn.

### Start the instance

```
sudo service tomcat9-georchestra start
```

## Tomcat GeoServer

### Create the instance

```
sudo tomcat9-instance-create -p 8380 -c 8305 /var/lib/tomcat9-geoserver0
sudo cp -r /usr/share/tomcat9 /usr/share/tomcat9-geoserver0
sudo mkdir /var/lib/tomcat9-geoserver0/conf/policy.d
sudo touch /var/lib/tomcat9-geoserver0/conf/policy.d/empty.policy
sudo rm -rf /var/lib/tomcat9-geoserver0/logs
sudo ln -s /var/log/tomcat9 /var/lib/tomcat9-geoserver0/logs
sudo mkdir -p /var/lib/tomcat9-geoserver0/conf/Catalina/localhost
sudo chown -R tomcat:tomcat /var/lib/tomcat9-geoserver0
sudo cp /lib/systemd/system/tomcat9.service /lib/systemd/system/tomcat9-geoserver0.service
sudo cp /usr/libexec/tomcat9/tomcat-start.sh /usr/libexec/tomcat9/tomcat-geoserver0-start.sh
sudo cp /etc/default/tomcat9 /etc/default/tomcat9-geoserver0
```

Adapt the `/usr/libexec/tomcat9/tomcat-geoserver0-start.sh` so that it sources the `/etc/default/tomcat9-geoserver0` file instead:

```
# Load the service settings
. /etc/default/tomcat9-geoserver0
```

And adapt the previously created systemd unit file in `/lib/systemd/system/tomcat9-geoserver0.service`:


```diff
--- tomcat9.service	2019-06-13 21:26:12.000000000 +0000
+++ tomcat9-geoserver0.service	2020-01-20 13:46:12.968000000 +0000
@@ -11,14 +11,14 @@

 # Configuration
 Environment="CATALINA_HOME=/usr/share/tomcat9"
-Environment="CATALINA_BASE=/var/lib/tomcat9"
+Environment="CATALINA_BASE=/var/lib/tomcat9-geoserver0"
 Environment="CATALINA_TMPDIR=/tmp"
 Environment="JAVA_OPTS=-Djava.awt.headless=true"

 # Lifecycle
 Type=simple
 ExecStartPre=+/usr/libexec/tomcat9/tomcat-update-policy.sh
-ExecStart=/bin/sh /usr/libexec/tomcat9/tomcat-start.sh
+ExecStart=/bin/sh /usr/libexec/tomcat9/tomcat-geoserver0-start.sh
 SuccessExitStatus=143
 Restart=on-abort

@@ -35,7 +35,7 @@
 CacheDirectoryMode=750
 ProtectSystem=strict
 ReadWritePaths=/etc/tomcat9/Catalina/
-ReadWritePaths=/var/lib/tomcat9/webapps/
+ReadWritePaths=/var/lib/tomcat9-geoserver0/
+ReadWritePaths=/opt/geoserver_data_dir
+ReadWritePaths=/opt/geowebcache_cache_dir
 ReadWritePaths=/var/log/tomcat9/
 RequiresMountsFor=/var/log/tomcat9

```

Enable the instance:

```
sudo systemctl daemon-reload
sudo systemctl enable tomcat9-georchestra
```

Locate tomcat's `conf/context.xml` and update the `<Context>` tag in order to
set `useRelativeRedirects` to `false`, eg:

```xml
<Context useRelativeRedirects="false">
    <WatchedResource>WEB-INF/web.xml</WatchedResource>
    <WatchedResource>${catalina.base}/conf/web.xml</WatchedResource>
</Context>
```

See [#1857](https://github.com/georchestra/georchestra/pull/1847) for the
motivations behind setting this parameter.

### Customize Java runtime & options

In ```/etc/default/tomcat9-geoserver0```:
```
JAVA_HOME=/usr/lib/jvm/adoptopenjdk-8-hotspot-amd64
```

And later add these lines:
```
JAVA_OPTS="$JAVA_OPTS \
            -DGEOSERVER_DATA_DIR=/opt/geoserver_data_dir \
            -DGEOWEBCACHE_CACHE_DIR=/opt/geowebcache_cache_dir"

JAVA_OPTS="$JAVA_OPTS \
            -Dfile.encoding=UTF8 \
            -Djavax.servlet.request.encoding=UTF-8 \
            -Djavax.servlet.response.encoding=UTF-8"

JAVA_OPTS="$JAVA_OPTS \
            -Xms2G -Xmx2G"

JAVA_OPTS="$JAVA_OPTS \
            -server \
            -XX:+UseParNewGC \
            -XX:ParallelGCThreads=2 \
            -XX:SoftRefLRUPolicyMSPerMB=36000 \
            -XX:+UseConcMarkSweepGC"
```
This allocates 2GB of your server RAM to GeoServer.

The ```/opt/geoserver_data_dir``` directory should be owned by tomcat, and created by checking out this repository [georchestra/geoserver_minimal_datadir](https://github.com/georchestra/geoserver_minimal_datadir):

```
sudo git clone -b master https://github.com/georchestra/geoserver_minimal_datadir.git /opt/geoserver_data_dir
sudo chown -R tomcat /opt/geoserver_data_dir
sudo mkdir -p /opt/geowebcache_cache_dir
sudo chown -R tomcat /opt/geowebcache_cache_dir

```
Note that this data dir holds **several branches**: please refer to the repository [README](https://github.com/georchestra/geoserver_minimal_datadir/blob/master/README.md) in order to **choose the correct one**.


As before (change the ```STOREPASSWORD``` string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat9/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"
```

In case your connection to the internet needs to pass through a proxy, you should also add the ```-Dhttp.proxyHost=xxxx -Dhttp.proxyPort=xxxx``` options here.


### Notes about the s3-geotiff module

The `s3-geotiff` module which allows to optimally store geotiff files onto s3-compatible buckets is now included by default. If you plan
to use its features in your GeoServer instance, you will also have to add the following JAVA_OPTS options:

```
JAVA_OPTS="$JAVA_OPTS \
            -Ds3.caching.ehCacheConfig=/etc/georchestra/geoserver/s3-geotiff/s3-geotiff-ehcache.xml \
            -Ds3.properties.location=/etc/georchestra/geoserver/s3-geotiff/s3.properties \"
```

### Note for geofence users

If geofence is activated in our geoserver, one will require some specific environment variables set. in the `/etc/default/tomcat9-geoserver0` file, consider adding:

```
JAVA_OPTS="$JAVA_OPTS \
           -Dgeofence-ovr=file:/etc/georchestra/geoserver/geofence/geofence-datasource-ovr.properties"
```

### Note for geowebcache users

GeoWebCache can be used either as a standalone webapp or integrated to GeoServer. If we want to go for the first option, we will also require the following JAVA_OPTS options:

```
JAVA_OPTS="$JAVA_OPTS
           -DGEOWEBCACHE_CONFIG_DIR=/opt/geowebcache_datadir \
           -DGEOWEBCACHE_CACHE_DIR=/opt/geowebcache_tiles"
```

### Exporting Java variables

Finally, do not forget to export the variables at the end of the script:

```
export JAVA_HOME
export JAVA_OPTS
```


### Configure the connector

For GeoServer, it is advised to lower the number of simultaneous threads handling incoming requests.
By default Tomcat assumes 200 threads, but experiments show that 20 is a better value.

It is also required to set the `scheme`, `proxyPort` & `proxyName` parameters, as follows in ```/var/lib/tomcat9-geoserver0/conf/server.xml```:
```
    <Connector port="8380" protocol="HTTP/1.1"
               connectionTimeout="20000"
               URIEncoding="UTF-8"
               maxThreads="20"
               minSpareThreads="20"
               scheme="https"
               proxyName="georchestra.mydomain.org"
               proxyPort="443"
               redirectPort="8443" />
```
... where `proxyName` is set to your service's fqdn.


### Start the instance

```
sudo service tomcat9-geoserver0 start
```

### In case of troubles with the GeoServer UI

While testing the following setup guide, we stumbled upon a strange behaviour of the geoserver UI. If you
are encountering some strange response from GeoServer ("400 Bad Request"), it might be due to the following [upstream
issue](https://osgeo-org.atlassian.net/browse/GEOS-9353).

As described in the issue, this can be solved by changing geoserver's `web.xml` file, and adding the following context variable:

```
<context-param>
  <param-name>GEOSERVER_CSRF_WHITELIST</param-name>
  <param-value>georchestra.mydomain.org</param-value>
</context-param>
```

As described in the [official GeoServer documentation](https://docs.geoserver.org/stable/en/user/security/webadmin/csrf.html), you should also be able to use
a JAVA_OPTS environment variable (eg `-DGEOSERVER_CSRF_WHITELIST=georchestra.mydomain.org`) instead of the previous modification.


Of course you will have to adapt the parameter to suit your setup. Setups based on the Jetty Servlet Container do not seem to be affected.
