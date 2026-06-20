CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL REFERENCES users(id),
                          train_id BIGINT NOT NULL REFERENCES trains(id),
                          source_station_id BIGINT NOT NULL REFERENCES stations(id),
                          destination_station_id BIGINT NOT NULL REFERENCES stations(id),
                          journey_date DATE NOT NULL,
                          status VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
                          total_fare DECIMAL(10,2) NOT NULL,
                          pnr_number VARCHAR(20) NOT NULL UNIQUE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);