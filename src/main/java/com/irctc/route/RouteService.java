package com.irctc.route;

import com.irctc.common.response.ApiResponse;
import com.irctc.route.dto.RouteRequest;
import com.irctc.route.dto.RouteResponse;
import com.irctc.station.Station;
import com.irctc.station.StationRepository;
import com.irctc.train.Train;
import com.irctc.train.TrainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {

    // Route service orchestrates 3 repositories — this is normal
    private final RouteRepository routeRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;

    public RouteService(RouteRepository routeRepository,
                        TrainRepository trainRepository,
                        StationRepository stationRepository) {
        this.routeRepository = routeRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public ApiResponse<RouteResponse> addRouteStop(RouteRequest request) {

        // Step 1: Validate train exists
        Train train = trainRepository.findById(request.getTrainId()).orElse(null);
        if (train == null) {
            return ApiResponse.error("Train with ID " + request.getTrainId() + " not found");
        }

        // Step 2: Validate station exists
        Station station = stationRepository.findById(request.getStationId()).orElse(null);
        if (station == null) {
            return ApiResponse.error("Station with ID " + request.getStationId() + " not found");
        }

        // Step 3: sequence_number must be unique per train
        if (routeRepository.existsByTrainIdAndSequenceNumber(
                request.getTrainId(), request.getSequenceNumber())) {
            return ApiResponse.error("Sequence number " + request.getSequenceNumber()
                    + " already exists for train " + train.getTrainNumber());
        }

        // Step 4: station must appear only once per train
        if (routeRepository.existsByTrainIdAndStationId(
                request.getTrainId(), request.getStationId())) {
            return ApiResponse.error("Station " + station.getCode()
                    + " is already a stop on train " + train.getTrainNumber());
        }

        // Step 5: parse "HH:mm" strings into LocalTime (what the entity uses)
        LocalTime arrivalTime = null;
        LocalTime departureTime = null;

        if (request.getArrivalTime() != null && !request.getArrivalTime().isBlank()) {
            try {
                arrivalTime = LocalTime.parse(request.getArrivalTime());
            } catch (DateTimeParseException e) {
                return ApiResponse.error("Invalid arrival time format. Use HH:mm (e.g. 06:30)");
            }
        }

        if (request.getDepartureTime() != null && !request.getDepartureTime().isBlank()) {
            try {
                departureTime = LocalTime.parse(request.getDepartureTime());
            } catch (DateTimeParseException e) {
                return ApiResponse.error("Invalid departure time format. Use HH:mm (e.g. 07:00)");
            }
        }

        // Step 6: Build and save entity
        Route route = new Route();
        route.setTrain(train);
        route.setStation(station);
        route.setSequenceNumber(request.getSequenceNumber());
        route.setArrivalTime(arrivalTime);
        route.setDepartureTime(departureTime);
        route.setDistanceFromOrigin(request.getDistanceFromOrigin());

        Route saved = routeRepository.save(route);

        // RouteResponse accesses train and station via LAZY loading —
        // safe here because we are still inside @Transactional (session is open)
        return ApiResponse.success("Route stop added successfully", new RouteResponse(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<RouteResponse>> getRouteByTrain(Long trainId) {

        if (!trainRepository.existsById(trainId)) {
            return ApiResponse.error("Train with ID " + trainId + " not found");
        }

        List<RouteResponse> routes = routeRepository
                .findByTrainIdOrderBySequenceNumberAsc(trainId)
                .stream()
                .map(RouteResponse::new)
                .collect(Collectors.toList());

        if (routes.isEmpty()) {
            return ApiResponse.error("No route stops found for this train");
        }

        return ApiResponse.success("Route fetched successfully", routes);
    }
}
