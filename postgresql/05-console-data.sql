--
-- PostgreSQL database
--

BEGIN;

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

INSERT INTO email_template (content, name) VALUES ('Bonjour et bienvenue', 'Hello');
INSERT INTO email_template (content, name) VALUES ('Votre compte a été supprimé', 'Deleted');

INSERT INTO admin_attachments (content, mimetype, name) VALUES (lo_import('/docker-entrypoint-initdb.d/license.txt'), 'text/plain', 'license.txt');
INSERT INTO admin_attachments (content, mimetype, name) VALUES (lo_import('/docker-entrypoint-initdb.d/logo.png'), 'image/png', 'logo.png');

INSERT INTO admin_emails (body, date, recipient, sender, subject) VALUES ( 'Votre compte a été suprimé', '2016-05-18 09:31:47.928', 'testadmin', 'testadmin', 'Deleted');

INSERT INTO delegations (uid, orgs, roles) VALUES ('testeditor', '{psc, c2c}', '{EXTRACTORAPP, GN_EDITOR}');
INSERT INTO delegations (uid, orgs, roles) VALUES ('testuser', '{psc, cra}', '{GN_REVIEWER, GN_EDITOR}');

COMMIT;
