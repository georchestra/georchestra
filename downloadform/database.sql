--
-- PostgreSQL database
--

begin;

create schema downloadform;

set search_path to downloadform,public,pg_catalog;

create table log_table (
  id serial primary key,
  username varchar(200), -- can be NULL (eg: anonymous user)
  sessionid varchar(32) not null, -- this is the security-proxy JSESSIONID
  first_name varchar(200) not null,
  second_name varchar(200) not null,
  company varchar(200) not null,
  email varchar(200) not null,
  phone varchar(100),
  requested_at timestamp without time zone default NOW(),
  comment text
);
create index log_table_username on log_table using btree (username);
create index log_table_sessionid on log_table using btree (sessionid);


-- GN: log MD id and filename (resource.get parameters)
create table geonetwork_log (
  metadata_id integer not null, -- this is not the UUID, but the local ID
  filename varchar(200) not null
) inherits (log_table);
create index geonetwork_log_id_fname on geonetwork_log using btree (metadata_id, filename);

-- extractorapp log table, which contains just the JSON spec for now (could be exploited later client side to display extracted stuff)
-- json_spec example : {"emails":["toto@titi.com"],"globalProperties":{"projection":"EPSG:4326","resolution":0.5,"rasterFormat":"geotiff","vectorFormat":"shp","bbox":{"srs":"EPSG:4326","value":[-2.2,42.6,1.9,46]}},"layers":[{"projection":null,"resolution":null,"format":null,"bbox":null,"owsUrl":"http://s.com/geoserver/wfs/WfsDispatcher?","owsType":"WFS","layerName":"pigma:cantons"},{"projection":null,"resolution":null,"format":null,"bbox":null,"owsUrl":"http://s.com/geoserver/pigma/wcs?","owsType":"WCS","layerName":"pigma:protected_layer_for_integration_testing"}]}
create table extractorapp_log (
  json_spec text not null
) inherits (log_table);
create index extractorapp_log_json_spec on extractorapp_log using btree (json_spec);


create table data_use (
  id serial primary key,
  name varchar(100)
);

-- sample data:
insert into data_use (name) values ('Administratif et budgétaire');
insert into data_use (name) values ('Aménagement du Territoire et Gestion de l''Espace');
insert into data_use (name) values ('Communication');
insert into data_use (name) values ('Environnement');
insert into data_use (name) values ('Fond de Plan');
insert into data_use (name) values ('Foncier et Urbanisme');
insert into data_use (name) values ('Formation');
insert into data_use (name) values ('Gestion du Domaine Public');
insert into data_use (name) values ('Mise en valeur du Territoire (Tourisme)');
insert into data_use (name) values ('Risques Naturels et Technologiques');


create table logtable_datause (
  logtable_id integer not null,
  datause_id integer not null,
  primary key (logtable_id, datause_id)
);
-- commented out because it generates an error:
--alter table logtable_datause add constraint fk_logtable_id foreign key (logtable_id) REFERENCES log_table (id) ;
--org.postgresql.util.PSQLException: ERROR: insert or update on table "logtable_datause" violates foreign key constraint "fk_logtable_id"
--Detail: Key (logtable_id)=(2) is not present in table "log_table".
--  at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2102)
alter table logtable_datause add constraint fk_datause_id foreign key (datause_id) REFERENCES data_use (id) ;

create table extractorapp_layers (
  id serial primary key,
  extractorapp_log_id integer NOT NULL,
  projection character varying(12),
  resolution double precision,
  format character varying(10),
  bbox_srs character varying(12),
  "left" double precision,
  bottom double precision,
  "right" double precision,
  top double precision,
  ows_url character varying(1024),
  ows_type character varying(3),
  layer_name text
);

create index extractorapp_layers_layer_name on extractorapp_layers using btree (layer_name);

commit;
