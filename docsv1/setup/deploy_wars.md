# Deploying the webapps

This section will describe how to deploy the official geOrchestra webapps into
previously set up tomcat instances.

There are other possibilities to do so (e.g. installing the debian packages), but this is outside of the scope of this one.

## tomcat-proxycas

These can be both deployed using the following command:

```
wget -O /tmp/ROOT.war https://packages.georchestra.org/wars-master/ROOT-generic.war
mv /tmp/ROOT.war /var/lib/tomcat9-proxycas/webapps/

wget -O /tmp/cas.war https://packages.georchestra.org/wars-master/cas-generic.war
mv /tmp/cas.war /var/lib/tomcat9-proxycas/webapps/

```

## tomcat-georchestra

```

wget -O /tmp/header.war https://packages.georchestra.org/wars-master/header-generic.war
mv /tmp/header.war /var/lib/tomcat9-georchestra/webapps/

wget -O /tmp/geonetwork.war https://packages.georchestra.org/wars-master/geonetwork.war
mv /tmp/geonetwork.war /var/lib/tomcat9-georchestra/webapps/

wget -O /tmp/analytics.war https://packages.georchestra.org/wars-master/analytics-generic.war
mv /tmp/analytics.war /var/lib/tomcat9-georchestra/webapps

wget -O /tmp/console.war https://packages.georchestra.org/wars-master/console-generic.war
mv /tmp/console.war /var/lib/tomcat9-georchestra/webapps

```


## tomcat-geoserver0

```
wget -O /tmp/geoserver.war https://packages.georchestra.org/wars-master/geoserver-generic.war
mv /tmp/geoserver.war /var/lib/tomcat9-georchestra/webapps/

wget -O /tmp/geowebcache.war https://packages.georchestra.org/wars-master/geowebcache-generic.war
mv /tmp/geowebcache.war /var/lib/tomcat9-georchestra/webapps/
```
