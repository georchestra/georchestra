# Tomcat

We need 3 tomcat instances:
 * one for the proxy and cas webapps
 * an other one for geoserver
 * the last one for the other webapps
 
## Prerequisites

```
sudo apt-get install -y tomcat6 tomcat6-user
```

We will deactivate the default tomcat instance, just to be sure:
```
sudo update-rc.d -f tomcat6 remove
sudo service tomcat6 stop
```

## Keystore

To create a Keystore, enter the following:
```
sudo keytool -genkey \
    -alias georchestra_localhost \
    -keystore /etc/tomcat6/keystore \
    -storepass STOREPASSWORD \
    -keypass STOREPASSWORD \
    -keyalg RSA \
    -keysize 2048 \
    -dname "CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=FR" 
```
... where ```STOREPASSWORD``` is a password you choose, and the ```dname``` string is customized.


In case the LDAP connection uses SSL (which is not the default in the geOrchestra template configuration), the certificate must be added to the keystore. 

First get the public key:
```
echo "" | openssl s_client -connect LDAPHOST:LDAPPORT -showcerts 2>/dev/null | openssl x509 -out /tmp/certfile.txt
```
... and then add it to the keystore:
```
sudo keytool -import -alias cert_ldap -file /tmp/certfile.txt -keystore /etc/tomcat6/keystore
```

Finally, verify the list of keys in keystore:
```
keytool -keystore /etc/tomcat6/keystore -list
```


## Tomcat proxycas

### Create the instance

Let's create an instance named ```tomcat-proxycas```:

```
sudo tomcat6-instance-create -p 8180 -c 8105 /var/lib/tomcat-proxycas
```
8180 will be the HTTP port and 8105 the stop port.


Then:
```
sudo mkdir /var/lib/tomcat-proxycas/conf/policy.d
sudo touch /var/lib/tomcat-proxycas/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 /var/lib/tomcat-proxycas
sudo cp /etc/init.d/tomcat6 /etc/init.d/tomcat-proxycas
sudo cp /etc/default/tomcat6 /etc/default/tomcat-proxycas
```

Finally, edit the ```/etc/init.d/tomcat-proxycas``` script, find the following line:
```
# Provides:          tomcat6
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
              -Xms256m \
              -Xmx256m \
              -XX:MaxPermSize=128m"

JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat6/keystore \
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
               keystoreFile="/etc/tomcat6/keystore"
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
sudo update-rc.d tomcat-proxycas defaults 90
sudo service tomcat-proxycas start
```





## Tomcat geOrchestra

### Create the instance

Same here ... just changing names and ports.
```
sudo tomcat6-instance-create -p 8280 -c 8205 /var/lib/tomcat-georchestra
sudo mkdir /var/lib/tomcat-georchestra/conf/policy.d
sudo touch /var/lib/tomcat-georchestra/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 /var/lib/tomcat-georchestra
sudo cp /etc/init.d/tomcat6 /etc/init.d/tomcat-georchestra
sudo cp /etc/default/tomcat6 /etc/default/tomcat-georchestra
```

Finally, edit the ```/etc/init.d/tomcat-georchestra``` script, find the following line:
```
# Provides:          tomcat6
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
              -Xms2G \
              -Xmx2G \
              -XX:MaxPermSize=256m"

JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat6/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"

JAVA_OPTS="$JAVA_OPTS \              
              -Djava.util.prefs.userRoot=/tmp \
              -Djava.util.prefs.systemRoot=/tmp"
```
This allocates 2Gb of your server RAM to all geOrchestra webapps (except proxy, cas and geoserver).

If GeoNetwork is deployed:
```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeonetwork.dir=/path/to/your/geonetwork_data_dir \
              -Dgeonetwork.schema.dir=/path/to/your/geonetwork_data_dir/config/schema_plugins \
              -Dgeonetwork.jeeves.configuration.overrides.file=/var/lib/tomcat-georchestra/webapps/geonetwork/WEB-INF/config-overrides-georchestra.xml"
```
... where ```/path/to/your/geonetwork_data_dir``` is a directory owned by tomcat6, created by checking out this repository [georchestra/geonetwork_minimal_datadir](https://github.com/georchestra/geonetwork_minimal_datadir)

Example:
```
sudo git clone https://github.com/georchestra/geonetwork_minimal_datadir.git /opt/geonetwork_data_dir
sudo chown -R tomcat6 /opt/geonetwork_data_dir
```

If the extractor application is deployed:
```
JAVA_OPTS="$JAVA_OPTS \
               -Dorg.geotools.referencing.forceXY=true \
               -Dextractor.storage.dir=/path/to/temporary/extracts/"
```
... where ```/path/to/temporary/extracts/``` is a directory owned by tomcat6 in a dedicated server partition.

In case your connection to the internet is proxied, you should also add the ```-Dhttp.proxy*``` options here.


### Configure connector

In ```/var/lib/tomcat-georchestra/conf/server.xml```:
```
    <Connector port="8280" protocol="HTTP/1.1" 
               connectionTimeout="20000" 
               URIEncoding="UTF-8"
               redirectPort="8443" />

```

### Start the instance

```
sudo update-rc.d tomcat-georchestra defaults 90
sudo service tomcat-georchestra start
```





## Tomcat GeoServer

### Create the instance

```
sudo tomcat6-instance-create -p 8380 -c 8305 /var/lib/tomcat-geoserver0
sudo mkdir /var/lib/tomcat-geoserver0/conf/policy.d
sudo touch /var/lib/tomcat-geoserver0/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 /var/lib/tomcat-geoserver0
sudo cp /etc/init.d/tomcat6 /etc/init.d/tomcat-geoserver0
sudo cp /etc/default/tomcat6 /etc/default/tomcat-geoserver0
```

Finally, edit the ```/etc/init.d/tomcat-geoserver0``` script, find the following line:
```
# Provides:          tomcat6
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
            -XX:NewRatio=2 \
            -XX:+AggressiveOpts"
```
This allocates 2Gb of your server RAM to GeoServer.

The ```/path/to/your/geoserver_data_dir``` directory should be owned by tomcat6, and created by checking out this repository [georchestra/geoserver_minimal_datadir](https://github.com/georchestra/geoserver_minimal_datadir):

Example:
```
sudo git clone -b master https://github.com/georchestra/geoserver_minimal_datadir.git /opt/geoserver_data_dir
sudo chown -R tomcat6 /opt/geoserver_data_dir
```
Note that this data dir holds **several branches**: please refer to the repository [README](https://github.com/georchestra/geoserver_minimal_datadir/blob/master/README.md) in order to **choose the correct one**.


As before (change the ```STOREPASSWORD``` string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat6/keystore \
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
sudo update-rc.d tomcat-geoserver0 defaults 90
sudo service tomcat-geoserver0 start
```


