package com.irctc.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {

    boolean existsBySeatIdAndBooking_JourneyDateAndStatus(
            Long seatId, LocalDate journeyDate, String status);
    // Spring reads: existsBy + SeatId + And + Booking_JourneyDate
    // Booking_JourneyDate means: go into the Booking relation, get journeyDate field
    // This is a nested field derived query — Spring handles the JOIN automatically

    // Get all booked seats for a booking — for building response
    List<BookedSeat> findByBookingId(Long bookingId);
    // Add this to your existing BookedSeatRepository

    // Check if ANY active (non-cancelled) seats remain in a booking
// Used to decide if the whole booking should become CANCELLED
    boolean existsByBookingIdAndStatus(Long bookingId, String status);

}