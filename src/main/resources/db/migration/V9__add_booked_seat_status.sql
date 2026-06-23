-- Add status column to booked_seats table
-- DEFAULT 'CONFIRMED' because all existing booked seats are confirmed
ALTER TABLE booked_seats
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED';