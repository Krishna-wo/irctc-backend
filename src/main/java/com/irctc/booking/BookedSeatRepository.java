package com.irctc.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {

    // THE most critical query in the system:
    // Is this specific seat already booked on this specific date?
    // This prevents double booking
    boolean existsBySeatIdAndBooking_JourneyDate(
            Long seatId, LocalDate journeyDate);
    // Spring reads: existsBy + SeatId + And + Booking_JourneyDate
    // Booking_JourneyDate means: go into the Booking relation, get journeyDate field
    // This is a nested field derived query — Spring handles the JOIN automatically

    // Get all booked seats for a booking — for building response
    List<BookedSeat> findByBookingId(Long bookingId);
}