CREATE TABLE coaches (
                         id BIGSERIAL PRIMARY KEY,
                         train_id BIGINT NOT NULL REFERENCES trains(id),
                         coach_number VARCHAR(10) NOT NULL,
                         coach_type VARCHAR(20) NOT NULL,
                         total_seats INTEGER NOT NULL,
                         UNIQUE(train_id, coach_number)
);