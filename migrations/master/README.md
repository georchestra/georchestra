# Postgresql docker image

⚠️ If you use postgresql in docker, there's a change since https://github.com/georchestra/georchestra/pull/4574

The volume used by postgres to store data is now `/var/lib/postgresql/` instead of `/var/lib/postgresql/data`.

# Drop RabbitMQ support 

⚠️ RabbitMQ support is dropped since https://github.com/georchestra/georchestra-gateway/pull/264

**Only compatible with GW 3.0+

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
