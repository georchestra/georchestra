-- Table: ogc_services_log

-- DROP TABLE ogc_services_log;

CREATE TABLE ogc_services_log
(
  user_name character varying(255),
  date date,
  service character varying(5),
  layer character varying(255),
  id bigserial NOT NULL,
  request character varying(20),
  org character varying(255),
  CONSTRAINT primary_key PRIMARY KEY (id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ogc_services_log
  OWNER TO "www-data";

CREATE INDEX CONCURRENTLY user_name_index ON ogc_services_log (user_name);
CREATE INDEX CONCURRENTLY date_index ON ogc_services_log (date);
CREATE INDEX CONCURRENTLY service_index ON ogc_services_log (service);
CREATE INDEX CONCURRENTLY layer_index ON ogc_services_log (layer);