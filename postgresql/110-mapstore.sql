CREATE SCHEMA geostore;

GRANT USAGE ON SCHEMA geostore TO georchestra ;
GRANT ALL ON SCHEMA geostore TO georchestra ;

create table geostore.gs_attribute (
    id int8 not null,
    attribute_date timestamp,
    name varchar(255) not null,
    attribute_number float8,
    attribute_text varchar(255),
    attribute_type varchar(255) not null,
    resource_id int8 not null,
    primary key (id),
    unique (name, resource_id)
);

create table geostore.gs_category (
    id int8 not null,
    name varchar(255) not null,
    primary key (id),
    unique (name)
);

create table geostore.gs_resource (
    id int8 not null,
    creation timestamp not null,
    description varchar(10000),
    lastUpdate timestamp,
    metadata varchar(30000),
    name varchar(255) not null,
    category_id int8 not null,
    primary key (id),
    unique (name)
);

create table geostore.gs_security (
    id int8 not null,
    canRead bool not null,
    canWrite bool not null,
    group_id int8,
    resource_id int8,
    user_id int8,
    username varchar(255),
    groupname varchar(255),
    primary key (id),
    unique (user_id, resource_id),
    unique (resource_id, group_id)
);

create table geostore.gs_stored_data (
    id int8 not null,
    stored_data varchar(10000000) not null,
    resource_id int8 not null,
    primary key (id),
    unique (resource_id)
);

create table geostore.gs_user (
    id int8 not null,
    name varchar(255) not null,
    user_password varchar(255),
    user_role varchar(255) not null,
    group_id int8,
    enabled char(1) NOT NULL DEFAULT 'Y',
    primary key (id),
    unique (name)
);

create table geostore.gs_user_attribute (
    id int8 not null,
    name varchar(255) not null,
    string varchar(255),
    user_id int8 not null,
    primary key (id),
    unique (name, user_id)
);

create table geostore.gs_usergroup (
    id int8 not null,
    groupName varchar(255) not null,
    description varchar(255),
    enabled char(1) NOT NULL DEFAULT 'Y',
    primary key (id),
    unique (groupName)
);

create table geostore.gs_usergroup_members (
    user_id int8 not null, 
    group_id int8 not null, 
    primary key (user_id, group_id)
);

alter table geostore.gs_usergroup_members 
    add constraint FKFDE460DB62224F72 
    foreign key (user_id) 
    references geostore.gs_user;
    
alter table geostore.gs_usergroup_members 
    add constraint FKFDE460DB9EC981B7 
    foreign key (group_id) 
    references geostore.gs_usergroup;

create index idx_attribute_name on geostore.gs_attribute (name);

create index idx_attribute_resource on geostore.gs_attribute (resource_id);

create index idx_attribute_text on geostore.gs_attribute (attribute_text);

create index idx_attribute_type on geostore.gs_attribute (attribute_type);

create index idx_attribute_date on geostore.gs_attribute (attribute_date);

create index idx_attribute_number on geostore.gs_attribute (attribute_number);

alter table geostore.gs_attribute 
    add constraint fk_attribute_resource 
    foreign key (resource_id) 
    references geostore.gs_resource;

create index idx_category_type on geostore.gs_category (name);

create index idx_resource_name on geostore.gs_resource (name);

create index idx_resource_description on geostore.gs_resource (description);

create index idx_resource_metadata on geostore.gs_resource (metadata);

create index idx_resource_update on geostore.gs_resource (lastUpdate);

create index idx_resource_creation on geostore.gs_resource (creation);

create index idx_resource_category on geostore.gs_resource (category_id);

alter table geostore.gs_resource 
    add constraint fk_resource_category 
    foreign key (category_id) 
    references geostore.gs_category;

create index idx_security_resource on geostore.gs_security (resource_id);

create index idx_security_user on geostore.gs_security (user_id);

create index idx_security_group on geostore.gs_security (group_id);

create index idx_security_write on geostore.gs_security (canWrite);

create index idx_security_read on geostore.gs_security (canRead);

create index idx_security_username on geostore.gs_security (username);

create index idx_security_groupname on geostore.gs_security (groupname);

alter table geostore.gs_security 
    add constraint fk_security_user 
    foreign key (user_id) 
    references geostore.gs_user;

alter table geostore.gs_security 
    add constraint fk_security_group 
    foreign key (group_id) 
    references geostore.gs_usergroup;

alter table geostore.gs_security 
    add constraint fk_security_resource 
    foreign key (resource_id) 
    references geostore.gs_resource;

alter table geostore.gs_stored_data 
    add constraint fk_data_resource 
    foreign key (resource_id) 
    references geostore.gs_resource;

create index idx_user_group on geostore.gs_user (group_id);

create index idx_user_password on geostore.gs_user (user_password);

create index idx_user_name on geostore.gs_user (name);

create index idx_user_role on geostore.gs_user (user_role);

alter table geostore.gs_user 
    add constraint fk_user_ugroup 
    foreign key (group_id) 
    references geostore.gs_usergroup;

create index idx_user_attribute_name on geostore.gs_user_attribute (name);

create index idx_user_attribute_text on geostore.gs_user_attribute (string);

create index idx_attribute_user on geostore.gs_user_attribute (user_id);

alter table geostore.gs_user_attribute 
    add constraint fk_uattrib_user 
    foreign key (user_id) 
    references geostore.gs_user;

create index idx_usergroup_name on geostore.gs_usergroup (groupName);

create sequence geostore.hibernate_sequence;

insert into geostore.gs_category (id, name) values (1, 'MAP');
insert into geostore.gs_category (id, name) values (2, 'THUMBNAIL');
insert into geostore.gs_category (id, name) values (3, 'DETAILS');
insert into geostore.gs_category (id, name) values (4, 'DASHBOARD');
