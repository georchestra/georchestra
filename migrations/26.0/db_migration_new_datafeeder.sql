BEGIN;

--
-- IMPORTANT: DO NOT EDIT THIS FILE.
-- Database edits should now be made with Alembic migrations
--

CREATE SCHEMA IF NOT EXISTS datafeeder;

CREATE SCHEMA IF NOT EXISTS data;

CREATE SCHEMA IF NOT EXISTS staging;

-- Enable pgcrypto extension for encrypted storage of credentials
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SEQUENCE IF NOT EXISTS datafeeder.hibernate_sequence;

GRANT ALL ON datafeeder.hibernate_sequence TO georchestra;

GRANT ALL ON SCHEMA staging TO georchestra;

CREATE TYPE datafeeder.rule_type_enum AS ENUM(
    'DATA',
    'METADATA'
);

CREATE TYPE datafeeder.rule_value_enum AS ENUM(
    'READ',
    'WRITE'
);

CREATE TABLE IF NOT EXISTS datafeeder.integrity_link(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    data_id varchar(255) NULL,
    metadata_id varchar(255) NULL,
    integrity_title text NULL,
    integrity_owner varchar(255) NOT NULL,
    integrity_organization varchar(255) NOT NULL,
    integrity_transformation jsonb NULL,
    source_import_type varchar(10) NOT NULL,
    source_url text NULL,
    source_file_name varchar(255) NULL,
    source_file_type varchar(10) NULL,
    source_username text NULL,
    source_password_encrypted text NULL,
    staging_table_name varchar(63) NOT NULL,
    staging_retrieve_time interval NULL,
    final_table_name varchar(63) UNIQUE NULL,
    last_retrieval_timestamp timestamp NULL,
    schedule varchar(63) NULL,
    schedule_enabled boolean DEFAULT FALSE,
    gn_is_published boolean DEFAULT FALSE,
    gs_is_published boolean DEFAULT FALSE,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN datafeeder.integrity_link.staging_retrieve_time IS 'Estimated time taken to retrieve data into staging table. Used to define the minimum interval allowed between two schedules.';

COMMENT ON COLUMN datafeeder.integrity_link.last_retrieval_timestamp IS 'Timestamp of the last successful retrieval into the final table';

CREATE TABLE IF NOT EXISTS datafeeder.integrity_link_rules(
    id serial,
    integrity_link_id uuid REFERENCES datafeeder.integrity_link(id) ON DELETE CASCADE,
    rule_type datafeeder.rule_type_enum NOT NULL,
    rule_value datafeeder.rule_value_enum DEFAULT 'READ',
    group_or_role varchar(255) NOT NULL
);

ALTER TABLE datafeeder.integrity_link_rules
    ADD CONSTRAINT uq_integrity_link_rules_link_type_group
    UNIQUE (integrity_link_id, rule_type, group_or_role);


COMMIT;
