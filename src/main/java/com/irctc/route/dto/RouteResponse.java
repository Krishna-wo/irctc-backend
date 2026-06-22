package com.irctc.route.dto;

import com.irctc.route.Route;

public class RouteResponse {

    private Long id;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private Long stationId;
    private String stationCode;
    private String stationName;
    private String stationCity;
    private Integer sequenceNumber;
    // LocalTime serializes to "HH:mm:ss" by default in Jackson — clean enough
    private String arrivalTime;
    private String departureTime;
    private Integer distanceFromOrigin;

    // Called while inside a @Transactional method so LAZY loading works fine
    public RouteResponse(Route route) {
        this.id = route.getId();
        this.trainId = route.getTrain().getId();
        this.trainNumber = route.getTrain().getTrainNumber();
        this.trainName = route.getTrain().getName();
        this.stationId = route.getStation().getId();
        this.stationCode = route.getStation().getCode();
        this.stationName = route.getStation().getName();
        this.stationCity = route.getStation().getCity();
        this.sequenceNumber = route.getSequenceNumber();
        // Convert LocalTime to "HH:mm" string for clean API output
        this.arrivalTime = route.getArrivalTime() != null
                ? route.getArrivalTime().toString() : null;
        this.departureTime = route.getDepartureTime() != null
                ? route.getDepartureTime().toString() : null;
        this.distanceFromOrigin = route.getDistanceFromOrigin();
    }

    public Long getId() { return id; }
    public Long getTrainId() { return trainId; }
    public String getTrainNumber() { return trainNumber; }
    public String getTrainName() { return trainName; }
    public Long getStationId() { return stationId; }
    public String getStationCode() { return stationCode; }
    public String getStationName() { return stationName; }
    public String getStationCity() { return stationCity; }
    public Integer getSequenceNumber() { return sequenceNumber; }
    public String getArrivalTime() { return arrivalTime; }
    public String getDepartureTime() { return departureTime; }
    public Integer getDistanceFromOrigin() { return distanceFromOrigin; }
}
