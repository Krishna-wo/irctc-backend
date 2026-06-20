CREATE TABLE trains (
                        id BIGSERIAL PRIMARY KEY,
                        train_number VARCHAR(10) NOT NULL UNIQUE,
                        name VARCHAR(150) NOT NULL,
                        type VARCHAR(30) NOT NULL,
                        total_distance_km INTEGER NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);