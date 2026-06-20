package com.irctc.station;
import com.irctc.common.response.ApiResponse;
import com.irctc.station.dto.StationRequest;
import com.irctc.station.dto.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
// @Service tells Spring: this is a business logic bean, manage its lifecycle
public class StationService {

    private final StationRepository stationRepository;

    // Constructor injection — preferred over @Autowired field injection
    // Why? Because this makes the dependency explicit and testable
    // In unit tests, you can pass a mock repository via constructor
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    // @Transactional means: wrap this entire method in a DB transaction
    // If anything throws an exception, ALL changes are rolled back
    // This is critical for data consistency
    public ApiResponse<com.irctc.station.dto.StationResponse> addStation(StationRequest request) {

        // Business Rule 1: Station codes must be unique
        // "NDLS" cannot exist twice in the system
        // We check BEFORE trying to save to give a clean error message
        if (stationRepository.existsByCode(request.getCode().toUpperCase())) {
            // We use ApiResponse.error() — our wrapper from Day 4
            return ApiResponse.error("Station with code " + request.getCode() + " already exists");
        }

        // Build the entity from the request
        // We don't trust the client's formatting — we normalize here
        Station station = new Station();
        station.setName(request.getName().trim());
        // Always store codes in UPPERCASE — "ndls" and "NDLS" are the same station
        station.setCode(request.getCode().toUpperCase().trim());
        station.setCity(request.getCity().trim());
        station.setState(request.getState().trim());

        // Save to DB — JPA generates: INSERT INTO stations (name, code, city, state) VALUES (...)
        Station saved = stationRepository.save(station);

        // Return success with the response DTO
        return ApiResponse.success("Station added successfully", new StationResponse(saved));
    }

    // Read operation — no @Transactional needed for pure reads
    // Why? @Transactional adds overhead (transaction begin/commit)
    // For reads, we can use @Transactional(readOnly = true) for optimization
    @Transactional(readOnly = true)
    public ApiResponse<List<StationResponse>> getAllStations() {

        List<StationResponse> stations = stationRepository.findAll()
                .stream()
                // For each Station entity, create a StationResponse DTO
                .map(StationResponse::new)
                // Collect into a List
                .collect(Collectors.toList());

        return ApiResponse.success("Stations fetched successfully", stations);
    }

    @Transactional(readOnly = true)
    public ApiResponse<StationResponse> getStationByCode(String code) {

        // findByCode returns Optional<Station>
        // .orElse(null) means: give me the Station, or null if not found
        Station station = stationRepository.findByCode(code.toUpperCase()).orElse(null);

        if (station == null) {
            return ApiResponse.error("Station with code " + code + " not found");
        }

        return ApiResponse.success("Station found", new StationResponse(station));
    }
}