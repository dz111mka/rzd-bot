--liquibase formatted sql
--changeset Dmitriy:4

ALTER TABLE train_subscription ALTER COLUMN user_id TYPE bigint;