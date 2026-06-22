package com.irctc.search;

import com.irctc.route.Route;
import com.irctc.train.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchRepository extends JpaRepository<Route, Long> {
    // We extend JpaRepository<Route, Long> because we query the Route table

    // THE core search query
    // Self-join on Route table to find trains where source comes before destination
    @Query("""
        SELECT DISTINCT r1.train FROM Route r1
        JOIN Route r2 ON r1.train = r2.train
        WHERE r1.station.code = :sourceCode
        AND r2.station.code = :destinationCode
        AND r1.sequenceNumber < r2.sequenceNumber
    """)
    List<Train> findTrainsBetweenStations(
            @Param("sourceCode") String sourceCode,
            @Param("destinationCode") String destinationCode
    );

    // After finding trains, we need the specific stop for each train
    // This gets us the source stop — to show departure time, platform etc.
    // We already have this in RouteRepository but SearchRepository
    // needs it too for the enriched response
    @Query("""
        SELECT r FROM Route r
        WHERE r.train.id = :trainId
        AND r.station.code = :stationCode
    """)
    Optional<Route> findStopByTrainAndStation(
            @Param("trainId") Long trainId,
            @Param("stationCode") String stationCode
    );
}