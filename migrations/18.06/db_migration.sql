BEGIN;

SET search_path TO public,pg_catalog;

ALTER SCHEMA ldapadmin RENAME TO console;

CREATE TABLE console.delegations
(
  uid character varying(255) NOT NULL,
  orgs character varying[],
  roles character varying[],
  CONSTRAINT delegations_pkey PRIMARY KEY (uid)
);

COMMIT;
