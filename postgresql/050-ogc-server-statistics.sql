--
-- PostgreSQL database
--

BEGIN;

CREATE SCHEMA ogcstatistics;
SET search_path TO ogcstatistics,public,pg_catalog;

-- Create new version of ogc_services_log table
CREATE TABLE ogc_services_log(
  user_name character varying(255),
  date timestamp without time zone,
  service character varying(5),
  layer character varying(255),
  id bigserial,
  request character varying(20),
  org character varying(255),
  roles text[]
);

-- Return name of table that correspond to specified date, also create table if it does
-- not exists and indexes on table of previous month
CREATE OR REPLACE FUNCTION get_partition_table(my_date timestamp without time zone)
  RETURNS character varying AS
$BODY$
DECLARE
    my_table_name character varying;
    my_month character varying;
    my_year character varying;
    previous_month character varying;
    previous_year character varying;
    previous_table_name character varying;
    previous_table_oid oid;
    borne_sup date;
    borne_inf date;
    query character varying;
    base_table_name character varying = 'ogc_services_log';
    base_schema_name character varying = 'ogcstatistics';
BEGIN

  -- Generate table name
  my_month := EXTRACT(MONTH FROM my_date);
  my_year := EXTRACT(YEAR FROM my_date);

  my_table_name := base_table_name || '_y' || my_year || 'm' || my_month;

  -- RAISE NOTICE 'table name %.%', base_schema_name, my_table_name;

  -- Test if table already exists
  IF NOT (SELECT count(*) > 0
          FROM information_schema.tables
          WHERE table_schema = base_schema_name
    AND table_name = my_table_name) THEN

    borne_inf := (my_year || '-' || my_month || '-01')::date;
    borne_sup := borne_inf + INTERVAL '1 month';

    query := 'CREATE TABLE ' || base_schema_name || '.' || my_table_name || '( CHECK ( date >= DATE ''' || borne_inf || ''' AND date < DATE ''' || borne_sup || ''' ) ';
    query := query || ') INHERITS (' || base_schema_name || '.' || base_table_name || ')';

    -- Create table if it does not exists
    EXECUTE query;

    -- Create Indexes on previous table for user_name and date fields
    previous_month := EXTRACT(MONTH FROM (my_date - INTERVAL '1 month'));
    previous_year := EXTRACT(YEAR FROM (my_date - INTERVAL '1 month'));
    previous_table_name := base_table_name || '_y' || previous_year || 'm' || previous_month;

    -- Check if previous table exists
    IF (SELECT count(*) > 0
        FROM information_schema.tables
        WHERE table_schema = base_schema_name
        AND table_name = previous_table_name) THEN

      previous_table_oid := (base_schema_name || '.' || previous_table_name)::regclass::int;
      -- Check if indexes already exists
      IF NOT (WITH stat_indexes AS (SELECT t.oid,
                                           t.relname AS table_name,
                                           i.relname AS index_name,
                                           array_agg(a.attname) AS column_names
                                    FROM
                                       pg_class t,
                                       pg_class i,
                                       pg_index ix,
                                       pg_attribute a
                                    WHERE
                                       t.oid = ix.indrelid
                                       AND i.oid = ix.indexrelid
                                       AND a.attrelid = t.oid
                                       AND a.attnum = ANY(ix.indkey)
                                       AND t.relkind = 'r'
                                       AND t.oid = previous_table_oid
                                    GROUP BY t.oid, t.relname, index_name)
              SELECT count(*) = 2
              FROM stat_indexes
              WHERE column_names IN (ARRAY['date']::name[], ARRAY['user_name']::name[])) THEN

        query := 'CREATE INDEX ' || previous_table_name || '_date_idx ON ' || base_schema_name || '.' || previous_table_name || '(date)';
        EXECUTE query;
        query := 'CREATE INDEX ' || previous_table_name || '_user_name_idx ON ' || base_schema_name || '.' || previous_table_name || '(user_name)';
        EXECUTE query;

      END IF;

    END IF;

  END IF;

  RETURN base_schema_name || '.' || my_table_name;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE;

COMMENT ON FUNCTION get_partition_table(timestamp without time zone) IS 'Return name of table that correspond to specified date, also create table if it does not exists and indexes on table of previous month';



CREATE OR REPLACE FUNCTION insert_stat_trigger_function()
RETURNS TRIGGER AS $$
DECLARE
  table_name character varying;
BEGIN

  table_name := ogcstatistics.get_partition_table(NEW.date);

  -- insert record in child table
  EXECUTE 'INSERT INTO ' || table_name || ' VALUES ($1.*)' USING NEW;
  -- do *not* insert record in master table
  RETURN NULL;

END;
$$
LANGUAGE plpgsql;



CREATE TRIGGER insert_stat_trigger
    BEFORE INSERT ON ogc_services_log
    FOR EACH ROW EXECUTE PROCEDURE insert_stat_trigger_function();

COMMIT;
