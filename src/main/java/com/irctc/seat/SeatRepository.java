package com.irctc.seat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Get all seats in a coach — for displaying coach layout
    List<Seat> findByCoachId(Long coachId);

    // Unique constraint check before insert
    // seats table has uniqueConstraint on (coach_id, seat_number)
    boolean existsByCoachIdAndSeatNumber(Long coachId, String seatNumber);

    // Count seats in a coach — used for validation
    long countByCoachId(Long coachId);
}