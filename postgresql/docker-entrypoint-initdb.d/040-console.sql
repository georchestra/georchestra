--
-- PostgreSQL database
--

BEGIN;

CREATE SCHEMA console;

SET search_path TO console,public,pg_catalog;


CREATE TABLE user_token (
    uid character varying NOT NULL,
    token character varying,
    creation_date timestamp with time zone
);

ALTER TABLE ONLY user_token
    ADD CONSTRAINT uid PRIMARY KEY (uid);

CREATE UNIQUE INDEX token_idx ON user_token USING btree (token);

COMMIT;
