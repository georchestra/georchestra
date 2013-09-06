--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.9
-- Dumped by pg_dump version 9.1.9
-- Started on 2013-07-16 11:53:16 CEST

SET statement_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 1891 (class 1262 OID 11951)
-- Dependencies: 1890
-- Name: postgres; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON DATABASE postgres IS 'default administrative connection database';


--
-- TOC entry 162 (class 3079 OID 11677)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 1894 (class 0 OID 0)
-- Dependencies: 162
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 161 (class 1259 OID 16435)
-- Dependencies: 5
-- Name: user_token; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE user_token (
    uid character varying NOT NULL,
    token character varying,
    creation_date timestamp with time zone
);


ALTER TABLE public.user_token OWNER TO postgres;

--
-- TOC entry 1885 (class 2606 OID 16442)
-- Dependencies: 161 161 1887
-- Name: uid; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_token
    ADD CONSTRAINT uid PRIMARY KEY (uid);


--
-- TOC entry 1883 (class 1259 OID 16443)
-- Dependencies: 161 1887
-- Name: token_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX token_index ON user_token USING btree (token);


--
-- TOC entry 1893 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2013-07-16 11:53:16 CEST

--
-- PostgreSQL database dump complete
--

