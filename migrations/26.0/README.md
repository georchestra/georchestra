# From 25.0 to 26.0

## PostgreSQL Docker Image

⚠️ If you use PostgreSQL in Docker, there is a change since https://github.com/georchestra/georchestra/pull/4574

The volume used by PostgreSQL to store data is now `/var/lib/postgresql/` instead of `/var/lib/postgresql/data`.

## Logback Usage

All libraries and Console now use Logback (embedded with the Spring parent).

Folders in the geOrchestra data directory now use `<georchestra.datadir>/<context>/logback/logback.xml`. For example: `/etc/georchestra/console/logback/logback.xml`

## Gateway

All migrations notes for Gateway are here : https://github.com/orgs/georchestra/discussions/4671

### RabbitMQ Support Removed

⚠️ RabbitMQ support was dropped in https://github.com/georchestra/georchestra-gateway/pull/264

**Only compatible with GW 3.0+**

If you use Gateway 3.0+, you need to switch the Gateway configuration to point to the Console endpoint instead of RabbitMQ.

```yaml
georchestra:
  gateway:
    security:
#      events:
#        accountcreated:
#          # Set this URL to the Console endpoint to receive an email when a new user logs in
#          # for the first time and is created in LDAP.
#          url: "http://console:8080/console/internal/events/accountcreated"
```

## Removal of data-api and Java header

As GeoServer now provides a better implementation of OGC API Features, the data-api is no longer supported.

The Java header is now fully replaced by the web component one, embedded since geOrchestra 25.

## New datafeeder and analytics

https://github.com/georchestra/datafeeder replaces the older datafeeder, allowing data imports from more formats and sources.

https://github.com/georchestra/analytics/ also replaces the old analytics module, and is now compatible with SP and Gateway.