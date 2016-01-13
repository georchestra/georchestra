# Setting up the geOrchestra database

The "georchestra" database hosts several schemas, which are specific to the deployed modules:
```
createuser -SDRI www-data
createdb -E UTF8 -T template0 -O www-data georchestra
psql -d georchestra -c "ALTER USER \"www-data\" WITH PASSWORD 'www-data';"
```

Note 1: It is of course possible to store webapp-specific schemas in separate databases, taking advantage of geOrchestra's extreme configurability.

Note 2: PostGIS extensions are not required in the georchestra database, unless GeoFence is deployed (see below), or ```shared.psql.jdbc.driver=org.postgis.DriverWrapper``` in your configuration (but this is not the default setup).

## GeoNetwork schema

If **geonetwork** is to be deployed, you need to create a dedicated user and schema:
```
createuser -SDRI geonetwork
psql -d georchestra -c "ALTER USER geonetwork WITH PASSWORD 'www-data';"
psql -d georchestra -c 'CREATE SCHEMA AUTHORIZATION geonetwork;'
```

## Viewer schema

If **mapfishapp** is deployed:
```
psql -d georchestra -f postgresql/mapfishapp.sql
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON SCHEMA mapfishapp TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA mapfishapp TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA mapfishapp TO "www-data";'
```

## Ldapadmin schema

If the **ldapadmin** webapp is deployed:
```
psql -d georchestra -f postgresql/ldapadmin.sql
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON SCHEMA ldapadmin TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ldapadmin TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ldapadmin TO "www-data";'
```

## GeoFence schema

If **geofence** is deployed: (make sure to set the correct values for the ```baseURL```, ```username``` and ```password``` fields in the ```geofence.gf_gsinstance``` table)
```
psql -d georchestra -c 'CREATE EXTENSION postgis;'
psql -d georchestra -c 'GRANT SELECT ON public.spatial_ref_sys to "www-data";'
psql -d georchestra -c 'GRANT SELECT,INSERT,DELETE ON public.geometry_columns to "www-data";'
wget --no-check-certificate https://raw.githubusercontent.com/georchestra/geofence/georchestra-15.06/doc/setup/sql/002_create_schema_postgres.sql -O /tmp/geofence.sql
psql -d georchestra -f /tmp/geofence.sql
```
in the next query, replace every '@...@' with the values of your shared.maven.filters!
```
psql -d georchestra -c "INSERT INTO geofence.gf_gsinstance (id, baseURL, dateCreation, description, name, password, username) values (0, 'http(s)://@shared.server.name@/geoserver', 'now', 'locale geoserver', 'default-gs', '@shared.privileged.geoserver.pass@', '@shared.privileged.geoserver.user@');"
```
and continue
```
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON SCHEMA geofence TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA geofence TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA geofence TO "www-data";'
```

## Download form schema

If the **downloadform** module is deployed and ```shared.download_form.activated``` is true in your setup (false by default):
```
psql -d georchestra -f postgresql/downloadform.sql
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON SCHEMA downloadform TO "www-data";'
psql -d georchestra -c 'GRANT USAGE ON SCHEMA downloadform TO "geonetwork";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA downloadform TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA downloadform TO "www-data";'
psql -d georchestra -c 'GRANT SELECT ON downloadform.geonetwork_log TO "geonetwork";'
```

## OGC statistics schema

If the **security proxy** is deployed and ```shared.ogc.statistics.activated``` is true in your setup (false by default):
```
psql -d georchestra -f postgresql/ogcstatistics.sql
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON SCHEMA ogcstatistics TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ogcstatistics TO "www-data";'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ogcstatistics TO "www-data";'
```
