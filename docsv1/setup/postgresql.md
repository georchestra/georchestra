# Setting up the geOrchestra database

The "georchestra" database hosts several schemas, which are specific to the deployed modules:
```
CREATE USER georchestra WITH NOCREATEDB NOCREATEROLE PASSWORD 'georchestra';
CREATE DATABASE georchestra WITH OWNER georchestra TEMPLATE template0 ENCODING UTF8;
```

Note 1: It is of course possible to store webapp-specific schemas in separate databases, taking advantage of geOrchestra's extreme configurability.

Note 2: PostGIS extensions are not required in the georchestra database, unless GeoFence is deployed (see below).


## Console schema

If the **console** webapp is deployed:
```
psql -d georchestra -f postgresql/040-console.sql
```

## GeoFence schema

If **geofence** is deployed:
```
psql -d georchestra -f postgresql/010-create-extension.sql
psql -d georchestra -f postgresql/080-geofence.sql
```

## OGC statistics schema

If the **security proxy** is deployed and its log4j uses the `org.georchestra.ogcservstatistics.log4j.OGCServicesAppender` as configured in the [datadir](https://github.com/georchestra/datadir/blob/docker-master/security-proxy/log4j/log4j.properties):
```
psql -d georchestra -f postgresql/050-ogc-server-statistics.sql
```

## GeoWebCache schema

If geowebcache is used, it creates a geowebcache schema to store quota and relevant infos for
geowebcache. This setup is highly encouraged in a production environment to replace the local H2 database.

```
psql -d georchestra -f postgresql/110-geowebcache.sql
```

## Change ownership of database objects

Ensure geOrchestra database user is owner of database. If your database is dedicated to geOrchestra (no other
apps are running in same database), you can use following procedure to reset ownership of all objects to selected user, for
example ```georchestra``` :

```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/postgresql/fix-owner.sql -O /tmp/fix-owner.sql
psql -d georchestra -f /tmp/fix-owner.sql
psql -d georchestra -c "SELECT change_owner('console', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('ogcstatistics', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('geofence', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('public', 'georchestra');";
```

## GeoNetwork schema

If **geonetwork** is to be deployed, you need to create a dedicated schema:
```
psql -d georchestra -c "CREATE SCHEMA geonetwork;"
```

## Cleanup maintenance function

Finally, you can drop maintenance function :
```
psql -d georchestra -c "DROP FUNCTION change_owner(text, text);";
```
