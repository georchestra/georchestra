# Postgresql docker image

⚠️ If you use postgresql in docker, there's a change since https://github.com/georchestra/georchestra/pull/4574

The volume used by postgres to store data is now `/var/lib/postgresql/` instead of `/var/lib/postgresql/data`.

# Usage of logback

All librairies and console are now using logback (which is embedded with spring parent).

Folders use in georchestra datadir now use <georchestra.datadir>/<context>/logback/logback.xml. E.g: /etc/georchestra/console/logback/logback.xml

# Gateway
## Drop RabbitMQ support 

⚠️ RabbitMQ support is dropped since https://github.com/georchestra/georchestra-gateway/pull/264

**Only compatible with GW 3.0+**

If you use Gateway 3.0+, you need to switch configuration of the gateway to point to console's endpoint instead of RabbitMQ. 

```yaml
georchestra:
  gateway:
    security:
#      events:
#        accountcreated:
#          # Set this url to console's endpoint to be able to receive an email when a new user logs in
#          #  for the first time and is created in LDAP.
#          url: "http://console:8080/console/internal/events/accountcreated"
```

# Removing data-api and header


As geoserver now provides a better implementation of OGC API Features, the data-api is not supported anymore. 

Java header is now fully replaced by the webcomponent one, which is embedded since geOrchestra 25. 


# Introduce new datafeeder and analytics

https://github.com/georchestra/datafeeder is replacing the older one, allowing to add data from more formats and sources. 

https://github.com/georchestra/analytics/ is also replacing the old one, now compatible with SP and gateway.