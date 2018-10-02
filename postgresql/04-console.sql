--
-- PostgreSQL database
--

BEGIN;

CREATE SCHEMA console;

SET search_path TO console,public,pg_catalog;

CREATE TABLE admin_attachments (
  id bigserial,
  content oid,
  mimetype character varying(255),
  name character varying(255),
  CONSTRAINT admin_attachments_pkey PRIMARY KEY (id)
);

CREATE TABLE email_template (
  id bigserial,
  content text,
  name character varying(255),
  CONSTRAINT email_template_pkey PRIMARY KEY (id)
);

CREATE TABLE admin_emails (
  id bigserial,
  body text,
  date timestamp without time zone,
  recipient character varying(255),
  sender text,
  subject character varying(255),
  CONSTRAINT admin_emails_pkey PRIMARY KEY (id)
);

CREATE TABLE delegations
(
  uid character varying(255) NOT NULL,
  orgs character varying[],
  roles character varying[],
  CONSTRAINT delegations_pkey PRIMARY KEY (uid)
);


CREATE TABLE user_token (
    uid character varying NOT NULL,
    token character varying,
    creation_date timestamp with time zone
);

ALTER TABLE ONLY user_token
    ADD CONSTRAINT uid PRIMARY KEY (uid);

CREATE UNIQUE INDEX token_idx ON user_token USING btree (token);

COMMIT;
