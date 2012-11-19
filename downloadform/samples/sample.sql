begin;

create schema download;
grant all on schema download to "www-data";


create table download.log_table (
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
grant all on download.log_table to "www-data";
grant all on download.log_table_id_seq to "www-data";
create index log_table_username on download.log_table using btree (username);
create index log_table_sessionid on download.log_table using btree (sessionid);


-- GN: log MD id and filename (resource.get parameters)
create table download.geonetwork_log (
  metadata_id integer not null, -- this is not the UUID, but the local ID
  filename varchar(200) not null
) inherits (download.log_table);
grant all on download.geonetwork_log to "www-data";
create index geonetwork_log_id_fname on download.geonetwork_log using btree (metadata_id, filename);

-- extractorapp log table, which contains just the JSON spec for now (could be exploited later client side to display extracted stuff)
-- json_spec example : {"emails":["toto@titi.com"],"globalProperties":{"projection":"EPSG:4326","resolution":0.5,"rasterFormat":"geotiff","vectorFormat":"shp","bbox":{"srs":"EPSG:4326","value":[-2.2,42.6,1.9,46]}},"layers":[{"projection":null,"resolution":null,"format":null,"bbox":null,"owsUrl":"http://s.com/geoserver/wfs/WfsDispatcher?","owsType":"WFS","layerName":"pigma:cantons"},{"projection":null,"resolution":null,"format":null,"bbox":null,"owsUrl":"http://s.com/geoserver/pigma/wcs?","owsType":"WCS","layerName":"pigma:protected_layer_for_integration_testing"}]}
create table download.extractorapp_log (
  json_spec text not null
) inherits (download.log_table);
grant all on download.extractorapp_log to "www-data";
create index extractorapp_log_json_spec on download.extractorapp_log using btree (json_spec);


create table download.data_use (
  id serial primary key,
  name varchar(100)
);
grant select on download.data_use to "www-data";
grant select on download.data_use_id_seq to "www-data";

-- sample data:
insert into download.data_use (name) values ('Administratif et budgétaire');
insert into download.data_use (name) values ('Aménagement du Territoire et Gestion de l''Espace');
insert into download.data_use (name) values ('Communication');
insert into download.data_use (name) values ('Environnement');
insert into download.data_use (name) values ('Fond de Plan');
insert into download.data_use (name) values ('Foncier et Urbanisme');
insert into download.data_use (name) values ('Formation');
insert into download.data_use (name) values ('Gestion du Domaine Public');
insert into download.data_use (name) values ('Mise en valeur du Territoire (Tourisme)');
insert into download.data_use (name) values ('Risques Naturels et Technologiques');


create table download.logtable_datause (
  logtable_id integer not null,
  datause_id integer not null,
  primary key (logtable_id, datause_id)
);
grant all on download.logtable_datause to "www-data";
alter table download.logtable_datause add constraint fk_logtable_id foreign key (logtable_id) REFERENCES download.log_table (id) ;
alter table download.logtable_datause add constraint fk_datause_id foreign key (datause_id) REFERENCES download.data_use (id) ;

create table download.extractorapp_layers (
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
grant all on download.extractorapp_layers to "www-data";
grant all on download.extractorapp_layers_id_seq to "www-data";

create index extractorapp_layers_layer_name on download.extractorapp_layers using btree (layer_name);

commit;
