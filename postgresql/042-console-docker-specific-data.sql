--
-- Paths for the files are Dockerfile-specific, hence in this separate file.
-- Adapt to your use-case in the non-docker case.
--
SET search_path TO console,public,pg_catalog;

BEGIN;

INSERT INTO admin_attachments (content, mimetype, name) VALUES (lo_import('/docker-entrypoint-initdb.d/license.txt'), 'text/plain', 'license.txt');
INSERT INTO admin_attachments (content, mimetype, name) VALUES (lo_import('/docker-entrypoint-initdb.d/logo.png'), 'image/png', 'logo.png');

COMMIT;
