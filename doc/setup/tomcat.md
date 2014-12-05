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

## Tomcat proxycas

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
```

Finally, we make the instance start by default with the OS, and check it works:
```
sudo update-rc.d tomcat-proxycas defaults 90
sudo service tomcat-proxycas start
```

## Tomcat geOrchestra

Same here ... just changing names and ports.
```
cd /var/lib
sudo tomcat6-instance-create -p 8181 -c 8006 tomcat-georchestra
sudo mkdir /var/lib/tomcat-georchestra/conf/policy.d
sudo touch /var/lib/tomcat-georchestra/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 /var/lib/tomcat-georchestra
sudo cp /etc/init.d/tomcat6 /etc/init.d/tomcat-georchestra
sudo update-rc.d tomcat-georchestra defaults 90
sudo service tomcat-georchestra start
```

## Tomcat GeoServer

```
cd /var/lib
sudo tomcat6-instance-create -p 8190 -c 8015 tomcat-geoserver0
sudo mkdir /var/lib/tomcat-geoserver0/conf/policy.d
sudo touch /var/lib/tomcat-geoserver0/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 /var/lib/tomcat-geoserver0
sudo cp /etc/init.d/tomcat6 /etc/init.d/tomcat-geoserver0
sudo update-rc.d tomcat-geoserver0 defaults 90
sudo service tomcat-geoserver0 start
```

## misc

connector config: proxyhost, proxyport

if proxies: ```-Dhttp.proxyHost=... -Dhttp.proxyPort=.... -Dhttps.proxyHost=... -Dhttps.proxyPort=....```

