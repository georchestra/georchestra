# A single Tomcat instance


Install Tomcat from package
---------------------------

This one Tomcat instance installation is for testing purposes only. When running a real-world SDI, you will need to use various Tomcat instances.

    sudo apt-get install tomcat6

Remove any webapp

	sudo rm -rf /var/lib/tomcat6/webapps/*
	
Create a directory for tomcat6 java preferences (to avoid a `WARNING: Couldn't flush user prefs: java.util.prefs.BackingStoreException: Couldn't get file lock.` error)

	sudo mkdir /usr/share/tomcat6/.java
	sudo chown tomcat6:tomcat6 /usr/share/tomcat6/.java


Environment variables
----------------------

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS \
              -Djava.awt.headless=true \
              -Xms4G \
              -Xmx8G \
              -XX:MaxPermSize=256m "
```

Some geOrchestra applications will require you to add more JAVA_OPTS, read below...

Keystore/Trustore
-------------------

* Keystore creation (change the "mdpstore" password)

        cd /etc/tomcat6/
        sudo keytool -genkey -alias georchestra_localhost -keystore keystore -storepass mdpstore -keypass mdpstore -keyalg RSA -keysize 2048

    Put "localhost" in "first name and second name" since sec-proxy and CAS are on the same tomcat

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    In case the LDAP connection uses SSL, the certificate must be added to the keystore. First get the public key:

        echo "" | openssl s_client -connect LDAPHOST:LDAPPORT -showcerts 2>/dev/null | openssl x509 -out /tmp/certfile.txt

    and then add it to the keystore

        sudo keytool -import -alias cert_ldap -file /tmp/certfile.txt -keystore /etc/tomcat6/keystore

    Verify the list of keys in keystore

        keytool -keystore keystore -list
       
* truststore config

```
sudo nano /etc/default/tomcat6
```

```
JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=/etc/tomcat6/keystore -Djavax.net.ssl.trustStorePassword=mdpstore"
```

* connectors config

```
sudo nano /etc/tomcat6/server.xml
```

```
<Connector port="8080" protocol="HTTP/1.1"
   connectionTimeout="20000"
   URIEncoding="UTF-8"
   redirectPort="8443" />
```

```
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
   URIEncoding="UTF-8"
   maxThreads="150" scheme="https" secure="true"
   clientAuth="false"
   keystoreFile="/etc/tomcat6/keystore"
   keystorePass="mdpstore"
   compression="on"
   compressionMinSize="2048"
   noCompressionUserAgents="gozilla, traviata"
   compressableMimeType="text/html,text/xml,text/javascript,application/x-javascript,application/javascript,text/css" />
```

```
<Connector URIEncoding="UTF-8"
   port="8009"
   protocol="AJP/1.3"
   connectionTimeout="20000"
   redirectPort="8443" />
```
    
* Tomcat restart
 
        sudo service tomcat6 restart