BEGIN;

SET search_path TO ogcstatistics,public,pg_catalog;

CREATE OR REPLACE FUNCTION get_partition_table(my_date date)
  RETURNS character varying AS
$BODY$
DECLARE
    my_table_name character varying;
    my_month character varying;
    my_year character varying;
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

	END IF;

	RETURN base_schema_name || '.' || my_table_name;
	
END;
$BODY$
  LANGUAGE plpgsql VOLATILE;

COMMENT ON FUNCTION get_partition_table(date) IS 'Table name that correspond to specified date, also create this table if it does not exists';



CREATE OR REPLACE FUNCTION insert_stat_trigger_function()
RETURNS TRIGGER AS $$
DECLARE
	table_name character varying;
BEGIN

	table_name := get_partition_table(NEW.date);

	-- insert record in child table
	EXECUTE 'INSERT INTO ' || table_name || ' VALUES ($1.*)' USING NEW;

	-- do *not* insert record in master table
	RETURN NULL;

END;
$$
LANGUAGE plpgsql;



-- Prevent application to add records to old version of ogc_services_log table
ALTER TABLE ogc_services_log RENAME TO ogc_services_log_old;

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


CREATE TRIGGER insert_stat_trigger
    BEFORE INSERT ON ogc_services_log
    FOR EACH ROW EXECUTE PROCEDURE insert_stat_trigger_function();

COMMIT;
