SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
--SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;


CREATE SCHEMA forum;



CREATE TABLE forum.question (
    id integer NOT NULL,
    author character varying(255),
    title character varying(255),
    content text
);



CREATE SEQUENCE forum.question_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE forum.question_id_seq OWNED BY forum.question.id;
