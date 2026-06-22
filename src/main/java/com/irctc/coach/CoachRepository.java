package com.irctc.coach;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {

    // Get all coaches for a train
    // Used when displaying train composition
    List<Coach> findByTrainId(Long trainId);

    // Unique constraint check before insert
    // coaches table has uniqueConstraint on (train_id, coach_number)
    boolean existsByTrainIdAndCoachNumber(Long trainId, String coachNumber);
}