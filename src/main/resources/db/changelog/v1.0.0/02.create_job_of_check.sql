--liquibase formatted sql
--changeset Dmitriy:2

CREATE TABLE IF NOT EXISTS job_of_check (
    id                   UUID            PRIMARY KEY,       -- Идентификатор
    user_id              int             NOT NULL,          -- Идентификатор пользователя
    origin_station       VARCHAR(128)    NOT NULL,          -- Станция отправления
    destination_station  VARCHAR(128)    NOT NULL,          -- Станция прибытия
    departure_date       DATE            NOT NULL,          -- Дата
    hashcode             int                                -- Хэшкод
);
