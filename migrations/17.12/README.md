# UPGRADING from 16.12 to 17.12

## Database

In order to increase SQL query speed on the `ogcstatistics` schema, we added
several indexes to the database: one on the `user_name` and `date` columns for
all the tables located in the `ogcstatics.ogc_services_log` partition, except
for the last one. The last table receives new inserts, so adding an index there
would lower performances.

To automatically create these new indexes, you should update the
`get_partition_table()` procedure, using the `update-ogc-server-statistics.sql`
file.

For tables that were already present in your database, you should manually index
them, using the following queries. You have to adapt table names based on the
current state of your database.

For example, to create indexes on the table that holds stats for october 2016:
```sql
CREATE INDEX ogc_services_log_y2016m10_user_name_idx ON ogcstatistics.ogc_services_log_y2016m10 (user_name);
CREATE INDEX ogc_services_log_y2016m10_date_idx ON ogcstatistics.ogc_services_log_y2016m10 (date);
```

You should create these indexes for all tables except for the current month.
