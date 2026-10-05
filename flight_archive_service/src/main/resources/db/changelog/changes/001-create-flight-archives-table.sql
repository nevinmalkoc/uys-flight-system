--liquibase formatted sql
--changeset nevin:001-create-flight-archives
CREATE TABLE IF NOT EXISTS flight_archives (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(50) NOT NULL,
    departure_station VARCHAR(10),
    arrival_station VARCHAR(10),
    departure_time TIMESTAMP,
    version INT,
    status VARCHAR(50),
    is_current BOOLEAN,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
