# Tomcat

We need 3 tomcat instances:
 * one for the proxy and cas webapps
 * an other one for geoserver
 * the last one for the other webapps
 
## Prerequisites

```
sudo apt-get install -y tomcat8 tomcat8-user
```

We will deactivate the default tomcat instance, just to be sure:
```
sudo update-rc.d -f tomcat8 remove
sudo service tomcat8 stop
```

## Keystore

To create a keystore, enter the following:
```
sudo keytool -genkey \
    -alias georchestra_localhost \
    -keystore /etc/tomcat8/keystore \
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
    -destkeystore /etc/tomcat8/keystore
```

The password of the srckeystore is "changeit" by default, and should be modified in /etc/default/cacerts.

### SSL

As the SSL certificate is absolutely required, at least for the CAS module, you must add it to the keystore.
```
keytool -import -alias cert_ssl -file /var/www/georchestra/ssl/georchestra.crt -keystore /etc/tomcat8/keystore
```

### LDAP SSL

In case the LDAP connection uses SSL (which is not the default in the geOrchestra template configuration), its certificate must be added to the keystore. 

Here's how:

First get the public key.
```
echo "" | openssl s_client -connect LDAPHOST:LDAPPORT -showcerts 2>/dev/null | openssl x509 -out /tmp/certfile.txt
```
... and then add it to the keystore:
```
sudo keytool -import -alias cert_ldap -file /tmp/certfile.txt -keystore /etc/tomcat8/keystore
```

### Finally, 
verify the list of keys in keystore:
```
keytool -keystore /etc/tomcat8/keystore -list
```


## Tomcat proxycas

### Create the instance

Let's create an instance named ```tomcat-proxycas```:

```
sudo tomcat8-instance-create -p 8180 -c 8105 /var/lib/tomcat-proxycas
```
8180 will be the HTTP port and 8105 the stop port.


Then:
```
sudo mkdir /var/lib/tomcat-proxycas/conf/policy.d
sudo touch /var/lib/tomcat-proxycas/conf/policy.d/empty.policy
sudo chown -R tomcat8:tomcat8 /var/lib/tomcat-proxycas
sudo cp /etc/init.d/tomcat8 /etc/init.d/tomcat-proxycas
sudo cp /etc/default/tomcat8 /etc/default/tomcat-proxycas
```

Finally, edit the ```/etc/init.d/tomcat-proxycas``` script, find the following line:
```
# Provides:          tomcat8
```
... and replace it with:
```
# Provides:          tomcat-proxycas
```

### Customize Java options

In ```/etc/default/tomcat-proxycas```, we need to remove the ```-Xmx128m``` option: 
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
              -Djavax.net.ssl.trustStore=/etc/tomcat8/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"
```

In case your connection to the internet is proxied, you should also add something like this:
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
               keystoreFile="/etc/tomcat8/keystore"
               keystorePass="STOREPASSWORD"
               compression="on"
               compressionMinSize="2048"
               noCompressionUserAgents="gozilla, traviata"
               compressableMimeType="text/html,text/xml,application/xml,text/javascript,application/x-javascript,application/javascript,text/css" />
```
... in which you also take care of changing the ```STOREPASSWORD``` string.


### Start the instance

Finally, we make the instance start by default with the OS, and check it works:
```
sudo insserv tomcat-proxycas
sudo service tomcat-proxycas start
```





## Tomcat geOrchestra

### Create the instance

Same here ... just changing names and ports.
```
sudo tomcat8-instance-create -p 8280 -c 8205 /var/lib/tomcat-georchestra
sudo mkdir /var/lib/tomcat-georchestra/conf/policy.d
sudo touch /var/lib/tomcat-georchestra/conf/policy.d/empty.policy
sudo chown -R tomcat8:tomcat8 /var/lib/tomcat-georchestra
sudo cp /etc/init.d/tomcat8 /etc/init.d/tomcat-georchestra
sudo cp /etc/default/tomcat8 /etc/default/tomcat-georchestra
```

Finally, edit the ```/etc/init.d/tomcat-georchestra``` script, find the following line:
```
# Provides:          tomcat8
```
... and replace it with:
```
# Provides:          tomcat-georchestra
```

### Customize Java options

In ```/etc/default/tomcat-georchestra```, we need to remove the ```-Xmx128m``` option: 
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
              -Djavax.net.ssl.trustStore=/etc/tomcat8/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"

JAVA_OPTS="$JAVA_OPTS \
              -Djava.util.prefs.userRoot=/tmp \
              -Djava.util.prefs.systemRoot=/tmp"
```
This allocates 2Gb of your server RAM to all geOrchestra webapps (except proxy, cas and geoserver).

#### GeoNetwork 2.x

If GeoNetwork 2.x (legacy version) is being deployed:
```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeonetwork.dir=/path/to/your/geonetwork_data_dir \
              -Dgeonetwork.schema.dir=/path/to/your/geonetwork_data_dir/config/schema_plugins \
              -Dgeonetwork.jeeves.configuration.overrides.file=/var/lib/tomcat-georchestra/webapps/geonetwork/WEB-INF/config-overrides-georchestra.xml"
