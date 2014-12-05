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

## Keystore/Trustore

To create a Keystore, enter the following:
```
cd /etc/tomcat6/
sudo keytool -genkey -alias georchestra_localhost -keystore keystore -storepass STOREPASSWORD -keypass STOREPASSWORD -keyalg RSA -keysize 2048
```
... where STOREPASSWORD is a password you choose.

The keytool command will ask a few questions (see below). Put "localhost" in "first name and second name" since the proxy and CAS webapps are on the same tomcat.
```
Quels sont vos prénom et nom ?
  [Unknown] :  localhost
Quel est le nom de votre unité organisationnelle ?
  [Unknown] :
Quelle est le nom de votre organisation ?
  [Unknown] :
Quel est le nom de votre ville de résidence ?
  [Unknown] :
Quel est le nom de votre état ou province ?
  [Unknown] :
Quel est le code de pays ? deux lettres pour cette unit? ?
  [Unknown] :
Est-ce CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown ?
  [non] :  oui
```

In case the LDAP connection uses SSL, the certificate must be added to the keystore. First get the public key:
```
echo "" | openssl s_client -connect LDAPHOST:LDAPPORT -showcerts 2>/dev/null | openssl x509 -out /tmp/certfile.txt
```
... and then add it to the keystore:
```
sudo keytool -import -alias cert_ldap -file /tmp/certfile.txt -keystore /etc/tomcat6/keystore
```

Finally, verify the list of keys in keystore:
```
keytool -keystore keystore -list
```


## Tomcat proxycas

### Create the instance

Let's create an instance named ```tomcat-proxycas```:

```
cd /var/lib
sudo tomcat6-instance-create -p 8180 -c 8005 tomcat-proxycas
```
8180 will be the HTTP port and 8005 the stop port.


Then:
```
sudo mkdir /var/lib/tomcat-proxycas/conf/policy.d
sudo touch /var/lib/tomcat-proxycas/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 /var/lib/proxycas
sudo cp /etc/init.d/tomcat6 /etc/init.d/tomcat-proxycas
sudo cp /etc/default/tomcat6 /etc/default/tomcat-proxycas
```

### Customize Java options

In /etc/default/tomcat-proxycas, we need to remove the -Xmx128m option: 
```
JAVA_OPTS="-Djava.awt.headless=true -XX:+UseConcMarkSweepGC"
```

And later add these lines (change the STOREPASSWORD string):
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

In /var/lib/tomcat-proxycas/conf/server.xml, find the place where the HTTP connector is defined, and change it into:
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
... in which you also take care of changing the STOREPASSWORD string.


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
cd /var/lib
sudo tomcat6-instance-create -p 8181 -c 8006 tomcat-georchestra
sudo mkdir /var/lib/tomcat-georchestra/conf/policy.d
sudo touch /var/lib/tomcat-georchestra/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 /var/lib/tomcat-georchestra
sudo cp /etc/init.d/tomcat6 /etc/init.d/tomcat-georchestra
sudo cp /etc/default/tomcat6 /etc/default/tomcat-georchestra
```
### Customize Java options

In /etc/default/tomcat-georchestra, we need to remove the -Xmx128m option: 
```
JAVA_OPTS="-Djava.awt.headless=true -XX:+UseConcMarkSweepGC"
```

And later add these lines (change the STOREPASSWORD string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Xms2G \
              -Xmx2G \
              -XX:MaxPermSize=256m"

JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat6/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"
```
This allocates 2Gb of your server RAM to all geOrchestra webapps (except proxy, cas and geoserver).

If GeoNetwork is deployed:
```
JAVA_OPTS="$JAVA_OPTS \
              -Dgeonetwork.dir=/path/to/geonetwork-data-dir \
              -Dgeonetwork.schema.dir=/var/lib/tomcat-georchestra/webapps/geonetwork/WEB-INF/data/config/schema_plugins \
              -Dgeonetwork.jeeves.configuration.overrides.file=/var/lib/tomcat-georchestra/webapps/geonetwork/WEB-INF/config-overrides-georchestra.xml"
```

If the extractor application is deployed:
```
JAVA_OPTS="$JAVA_OPTS \
               -Dorg.geotools.referencing.forceXY=true \
               -Dextractor.storage.dir=/path/to/temporary/extracts/"
```
... where /path/to/temporary/extracts/ is a directory owned by tomcat6 in a dedicated server partition.

In case your connection to the internet is proxied, you should also add the -Dhttp.proxy* options here.


### Configure connectors 

TODO

### Start the instance

```
sudo update-rc.d tomcat-georchestra defaults 90
sudo service tomcat-georchestra start
```





## Tomcat GeoServer

### Create the instance

```
cd /var/lib
sudo tomcat6-instance-create -p 8190 -c 8015 tomcat-geoserver0
sudo mkdir /var/lib/tomcat-geoserver0/conf/policy.d
sudo touch /var/lib/tomcat-geoserver0/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 /var/lib/tomcat-geoserver0
sudo cp /etc/init.d/tomcat6 /etc/init.d/tomcat-geoserver0
sudo cp /etc/default/tomcat6 /etc/default/tomcat-geoserver0
```

### Customize Java options

In /etc/default/tomcat-geoserver0, we need to remove the -Xmx128m option: 
```
JAVA_OPTS="-Djava.awt.headless=true -XX:+UseConcMarkSweepGC"
```

And later add these lines:
```
JAVA_OPTS="$JAVA_OPTS \
            -Xms2G -Xmx2G -XX:PermSize=256m -XX:MaxPermSize=256m \
            -DGEOSERVER_DATA_DIR=/path/to/geoserver/data/dir \
            -DGEOWEBCACHE_CACHE_DIR=/path/to/geowebcache/cache/dir \
            -Dfile.encoding=UTF8 \
            -Djavax.servlet.request.encoding=UTF-8 \
            -Djavax.servlet.response.encoding=UTF-8 \
            -server \
            -XX:+UseParNewGC -XX:ParallelGCThreads=2 \
            -XX:SoftRefLRUPolicyMSPerMB=36000 \
            -XX:NewRatio=2 \
            -XX:+AggressiveOpts"
```
This allocates 2Gb of your server RAM to GeoServer.

As before (change the STOREPASSWORD string):
```
JAVA_OPTS="$JAVA_OPTS \
              -Djavax.net.ssl.trustStore=/etc/tomcat6/keystore \
              -Djavax.net.ssl.trustStorePassword=STOREPASSWORD"
```

In case your connection to the internet is proxied, you should also add the -Dhttp.proxy* options here.

### Configure connectors 

TODO

### Start the instance

```
sudo update-rc.d tomcat-geoserver0 defaults 90
sudo service tomcat-geoserver0 start
```


