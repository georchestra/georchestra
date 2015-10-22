--
-- PostgreSQL database
--

BEGIN;

CREATE SCHEMA ogcstatistics;

SET search_path TO ogcstatistics,public,pg_catalog;


CREATE TABLE ogc_services_log (
  user_name character varying(255),
  date date,
  service character varying(5),
  layer character varying(255),
  id bigserial NOT NULL,
  request character varying(20),
  org character varying(255),
  CONSTRAINT primary_key PRIMARY KEY (id )
);

CREATE INDEX user_name_index ON ogc_services_log USING btree (user_name);
CREATE INDEX date_index ON ogc_services_log USING btree (date);
CREATE INDEX service_index ON ogc_services_log USING btree (service);
CREATE INDEX layer_index ON ogc_services_log USING btree (layer);

COMMIT;
