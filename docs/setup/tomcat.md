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

To create a keystore, enter the following:
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

If the geOrchestra webapps have to communicate with remote HTTPS services, our keystore/trustore has to include CA certificates.

This will be the case when:
 * the proxy has to relay an https service
 * geonetwork will harvest remote https nodes
 * geoserver will proxy remote https ogc services

To do this:
```
sudo keytool -importkeystore \
    -srckeystore /etc/ssl/certs/java/cacerts \
    -destkeystore /etc/tomcat9/keystore
```

The password of the srckeystore is "changeit" by default, and should be modified in /etc/default/cacerts.

### SSL

As the SSL certificate is absolutely required, at least for the CAS module, you must add it to the keystore.
```
keytool -import -alias cert_ssl -file /var/www/georchestra/ssl/georchestra.crt -keystore /etc/tomcat9/keystore
```
If you have a Let's Encrypt certificate using Certbot the file to import is : /etc/letsencrypt/live/[your_sdi_domain_name]/cert.pem

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

Finally, edit the `/lib/systemd/system/tomcat9-proxycas` script, find the following line, and adapt to the created instance:

```
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
+ReadWritePaths=/var/lib/tomcat9-proxycas/webapps/
 ReadWritePaths=/var/log/tomcat9/
 RequiresMountsFor=/var/log/tomcat9

```

And reload the `systemd` configuration:

```
sudo systemctl daemon-reload
```

### Customize Java options

In ```/etc/default/tomcat9-proxycas```, we will adapt the JAVA_OPTS environment variable to suit our needs:

```
JAVA_OPTS="-Djava.awt.headless=true -XX:+UseConcMarkSweepGC"
```

