begin;

create schema mapfishapp;

create table mapfishapp.geodocs (
  id bigserial primary key, -- 1 to 9223372036854775807 (~ 1E19)
  username varchar(200), -- can be NULL (eg: anonymous user)
  standard varchar(3) not null, -- eg: CSV, KML, SLD, WMC, GML
  raw_file_content text not null, -- file content
  file_hash varchar(32) unique not null, -- md5sum
  created_at timestamp without time zone default NOW(), -- creation date
  last_access timestamp without time zone, -- last access date
  access_count integer default 0 -- access count, defaults to 0
);

create index geodocs_file_hash on mapfishapp.geodocs using btree (file_hash);
create index geodocs_username on mapfishapp.geodocs using btree (username);
create index geodocs_standard on mapfishapp.geodocs using btree (standard);
create index geodocs_created_at on mapfishapp.geodocs using btree (created_at);
create index geodocs_last_access on mapfishapp.geodocs using btree (last_access);
create index geodocs_access_count on mapfishapp.geodocs using btree (access_count);

commit;
