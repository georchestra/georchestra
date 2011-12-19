-- Table: ogc_services_log

-- DROP TABLE ogc_services_log;

CREATE TABLE ogc_services_log
(
  user_name character varying(255),
  date date,
  service character(5),
  layer character varying(255),
  id bigserial NOT NULL,
  CONSTRAINT primary_key PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ogc_services_log OWNER TO postgres;

