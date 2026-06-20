CREATE TABLE routes (
                        id BIGSERIAL PRIMARY KEY,
                        train_id BIGINT NOT NULL REFERENCES trains(id),
                        station_id BIGINT NOT NULL REFERENCES stations(id),
                        sequence_number INTEGER NOT NULL,
                        arrival_time TIME,
                        departure_time TIME,
                        distance_from_origin INTEGER NOT NULL DEFAULT 0,
                        UNIQUE(train_id, sequence_number),
                        UNIQUE(train_id, station_id)
);