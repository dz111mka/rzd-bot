--liquibase formatted sql
--changeset Dmitriy:6

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_station_name_trgm
    ON station
    USING gin (lower(name) gin_trgm_ops);
