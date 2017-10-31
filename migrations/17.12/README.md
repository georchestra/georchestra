# UPGRADING from 16.12 to 17.12

## Database

In order to increase performance on SQL query on `ogcstatistics` schema, we add
some indexes on database. We add an index on `user_name` and `date` columns for all
tables in `ogcstatics.ogc_services_log` partition except last one. The last table
is used to insert current statistics so adding index on this table will decrease
performance of geOrchestra.

To automaticly create index, update `get_partition_table()` with
`update-ogc-server-statistics.sql` file.
