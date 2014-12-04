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

Let's create an instance named ```proxycas```:

```
cd /var/lib
sudo tomcat6-instance-create -p 8180 -c 8005 proxycas
```
8180 will be the HTTP port and 8005 the stop port.


Then:
```
sudo mkdir proxycas/conf/policy.d
sudo touch proxycas/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 proxycas
cd /etc/init.d/
sudo cp tomcat6 proxycas
```

Finally, we make the instance start by default with the OS, and check it works:
```
sudo update-rc.d proxycas defaults 90
sudo service proxycas start
```

## Tomcat geOrchestra

Same here ... just changing names and ports.
```
cd /var/lib
sudo tomcat6-instance-create -p 8181 -c 8006 georchestra
sudo mkdir georchestra/conf/policy.d
sudo touch georchestra/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 georchestra
cd /etc/init.d/
sudo cp tomcat6 georchestra
sudo update-rc.d georchestra defaults 90
sudo service georchestra start
```

## Tomcat GeoServer

```
cd /var/lib
sudo tomcat6-instance-create -p 8190 -c 8015 geoserver0
sudo mkdir geoserver0/conf/policy.d
sudo touch geoserver0/conf/policy.d/empty.policy
sudo chown -R tomcat6:tomcat6 geoserver0
cd /etc/init.d/
sudo cp tomcat6 geoserver0
sudo update-rc.d geoserver0 defaults 90
sudo service geoserver0 start
```

## misc

connector config: proxyhost, proxyport

if proxies: ```-Dhttp.proxyHost=... -Dhttp.proxyPort=.... -Dhttps.proxyHost=... -Dhttps.proxyPort=....```

