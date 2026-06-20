package com.irctc.station;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// JpaRepository<Station, Long> gives us:
// save(), findById(), findAll(), deleteById(), existsById() — all for free
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    // Spring Data JPA reads this method name and writes the SQL for us:
    // SELECT * FROM stations WHERE code = ?
    // This is called "Derived Query" — very powerful, very clean
    Optional<Station> findByCode(String code);

    // We need this to check duplicates before saving
    // SELECT COUNT(*) > 0 FROM stations WHERE code = ?
    boolean existsByCode(String code);

    // Search by city — useful for the "find trains from Mumbai" feature later
    // SELECT * FROM stations WHERE LOWER(city) = LOWER(?)
    // Actually Spring generates: WHERE city = ? — we handle case in service
    java.util.List<Station> findByCityIgnoreCase(String city);
}