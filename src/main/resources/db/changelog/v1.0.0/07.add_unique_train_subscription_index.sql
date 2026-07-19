--liquibase formatted sql
--changeset Dmitriy:7

DELETE FROM train_subscription subscription
USING train_subscription duplicate
WHERE subscription.ctid < duplicate.ctid
  AND subscription.user_id = duplicate.user_id
  AND subscription.origin_station = duplicate.origin_station
  AND subscription.destination_station = duplicate.destination_station
  AND subscription.departure_date = duplicate.departure_date;

CREATE UNIQUE INDEX IF NOT EXISTS uq_train_subscription_route
    ON train_subscription (user_id, origin_station, destination_station, departure_date);
