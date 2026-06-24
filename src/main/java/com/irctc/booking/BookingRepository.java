package com.irctc.booking;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find booking by PNR — for PNR status check
    Optional<Booking> findByPnrNumber(String pnrNumber);

    // Get all bookings for a user — for "My Bookings" page
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    // Add these to your existing BookingRepository

    // SUM all totalFare across all CONFIRMED bookings
// @Query uses JPQL — Booking is entity name, totalFare is field name
    @Query("SELECT COALESCE(SUM(b.totalFare), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal calculateTotalRevenue();

    // Find which train has the most bookings
// GROUP BY train, ORDER BY count descending, take first result
    @Query("SELECT b.train.trainNumber FROM Booking b WHERE b.status = 'CONFIRMED' GROUP BY b.train ORDER BY COUNT(b) DESC LIMIT 1")
    String findMostBookedTrainNumber();


    // Find all PAYMENT_PENDING bookings created more than X minutes ago
// These are the ones we need to auto-cancel
    @Query("SELECT b FROM Booking b WHERE b.status = 'PAYMENT_PENDING' AND b.createdAt <= :cutoffTime")
    List<Booking> findExpiredPendingBookings(@Param("cutoffTime") LocalDateTime cutoffTime);
// cutoffTime = now minus 10 minutes
// Any PAYMENT_PENDING booking created before cutoffTime is expired
}