--
-- PostgreSQL database
--

BEGIN;

CREATE SCHEMA ldapadmin;

SET search_path TO ldapadmin,public,pg_catalog;


CREATE TABLE user_token (
    uid character varying NOT NULL,
    token character varying,
    creation_date timestamp with time zone
);

ALTER TABLE ONLY user_token
    ADD CONSTRAINT uid PRIMARY KEY (uid);

CREATE UNIQUE INDEX token_idx ON user_token USING btree (token);

COMMIT;
