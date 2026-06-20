CREATE TABLE stations (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          code VARCHAR(10) NOT NULL UNIQUE,
                          city VARCHAR(100) NOT NULL,
                          state VARCHAR(100) NOT NULL
);