BEGIN;

create schema edit;
grant all on schema edit to "www-data";


create table edit.menhir (
   id serial primary key,
   name text not null,
   pieces smallint, 
   tilt smallint, 
   height smallint, 
   width smallint, 
   thickness smallint, 
   stone_id integer, 
   orientation smallint, 
   position_accuracy_id integer,
   displaced boolean,
   description text
);

select addgeometrycolumn('edit', 'menhir', 'geometry', 2154, 'POINT', 2);

create index menhir_geometry_idx on edit.menhir using GIST(geometry GIST_GEOMETRY_OPS);


grant all on edit.menhir to "www-data";
grant all on edit.menhir_id_seq to "www-data";





create table edit.parcelle (
  id serial primary key,
  name text not null,
  value float,
  description text
);

select addgeometrycolumn('edit', 'parcelle', 'geometry', 2154, 'POLYGON', 2);

create index parcelle_geometry_idx on edit.parcelle using GIST(geometry GIST_GEOMETRY_OPS);

grant all on edit.parcelle to "www-data";
grant all on edit.parcelle_id_seq to "www-data";




create table edit.chemin (
  id serial primary key,
  name text not null,
  width float,
  length integer,
  description text
);

select addgeometrycolumn('edit', 'chemin', 'geometry', 2154, 'LINESTRING', 2);

create index chemin_geometry_idx on edit.chemin using GIST(geometry GIST_GEOMETRY_OPS);

grant all on edit.chemin to "www-data";
grant all on edit.chemin_id_seq to "www-data";

COMMIT;