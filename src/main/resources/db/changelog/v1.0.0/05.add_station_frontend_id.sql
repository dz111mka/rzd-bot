--liquibase formatted sql
--changeset Dmitriy:5

ALTER TABLE station ADD COLUMN IF NOT EXISTS frontend_id VARCHAR(24);

UPDATE station
SET frontend_id = '5a323c29340c7441a0a556bb'
WHERE id = 2000000;

UPDATE station
SET frontend_id = '5a3244bc340c7441a0a556ca'
WHERE id = 2004000;

UPDATE station
SET frontend_id = '5a13ba81340c745ca1e7e9a5'
WHERE id = 2064788;

UPDATE station
SET frontend_id = '5a8aca65340c742578d36789'
WHERE id = 2100170;
