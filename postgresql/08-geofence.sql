BEGIN;

--
-- Name: geofence; Type: SCHEMA; Schema: -; Owner: georchestra
--

CREATE SCHEMA geofence;


SET search_path = geofence, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: gf_adminrule; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_adminrule (
    id bigint NOT NULL,
    grant_type character varying(255) NOT NULL,
    ip_high bigint,
    ip_low bigint,
    ip_size integer,
    priority bigint NOT NULL,
    rolename character varying(255),
    username character varying(255),
    workspace character varying(255),
    instance_id bigint
);


ALTER TABLE gf_adminrule OWNER TO georchestra;

--
-- Name: gf_gfuser; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_gfuser (
    id bigint NOT NULL,
    datecreation timestamp without time zone,
    emailaddress character varying(255),
    enabled boolean NOT NULL,
    extid character varying(255),
    fullname character varying(255),
    name character varying(255) NOT NULL,
    password character varying(255)
);


ALTER TABLE gf_gfuser OWNER TO georchestra;

--
-- Name: gf_gsinstance; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_gsinstance (
    id bigint NOT NULL,
    baseurl character varying(255) NOT NULL,
    datecreation timestamp without time zone,
    description character varying(255),
    name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE gf_gsinstance OWNER TO georchestra;

--
-- Name: gf_gsuser; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_gsuser (
    id bigint NOT NULL,
    admin boolean NOT NULL,
    datecreation timestamp without time zone,
    emailaddress character varying(255),
    enabled boolean NOT NULL,
    extid character varying(255),
    fullname character varying(255),
    name character varying(255) NOT NULL,
    password character varying(255)
);


ALTER TABLE gf_gsuser OWNER TO georchestra;

--
-- Name: gf_layer_attributes; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_layer_attributes (
    details_id bigint NOT NULL,
    access_type character varying(255),
    data_type character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE gf_layer_attributes OWNER TO georchestra;

--
-- Name: gf_layer_custom_props; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_layer_custom_props (
    details_id bigint NOT NULL,
    propvalue character varying(255),
    propkey character varying(255) NOT NULL
);


ALTER TABLE gf_layer_custom_props OWNER TO georchestra;

--
-- Name: gf_layer_details; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_layer_details (
    id bigint NOT NULL,
    area public.geometry,
    catalog_mode character varying(255),
    cqlfilterread character varying(4000),
    cqlfilterwrite character varying(4000),
    defaultstyle character varying(255),
    type character varying(255),
    rule_id bigint NOT NULL
);


ALTER TABLE gf_layer_details OWNER TO georchestra;

--
-- Name: gf_layer_styles; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_layer_styles (
    details_id bigint NOT NULL,
    stylename character varying(255)
);


ALTER TABLE gf_layer_styles OWNER TO georchestra;

--
-- Name: gf_rule; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_rule (
    id bigint NOT NULL,
    grant_type character varying(255) NOT NULL,
    ip_high bigint,
    ip_low bigint,
    ip_size integer,
    layer character varying(255),
    priority bigint NOT NULL,
    request character varying(255),
    rolename character varying(255),
    service character varying(255),
    username character varying(255),
    workspace character varying(255),
    instance_id bigint
);


ALTER TABLE gf_rule OWNER TO georchestra;

--
-- Name: gf_rule_limits; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_rule_limits (
    id bigint NOT NULL,
    area public.geometry,
    catalog_mode character varying(255),
    rule_id bigint NOT NULL
);


ALTER TABLE gf_rule_limits OWNER TO georchestra;

--
-- Name: gf_user_usergroups; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_user_usergroups (
    user_id bigint NOT NULL,
    group_id bigint NOT NULL
);


ALTER TABLE gf_user_usergroups OWNER TO georchestra;

--
-- Name: gf_usergroup; Type: TABLE; Schema: geofence; Owner: georchestra
--

CREATE TABLE gf_usergroup (
    id bigint NOT NULL,
    datecreation timestamp without time zone,
    enabled boolean NOT NULL,
    extid character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE gf_usergroup OWNER TO georchestra;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: geofence; Owner: georchestra
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hibernate_sequence OWNER TO georchestra;


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: geofence; Owner: georchestra
--

SELECT pg_catalog.setval('hibernate_sequence', 1, false);


--
-- Name: gf_adminrule gf_adminrule_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_adminrule
    ADD CONSTRAINT gf_adminrule_pkey PRIMARY KEY (id);


--
-- Name: gf_adminrule gf_adminrule_username_rolename_instance_id_workspace_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_adminrule
    ADD CONSTRAINT gf_adminrule_username_rolename_instance_id_workspace_key UNIQUE (username, rolename, instance_id, workspace);


--
-- Name: gf_gfuser gf_gfuser_extid_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_gfuser
    ADD CONSTRAINT gf_gfuser_extid_key UNIQUE (extid);


--
-- Name: gf_gfuser gf_gfuser_name_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_gfuser
    ADD CONSTRAINT gf_gfuser_name_key UNIQUE (name);


--
-- Name: gf_gfuser gf_gfuser_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_gfuser
    ADD CONSTRAINT gf_gfuser_pkey PRIMARY KEY (id);


--
-- Name: gf_gsinstance gf_gsinstance_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_gsinstance
    ADD CONSTRAINT gf_gsinstance_pkey PRIMARY KEY (id);


--
-- Name: gf_gsuser gf_gsuser_extid_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_gsuser
    ADD CONSTRAINT gf_gsuser_extid_key UNIQUE (extid);


--
-- Name: gf_gsuser gf_gsuser_name_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_gsuser
    ADD CONSTRAINT gf_gsuser_name_key UNIQUE (name);


--
-- Name: gf_gsuser gf_gsuser_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_gsuser
    ADD CONSTRAINT gf_gsuser_pkey PRIMARY KEY (id);


--
-- Name: gf_layer_attributes gf_layer_attributes_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_layer_attributes
    ADD CONSTRAINT gf_layer_attributes_pkey PRIMARY KEY (details_id, name);


--
-- Name: gf_layer_custom_props gf_layer_custom_props_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_layer_custom_props
    ADD CONSTRAINT gf_layer_custom_props_pkey PRIMARY KEY (details_id, propkey);


--
-- Name: gf_layer_details gf_layer_details_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_layer_details
    ADD CONSTRAINT gf_layer_details_pkey PRIMARY KEY (id);


--
-- Name: gf_layer_details gf_layer_details_rule_id_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_layer_details
    ADD CONSTRAINT gf_layer_details_rule_id_key UNIQUE (rule_id);


--
-- Name: gf_rule_limits gf_rule_limits_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_rule_limits
    ADD CONSTRAINT gf_rule_limits_pkey PRIMARY KEY (id);


--
-- Name: gf_rule_limits gf_rule_limits_rule_id_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_rule_limits
    ADD CONSTRAINT gf_rule_limits_rule_id_key UNIQUE (rule_id);


--
-- Name: gf_rule gf_rule_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_rule
    ADD CONSTRAINT gf_rule_pkey PRIMARY KEY (id);


--
-- Name: gf_rule gf_rule_username_rolename_instance_id_service_request_works_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_rule
    ADD CONSTRAINT gf_rule_username_rolename_instance_id_service_request_works_key UNIQUE (username, rolename, instance_id, service, request, workspace, layer);


--
-- Name: gf_user_usergroups gf_user_usergroups_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_user_usergroups
    ADD CONSTRAINT gf_user_usergroups_pkey PRIMARY KEY (user_id, group_id);


--
-- Name: gf_usergroup gf_usergroup_extid_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_usergroup
    ADD CONSTRAINT gf_usergroup_extid_key UNIQUE (extid);


--
-- Name: gf_usergroup gf_usergroup_name_key; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_usergroup
    ADD CONSTRAINT gf_usergroup_name_key UNIQUE (name);


--
-- Name: gf_usergroup gf_usergroup_pkey; Type: CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_usergroup
    ADD CONSTRAINT gf_usergroup_pkey PRIMARY KEY (id);


--
-- Name: idx_adminrule_priority; Type: INDEX; Schema: geofence; Owner: georchestra
--

CREATE INDEX idx_adminrule_priority ON gf_adminrule USING btree (priority);


--
-- Name: idx_adminrule_workspace; Type: INDEX; Schema: geofence; Owner: georchestra
--

CREATE INDEX idx_adminrule_workspace ON gf_adminrule USING btree (workspace);


--
-- Name: idx_gsuser_name; Type: INDEX; Schema: geofence; Owner: georchestra
--

CREATE INDEX idx_gsuser_name ON gf_gsuser USING btree (name);


--
-- Name: idx_rule_layer; Type: INDEX; Schema: geofence; Owner: georchestra
--

CREATE INDEX idx_rule_layer ON gf_rule USING btree (layer);


--
-- Name: idx_rule_priority; Type: INDEX; Schema: geofence; Owner: georchestra
--

CREATE INDEX idx_rule_priority ON gf_rule USING btree (priority);


--
-- Name: idx_rule_request; Type: INDEX; Schema: geofence; Owner: georchestra
--

CREATE INDEX idx_rule_request ON gf_rule USING btree (request);


--
-- Name: idx_rule_service; Type: INDEX; Schema: geofence; Owner: georchestra
--

CREATE INDEX idx_rule_service ON gf_rule USING btree (service);


--
-- Name: idx_rule_workspace; Type: INDEX; Schema: geofence; Owner: georchestra
--

CREATE INDEX idx_rule_workspace ON gf_rule USING btree (workspace);


--
-- Name: gf_adminrule fk_adminrule_instance; Type: FK CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_adminrule
    ADD CONSTRAINT fk_adminrule_instance FOREIGN KEY (instance_id) REFERENCES gf_gsinstance(id);


--
-- Name: gf_layer_attributes fk_attribute_layer; Type: FK CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_layer_attributes
    ADD CONSTRAINT fk_attribute_layer FOREIGN KEY (details_id) REFERENCES gf_layer_details(id);


--
-- Name: gf_layer_details fk_details_rule; Type: FK CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_layer_details
    ADD CONSTRAINT fk_details_rule FOREIGN KEY (rule_id) REFERENCES gf_rule(id);


--
-- Name: gf_rule_limits fk_limits_rule; Type: FK CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_rule_limits
    ADD CONSTRAINT fk_limits_rule FOREIGN KEY (rule_id) REFERENCES gf_rule(id);


--
-- Name: gf_rule fk_rule_instance; Type: FK CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_rule
    ADD CONSTRAINT fk_rule_instance FOREIGN KEY (instance_id) REFERENCES gf_gsinstance(id);


--
-- Name: gf_layer_styles fk_styles_layer; Type: FK CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_layer_styles
    ADD CONSTRAINT fk_styles_layer FOREIGN KEY (details_id) REFERENCES gf_layer_details(id);


--
-- Name: gf_user_usergroups fk_uug_group; Type: FK CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_user_usergroups
    ADD CONSTRAINT fk_uug_group FOREIGN KEY (group_id) REFERENCES gf_usergroup(id);


--
-- Name: gf_user_usergroups fk_uug_user; Type: FK CONSTRAINT; Schema: geofence; Owner: georchestra
--

ALTER TABLE ONLY gf_user_usergroups
    ADD CONSTRAINT fk_uug_user FOREIGN KEY (user_id) REFERENCES gf_gsuser(id);


--
-- PostgreSQL database dump complete
--

COMMIT;
