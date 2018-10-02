--
-- PostgreSQL database
--

BEGIN;

SET search_path TO console,public,pg_catalog;

INSERT INTO email_template (content, name) VALUES ('Bonjour et bienvenue', 'Hello');
INSERT INTO email_template (content, name) VALUES ('Votre compte a été supprimé', 'Deleted');

INSERT INTO admin_emails (body, date, recipient, sender, subject) VALUES ( 'Votre compte a été suprimé', '2016-05-18 09:31:47.928', 'testadmin', 'testadmin', 'Deleted');

INSERT INTO delegations (uid, orgs, roles) VALUES ('testdelegatedadmin', '{psc, c2c}', '{EXTRACTORAPP, GN_EDITOR}');

COMMIT;
