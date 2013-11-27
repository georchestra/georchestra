begin;

create schema geodoc;
grant all on schema geodoc to "www-data";

create table geodoc.geodocs (
  id bigserial primary key, -- 1 to 9223372036854775807 (~ 1E19)
  username varchar(200), -- can be NULL (eg: anonymous user)
  standard varchar(3), -- eg: CSV, KML, SLD, WMC
  standard_version varchar(5), -- eg: 1.0.0 or 2.2
  raw_file_content text, -- file content
  file_hash varchar(32), -- md5sum
  created_at timestamp without time zone default NOW(), -- creation date
  last_access timestamp without time zone, -- last access date
  access_count integer default 0 -- access count, defaults to 0
);

grant all on geodoc.geodocs to "www-data";
grant all on geodoc.geodocs_id_seq to "www-data";

create index geodocs_file_hash on geodoc.geodocs using btree (file_hash);
create index geodocs_username on geodoc.geodocs using btree (username);
create index geodocs_standard on geodoc.geodocs using btree (standard, standard_version);
create index geodocs_created_at on geodoc.geodocs using btree (created_at);
create index geodocs_last_access on geodoc.geodocs using btree (last_access);
create index geodocs_access_count on geodoc.geodocs using btree (access_count);

commit;