# UPGRADING from 16.12 to 17.12

## Database

In order to increase performance on SQL query on `ogcstatistics` schema, we add
some indexes on database. We add an index on `user_name` and `date` columns for all
tables in `ogcstatics.ogc_services_log` partition except last one. The last table
is used to insert current statistics so adding index on this table will decrease
performance of geOrchestra.

To automaticly create index, update `get_partition_table()` with
`update-ogc-server-statistics.sql` file.

For table that are already present in database, you can add index manually with
following queries. You need to adapt table name based on current state of your
database.

For example, to create index on table that hold stats for october 2016:
```sql
CREATE INDEX ogc_services_log_y2016m10_user_name_idx ON ogcstatistics.ogc_services_log_y2016m10 (user_name);
CREATE INDEX ogc_services_log_y2016m10_date_idx ON ogcstatistics.ogc_services_log_y2016m10 (date);
```
You should apply those queries to all table except table of current month.
