--
-- PostgreSQL database
--

BEGIN;

SET search_path TO ldapadmin,public,pg_catalog;

CREATE TABLE admin_attachments (
  id bigserial,
  content oid,
  mimetype character varying(255),
  name character varying(255),
  CONSTRAINT admin_attachments_pkey PRIMARY KEY (id)
);

CREATE TABLE email_template (
  id bigserial,
  content character varying(255),
  name character varying(255),
  CONSTRAINT email_template_pkey PRIMARY KEY (id)
);

INSERT INTO email_template (content, name) VALUES ('Bonjour et bienvenue', 'Hello');
INSERT INTO email_template (content, name) VALUES ('Votre compte a été supprimé', 'Deleted');

INSERT INTO admin_attachments (content, mimetype, name) VALUES (lo_import('/docker-entrypoint-initdb.d/license.txt'), 'text/plain', 'license.txt');
INSERT INTO admin_attachments (content, mimetype, name) VALUES (lo_import('/docker-entrypoint-initdb.d/logo.png'), 'image/png', 'logo.png');

COMMIT;