```
... where ```/path/to/your/geonetwork_data_dir``` is a directory owned by tomcat8, created by checking out this repository [georchestra/geonetwork_minimal_datadir](https://github.com/georchestra/geonetwork_minimal_datadir)

Example:
```
sudo git clone https://github.com/georchestra/geonetwork_minimal_datadir.git /opt/geonetwork_data_dir
sudo chown -R tomcat8 /opt/geonetwork_data_dir
```

#### GeoNetwork 3.0.x (geOrchestra 15.12 and above)

If GeoNetwork 3.0.x is deployed, some extra java environment variables will be
required, because almost everything related to the configuration and the
geOrchestra integration has been exported outside the webapp.


```
sudo git clone https://github.com/georchestra/config.git /etc/georchestra
sudo git clone -b gn3.0.x https://github.com/georchestra/geonetwork_minimal_datadir.git /opt/geonetwork_data_dir
sudo chown -R tomcat8 /opt/geonetwork_data_dir
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


And replace every occurence of `${georchestra.datadir}` or `${env:georchestra.datadir}` by `/etc/georchestra`, unless you are already use to the datadir-enabled configuration for georchestra, see https://github.com/georchestra/config#georchestra-default-datadir for more info.

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
... where ```/path/to/temporary/extracts/``` is a directory owned by tomcat8 in a dedicated server partition.

If one of geonetwork or extractorapp is deployed:
```
JAVA_OPTS="$JAVA_OPTS \
               -Djava.util.prefs.userRoot=/var/lib/tomcat-georchestra/temp \
               -Djava.util.prefs.systemRoot=/var/lib/tomcat-georchestra/temp"
```

In case your connection to the internet is proxied, you should also add the ```-Dhttp.proxy*``` options here.


### Configure connector

In ```/var/lib/tomcat-georchestra/conf/server.xml```:
```
    <Connector port="8280" protocol="HTTP/1.1" 
               connectionTimeout="20000" 
               URIEncoding="UTF-8"
               redirectPort="8443" />

```

If the ldapadmin webapp is deployed, the connector must also include these options:
```
               proxyName="georchestra.mydomain.org"
               proxyPort="80"
```
(where ```georchestra.mydomain.org``` is your server FQDN)

### Start the instance

```
sudo insserv tomcat-georchestra
sudo service tomcat-georchestra start
```





## Tomcat GeoServer

### Create the instance

```
sudo tomcat8-instance-create -p 8380 -c 8305 /var/lib/tomcat-geoserver0
sudo mkdir /var/lib/tomcat-geoserver0/conf/policy.d
sudo touch /var/lib/tomcat-geoserver0/conf/policy.d/empty.policy
sudo chown -R tomcat8:tomcat8 /var/lib/tomcat-geoserver0
sudo cp /etc/init.d/tomcat8 /etc/init.d/tomcat-geoserver0
sudo cp /etc/default/tomcat8 /etc/default/tomcat-geoserver0
```

Finally, edit the ```/etc/init.d/tomcat-geoserver0``` script, find the following line:
```
# Provides:          tomcat8
```
... and replace it with:
```
# Provides:          tomcat-geoserver0
```

### Customize Java options

In ```/etc/default/tomcat-geoserver0```, we need to change: 
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

The ```/path/to/your/geoserver_data_dir``` directory should be owned by tomcat8, and created by checking out this repository [georchestra/geoserver_minimal_datadir](https://github.com/georchestra/geoserver_minimal_datadir):

Example:
```
sudo git clone -b master https://github.com/georchestra/geoserver_minimal_datadir.git /opt/geoserver_data_dir
sudo chown -R tomcat8 /opt/geoserver_data_dir
```
Note that this data dir holds **several branches**: please refer to the repository [README](https://github.com/georchestra/geoserver_minimal_datadir/blob/master/README.md) in order to **choose the correct one**.


As before (change the ```STOREPASSWORD``` string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat8/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"
```

In case your connection to the internet is proxied, you should also add the ```-Dhttp.proxy*``` options here.

### Configure connector

For GeoServer, it is advised to lower the number of simultaneous threads handling incoming requests.
By default Tomcat assumes 200 threads, but experiments show that 20 is a better value.

In ```/var/lib/tomcat-geoserver0/conf/server.xml```:
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
sudo insserv tomcat-geoserver0
sudo service tomcat-geoserver0 start
```

## Be careful

Remember that the geOrchestra binaries must be built according to the tomcat configuration described above.
By default, forking the template configuration should guarantee this.

Since we assume that :
 - proxy and cas are served by an http connector on localhost, port 8180
 - the geOrchestra webapps, except GeoServer, proxy and cas, are served by an http connector on port 8280
 - GeoServer is served by an http connector on port 8380

... you should verify that:

1. your reverse proxy points to port 8180 (proxy)
1. your GenerateConfig.groovy file correctly configures your proxy to point to the webapps, namely:

```groovy
def proxyDefaultTarget = "http://localhost:8280"

properties['proxy.mapping'] = """
<entry key="analytics"     value="proxyDefaultTarget/analytics/" />
<entry key="catalogapp"    value="proxyDefaultTarget/catalogapp/" />
<entry key="downloadform"  value="proxyDefaultTarget/downloadform/" />
<entry key="extractorapp"  value="proxyDefaultTarget/extractorapp/" />
<entry key="geonetwork"    value="proxyDefaultTarget/geonetwork/" />
<entry key="geoserver"     value="http://localhost:8380/geoserver/" />
<entry key="geowebcache"   value="proxyDefaultTarget/geowebcache/" />
<entry key="geofence"      value="proxyDefaultTarget/geofence/" />
<entry key="header"        value="proxyDefaultTarget/header/" />
<entry key="ldapadmin"     value="proxyDefaultTarget/ldapadmin/" />
<entry key="mapfishapp"    value="proxyDefaultTarget/mapfishapp/" />
<entry key="static"        value="proxyDefaultTarget/header/" />""".replaceAll("\n|\t","").replaceAll("proxyDefaultTarget",proxyDefaultTarget)
```
