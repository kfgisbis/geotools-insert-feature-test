set client_encoding to 'UTF8';

create schema if not exists bis;

create extension if not exists postgis;

CREATE TABLE bis.test (
	id uuid NOT NULL,
	geometry public.geometry(geometry, 4326) NULL,
	CONSTRAINT test_pkey PRIMARY KEY (id)
);