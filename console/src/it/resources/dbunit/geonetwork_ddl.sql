--
-- PostgreSQL database dump
--

-- Dumped from database version 11.2 (Debian 11.2-1.pgdg90+1)
-- Dumped by pg_dump version 11.2 (Ubuntu 11.2-1.pgdg18.04+1)

-- Created with the following command to satisfy the tables needed for integration testing:
-- pg_dump --schema-only --schema geonetwork \
-- -t geonetwork.users -t geonetwork.email -t geonetwork.metadata \
-- -t geonetwork.metadatacateg -t geonetwork.useraddress -t geonetwork.users \
-- --clean --if-exists --no-owner -h localhost -p 5432 -U georchestra georchestra > geonetwork_ddl.sql

-- Then added IF NOT EXISTS to all CREATE TABLE statements and made them single-line ones or dbunit fails running the script

CREATE TABLE IF NOT EXISTS geonetwork.email (user_id integer NOT NULL,email character varying(255));
CREATE TABLE IF NOT EXISTS geonetwork.metadata (id integer NOT NULL, data text NOT NULL, changedate character varying(30) NOT NULL, createdate character varying(30) NOT NULL, displayorder integer, doctype character varying(255), extra character varying(255), popularity integer NOT NULL, rating integer NOT NULL, root character varying(255), schemaid character varying(32) NOT NULL, title character varying(255), istemplate character(1) NOT NULL, isharvested character(1) NOT NULL, harvesturi character varying(512), harvestuuid character varying(255), groupowner integer, owner integer NOT NULL, source character varying(255) NOT NULL, uuid character varying(255) NOT NULL);
CREATE TABLE IF NOT EXISTS geonetwork.metadatacateg (metadataid integer NOT NULL, categoryid integer NOT NULL);
CREATE TABLE IF NOT EXISTS geonetwork.useraddress (userid integer NOT NULL, addressid integer NOT NULL);
CREATE TABLE IF NOT EXISTS geonetwork.users (id integer NOT NULL, isenabled character(1) DEFAULT 'y'::bpchar NOT NULL, kind character varying(16), lastlogindate character varying(255), name character varying(255), organisation character varying(255), profile integer NOT NULL, authtype character varying(32), nodeid character varying(255), password character varying(120) NOT NULL, security character varying(128), surname character varying(255), username character varying(255) NOT NULL);

