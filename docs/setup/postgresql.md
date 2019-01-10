# Setting up the geOrchestra database

The "georchestra" database hosts several schemas, which are specific to the deployed modules:
```
createuser -SDRI georchestra
createdb -E UTF8 -T template0 -O georchestra georchestra
psql -d georchestra -c "ALTER USER \"georchestra\" WITH PASSWORD 'georchestra';"
```

Note 1: It is of course possible to store webapp-specific schemas in separate databases, taking advantage of geOrchestra's extreme configurability.

Note 2: PostGIS extensions are not required in the georchestra database, unless GeoFence is deployed (see below), or ```shared.psql.jdbc.driver=org.postgis.DriverWrapper``` in your configuration (but this is not the default setup).

## Viewer schema

If **mapfishapp** is deployed:
```
psql -d georchestra -f postgresql/020-mapfishapp.sql
```

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

If the **security proxy** is deployed and ```OGC_STATISTICS``` is true in your setup ([true by default](https://github.com/georchestra/datadir/blob/18.06/analytics/js/GEOR_custom.js#L4)):
```
psql -d georchestra -f postgresql/050-ogc-server-statistics.sql
```

### Extractorapp schema

If the **extractor app** is deployed:
```
psql -d georchestra -f postgresql/010-create-extension.sql
psql -d georchestra -f postgresql/090-extractor-app.sql
```

### Atlas schema

If the **Atlas** is deployed:
```
psql -d georchestra -f postgresql/060-atlas.sql
```

## Change ownership of database objects

Ensure geOrchestra database user is owner of database. If your database is dedicated to geOrchestra (no other
apps are running in same database), you can use following procedure to reset ownership of all objects to selected user, for
example ```georchestra``` :

```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/postgresql/fix-owner.sql -O /tmp/fix-owner.sql
psql -d georchestra -f /tmp/fix-owner.sql
psql -d georchestra -c "SELECT change_owner('mapfishapp', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('console', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('ogcstatistics', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('extractorapp', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('geofence', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('atlas', 'georchestra');";
psql -d georchestra -c "SELECT change_owner('public', 'georchestra');";
```

## GeoNetwork schema

If **geonetwork** is to be deployed, you need to create a dedicated user and schema:
```
createuser -SDRI geonetwork
psql -d georchestra -c "ALTER USER geonetwork WITH PASSWORD 'georchestra';"
psql -d georchestra -c 'CREATE SCHEMA AUTHORIZATION geonetwork;'
psql -d georchestra -c "SELECT change_owner('geonetwork', 'geonetwork');";
```

## Cleanup maintenance function

Finally, you can drop maintenance function :
```
psql -d georchestra -c "DROP FUNCTION change_owner(text, text);";
```
