package com.irctc.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find booking by PNR — for PNR status check
    Optional<Booking> findByPnrNumber(String pnrNumber);

    // Get all bookings for a user — for "My Bookings" page
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}