And later add these lines (change the ```STOREPASSWORD``` string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeorchestra.datadir=/etc/georchestra"

JAVA_OPTS="$JAVA_OPTS \
              -Xms256m \
              -Xmx256m \
              -XX:MaxPermSize=128m"

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

### Configure connectors

In ```/var/lib/tomcat-proxycas/conf/server.xml```, find the place where the HTTP connector is defined, and change it into:
```
    <Connector port="8180" protocol="HTTP/1.1"
               connectionTimeout="20000"
               URIEncoding="UTF-8"
               redirectPort="8443" />

    <Connector port="8443" protocol="HTTP/1.1"
               SSLEnabled="true"
               scheme="https"
               secure="true"
               URIEncoding="UTF-8"
               maxThreads="150"
               clientAuth="false"
               keystoreFile="/etc/tomcat9/keystore"
               keystorePass="STOREPASSWORD"
               compression="on"
               compressionMinSize="2048"
               noCompressionUserAgents="gozilla, traviata"
               compressableMimeType="text/html,text/xml,application/xml,text/javascript,application/x-javascript,application/javascript,text/css" />
```
... in which you also take care of changing the ```STOREPASSWORD``` string.


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
+ReadWritePaths=/var/lib/tomcat9-georchestra/webapps/
 ReadWritePaths=/var/log/tomcat9/
 RequiresMountsFor=/var/log/tomcat9

```

Reload the `systemd` configuration:

```
sudo systemctl daemon-reload
sudo systemctl enable tomcat9-georchestra
```

### Customize Java options

In ```/etc/default/tomcat9-georchestra```, we need to adapt the JAVA_OPTS variable as well:
```
JAVA_OPTS="-Djava.awt.headless=true -XX:+UseConcMarkSweepGC"
```

And later add these lines (change the ```STOREPASSWORD``` string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeorchestra.datadir=/etc/georchestra"

JAVA_OPTS="$JAVA_OPTS \
              -Xms2G \
              -Xmx2G \
              -XX:MaxPermSize=256m"

JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat9/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"

JAVA_OPTS="$JAVA_OPTS \
              -Djava.util.prefs.userRoot=/tmp \
              -Djava.util.prefs.systemRoot=/tmp"
```
This allocates 2GB of your server RAM to all geOrchestra webapps (except proxy, cas and geoserver, which are located in other tomcat instances).

#### GeoNetwork 2.x

If GeoNetwork 2.x (legacy version) is being deployed:
```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeonetwork.dir=/path/to/your/geonetwork_data_dir \
              -Dgeonetwork.schema.dir=/path/to/your/geonetwork_data_dir/config/schema_plugins \
              -Dgeonetwork.jeeves.configuration.overrides.file=/var/lib/tomcat-georchestra/webapps/geonetwork/WEB-INF/config-overrides-georchestra.xml"
```
... where ```/path/to/your/geonetwork_data_dir``` is a directory owned by tomcat, created by checking out this repository [georchestra/geonetwork_minimal_datadir](https://github.com/georchestra/geonetwork_minimal_datadir)

Example:
```
sudo git clone https://github.com/georchestra/geonetwork_minimal_datadir.git /opt/geonetwork_data_dir
sudo chown -R tomcat /opt/geonetwork_data_dir
```

#### GeoNetwork 3.x (geOrchestra 15.12 and above)

If GeoNetwork 3.x is deployed, some extra java environment variables will be
required, because almost everything related to the configuration and the
geOrchestra integration has been exported outside of the webapp, into the geOrchestra datadir.


```
sudo git clone https://github.com/georchestra/config.git /etc/georchestra
sudo git clone -b gn3.4.1 https://github.com/georchestra/geonetwork_minimal_datadir.git /opt/geonetwork_data_dir
sudo chown -R tomcat /opt/geonetwork_data_dir
```

Customize the `/etc/georchestra/geonetwork/geonetwork.properties`, so that the
`geonetwork.dir` reflects the path where you actually cloned the default
datadir in the previous step, e.g. `/opt/geonetwork_data_dir`.

Then edit the following files in `/etc/georchestra/geonetwork/config`:

- `config-datadir-georchestra.xml`
- `config-db-georchestra.xml`
- `config-logging-georchestra.xml`
- `config-overrides-georchestra.xml`
- `config-security-georchestra.xml`


And replace every occurences of `${georchestra.datadir}` or `${env:georchestra.datadir}` by `/etc/georchestra`, unless you are already used to the datadir-enabled configuration for georchestra, see https://github.com/georchestra/config#georchestra-default-datadir for more info.

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


### Configure connector

Normally, the `tomcat9-instance-create` should have correctly set the listening port accordingly ; but we want to check the connector parameters:

In ```/var/lib/tomcat9-georchestra/conf/server.xml```:

```
    <Connector port="8280" protocol="HTTP/1.1"
               connectionTimeout="20000"
               URIEncoding="UTF-8"
               redirectPort="8443" />
```

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
+ReadWritePaths=/var/lib/tomcat9-geoserver0/webapps/
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

### Customize Java options

In ```/etc/default/tomcat9-geoserver0```, we need to change:
```
JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC"
```
...into:
```
JAVA_OPTS="-Djava.awt.headless=true"
```

And later add these lines:
```
JAVA_OPTS="$JAVA_OPTS \
            -DGEOSERVER_DATA_DIR=/path/to/your/geoserver_data_dir \
            -DGEOWEBCACHE_CACHE_DIR=/path/to/your/geowebcache_cache_dir"

JAVA_OPTS="$JAVA_OPTS \
            -Dfile.encoding=UTF8 \
            -Djavax.servlet.request.encoding=UTF-8 \
            -Djavax.servlet.response.encoding=UTF-8"

JAVA_OPTS="$JAVA_OPTS \
            -Xms2G -Xmx2G -XX:PermSize=256M -XX:MaxPermSize=256M"

JAVA_OPTS="$JAVA_OPTS \
            -server \
            -XX:+UseParNewGC \
            -XX:ParallelGCThreads=2 \
            -XX:SoftRefLRUPolicyMSPerMB=36000 \
            -XX:+UseConcMarkSweepGC"
```
This allocates 2Gb of your server RAM to GeoServer.

The ```/path/to/your/geoserver_data_dir``` directory should be owned by tomcat, and created by checking out this repository [georchestra/geoserver_minimal_datadir](https://github.com/georchestra/geoserver_minimal_datadir):

Example:
```
sudo git clone -b master https://github.com/georchestra/geoserver_minimal_datadir.git /opt/geoserver_data_dir
sudo chown -R tomcat /opt/geoserver_data_dir
```
Note that this data dir holds **several branches**: please refer to the repository [README](https://github.com/georchestra/geoserver_minimal_datadir/blob/master/README.md) in order to **choose the correct one**.


As before (change the ```STOREPASSWORD``` string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat9/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"
```

In case your connection to the internet needs to pass through a proxy, you should also add the ```-Dhttp.proxy*``` options here.

### Configure connector

For GeoServer, it is advised to lower the number of simultaneous threads handling incoming requests.
By default Tomcat assumes 200 threads, but experiments show that 20 is a better value.

In ```/var/lib/tomcat9-geoserver0/conf/server.xml```:
```
    <Connector port="8380" protocol="HTTP/1.1"
               connectionTimeout="20000"
               URIEncoding="UTF-8"
               maxThreads="20"
               minSpareThreads="20"
               redirectPort="8443" />

```

### Start the instance

```
sudo service tomcat9-geoserver0 start
```

## Be careful

Remember that the geOrchestra binaries must be built according to the tomcat configuration described above.
By default, forking the template configuration should guarantee this.

Since we assume that :
 - proxy and cas are served by a specific tomcat instance, listening on localhost, port 8180
 - the geOrchestra webapps, except GeoServer, proxy and cas, are served by another tomcat instance, on port 8280
 - GeoServer is served by its own dedicated tomcat instance, which listens on port 8380

... you should verify that your reverse proxy points to port 8180 (the `tomcat9-proxycas` instance).
