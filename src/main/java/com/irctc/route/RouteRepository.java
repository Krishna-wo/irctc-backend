package com.irctc.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // Get full schedule for a train ordered by sequence number (stop 1, 2, 3...)
    List<Route> findByTrainIdOrderBySequenceNumberAsc(Long trainId);

    // Route entity has uniqueConstraint on (train_id, sequence_number)
    // This check prevents duplicate sequence numbers before hitting DB
    boolean existsByTrainIdAndSequenceNumber(Long trainId, Integer sequenceNumber);

    // Route entity has uniqueConstraint on (train_id, station_id)
    // A station cannot appear twice on the same train's route
    boolean existsByTrainIdAndStationId(Long trainId, Long stationId);

    // Lookup a specific train+station stop — used for search in Day 6
    Optional<Route> findByTrainIdAndStationId(Long trainId, Long stationId);
}
