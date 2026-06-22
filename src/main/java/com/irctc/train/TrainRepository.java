package com.irctc.train;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    // Derived query → SELECT * FROM trains WHERE train_number = ?
    Optional<Train> findByTrainNumber(String trainNumber);

    // Duplicate check before insert
    boolean existsByTrainNumber(String trainNumber);
}
