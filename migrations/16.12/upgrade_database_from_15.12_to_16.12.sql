CREATE SCHEMA atlas;

CREATE SCHEMA extractorapp;

CREATE TABLE extractorapp.extractor_log(
  id serial,
  creation_date timestamp without time zone NOT NULL DEFAULT NOW(),
  duration interval,
  username character varying NOT NULL,
  roles character varying[] NOT NULL,
  org character varying NOT NULL,
  request_id character varying NOT NULL,
  CONSTRAINT extractor_log_pk PRIMARY KEY (id)
);

CREATE TABLE extractorapp.extractor_layer_log(
  id serial,
  extractor_log_id integer NOT NULL,
  projection character varying NOT NULL,
  resolution integer,
  format character varying NOT NULL,
  bbox geometry(Polygon,4326) NOT NULL,
  owstype character varying NOT NULL,
  owsurl text NOT NULL,
  layer_name text NOT NULL,
  is_successful boolean,
  CONSTRAINT extractor_layer_log_pk PRIMARY KEY (id),
  CONSTRAINT extractor_layer_log_fk FOREIGN KEY (extractor_log_id)
    REFERENCES extractorapp.extractor_log (id)
);
