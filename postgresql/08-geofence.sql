BEGIN;

CREATE SCHEMA geofence;

SET search_path TO geofence;

CREATE TABLE gf_gfuser (
  id int8 not null,
  dateCreation timestamp,
  emailAddress varchar(255),
  enabled bool not null,
  extId varchar(255) unique,
  fullName varchar(255),
  name varchar(255) not null unique,
  password varchar(255),
  PRIMARY KEY (id)
);

CREATE TABLE gf_gsinstance (
  id int8 not null,
  baseURL varchar(255) not null,
  dateCreation timestamp,
  description varchar(255),
  name varchar(255) not null,
  password varchar(255) not null,
  username varchar(255) not null,
  PRIMARY KEY (id)
);

CREATE TABLE gf_gsuser (
  id int8 not null,
  admin bool not null,
  dateCreation timestamp,
  emailAddress varchar(255),
  enabled bool not null,
  extId varchar(255) UNIQUE,
  fullName varchar(255),
  name varchar(255) not null UNIQUE,
  password varchar(255),
  PRIMARY KEY (id)
);

CREATE TABLE gf_layer_attributes (
  details_id int8 not null,
  access_type varchar(255),
  data_type varchar(255),
  name varchar(255) not null,
  PRIMARY KEY (details_id, name),
  UNIQUE (details_id, name)
);

CREATE TABLE gf_layer_custom_props (
  details_id int8 not null,
  propvalue varchar(255),
  propkey varchar(255),
  PRIMARY KEY (details_id, propkey)
);

CREATE TABLE gf_layer_details (
  id int8 not null,
  area public.geometry,
  cqlFilterRead varchar(4000),
  cqlFilterWrite varchar(4000),
  defaultStyle varchar(255),
  areaMetadataField varchar(255),
  type varchar(255),
  rule_id int8 not null,
  catalog_mode character varying(255),
  PRIMARY KEY (id),
  UNIQUE (rule_id)
);

CREATE TABLE gf_layer_styles (
  details_id int8 not null,
  styleName varchar(255)
);

CREATE TABLE gf_rule (
  id int8 not null,
  grant_type varchar(255) not null,
  layer varchar(255),
  priority int8 not null,
  request varchar(255),
  service varchar(255),
  workspace varchar(255),
  gsuser_id bigint,
  instance_id bigint,
  userGroup_id bigint,
  ip_high bigint,
  ip_low bigint,
  ip_size integer,
  rolename character varying(255),
  username character varying(255),
  PRIMARY KEY (id),
  UNIQUE (gsuser_id, userGroup_id, instance_id, service, request, workspace, layer)
);

CREATE TABLE gf_rule_limits (
  id int8 not null,
  area public.geometry,
  rule_id int8 not null,
  catalog_mode character varying(255),
  PRIMARY KEY (id),
  UNIQUE (rule_id)
);

CREATE TABLE gf_user_usergroups (
  user_id int8 not null,
  group_id int8 not null,
  PRIMARY KEY (user_id, group_id)
);

CREATE TABLE gf_usergroup (
  id int8 not null,
  dateCreation timestamp,
  enabled bool not null,
  extId varchar(255) UNIQUE,
  name varchar(255) not null UNIQUE,
  PRIMARY KEY (id)
);

CREATE TABLE gf_adminrule (
  id bigserial NOT NULL,
  grant_type character varying(255) NOT NULL,
  ip_high bigint,
  ip_low bigint,
  ip_size integer,
  priority bigint NOT NULL,
  rolename character varying(255),
  username character varying(255),
  workspace character varying(255),
  instance_id bigint,
  CONSTRAINT gf_adminrule_pkey PRIMARY KEY (id),
  CONSTRAINT gf_adminrule_username_rolename_instance_id_workspace_key UNIQUE (username, rolename, instance_id, workspace)
);

CREATE index idx_gsuser_name on gf_gsuser (name);

ALTER TABLE gf_layer_attributes
ADD CONSTRAINT fk_attribute_layer
FOREIGN KEY (details_id)
REFERENCES gf_layer_details;

ALTER TABLE gf_layer_custom_props
ADD CONSTRAINT fk_custom_layer
FOREIGN KEY (details_id)
REFERENCES gf_layer_details;

ALTER TABLE gf_layer_details
ADD CONSTRAINT fk_details_rule
FOREIGN KEY (rule_id)
REFERENCES gf_rule;

ALTER TABLE gf_layer_styles
ADD CONSTRAINT fk_styles_layer
FOREIGN KEY (details_id)
REFERENCES gf_layer_details;

ALTER TABLE gf_adminrule
ADD CONSTRAINT fk_adminrule_instance FOREIGN KEY (instance_id)
REFERENCES geofence.gf_gsinstance (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE index idx_rule_request on gf_rule (request);

CREATE index idx_rule_layer on gf_rule (layer);

CREATE index idx_rule_service on gf_rule (service);

CREATE index idx_rule_workspace on gf_rule (workspace);

CREATE index idx_rule_priority on gf_rule (priority);

ALTER TABLE gf_rule
ADD CONSTRAINT fk_rule_user
FOREIGN KEY (gsuser_id)
REFERENCES gf_gsuser;

ALTER TABLE gf_rule
ADD CONSTRAINT fk_rule_usergroup
FOREIGN KEY (userGroup_id)
REFERENCES gf_usergroup;

ALTER TABLE gf_rule
ADD CONSTRAINT fk_rule_instance
FOREIGN KEY (instance_id)
REFERENCES gf_gsinstance;

ALTER TABLE gf_rule_limits
ADD CONSTRAINT fk_limits_rule
FOREIGN KEY (rule_id)
REFERENCES gf_rule;

ALTER TABLE gf_user_usergroups
ADD CONSTRAINT fk_uug_user
FOREIGN KEY (user_id)
REFERENCES gf_gsuser;

ALTER TABLE gf_user_usergroups
ADD CONSTRAINT fk_uug_group
FOREIGN KEY (group_id)
REFERENCES gf_usergroup;

CREATE sequence hibernate_sequence;

COMMIT;

