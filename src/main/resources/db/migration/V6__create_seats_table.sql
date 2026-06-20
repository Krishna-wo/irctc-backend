CREATE TABLE seats (
                       id BIGSERIAL PRIMARY KEY,
                       coach_id BIGINT NOT NULL REFERENCES coaches(id),
                       seat_number VARCHAR(10) NOT NULL,
                       seat_type VARCHAR(20) NOT NULL,
                       UNIQUE(coach_id, seat_number)
);
