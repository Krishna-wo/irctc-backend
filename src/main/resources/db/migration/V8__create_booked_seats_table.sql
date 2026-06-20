CREATE TABLE booked_seats (
                              id BIGSERIAL PRIMARY KEY,
                              booking_id BIGINT NOT NULL REFERENCES bookings(id),
                              seat_id BIGINT NOT NULL REFERENCES seats(id),
                              passenger_name VARCHAR(100) NOT NULL,
                              passenger_age INTEGER NOT NULL,
                              passenger_gender VARCHAR(10) NOT NULL,
                              fare DECIMAL(10,2) NOT NULL
);