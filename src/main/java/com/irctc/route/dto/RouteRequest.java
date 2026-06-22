package com.irctc.route.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RouteRequest {

    @NotNull(message = "Train ID is required")
    private Long trainId;

    @NotNull(message = "Station ID is required")
    private Long stationId;

    // Route entity calls this "sequenceNumber" — must match
    @NotNull(message = "Sequence number is required")
    @Min(value = 1, message = "Sequence number must be at least 1")
    private Integer sequenceNumber;

    // Route entity uses LocalTime — we accept "HH:mm" strings and parse them
    // arrivalTime is nullable — first stop has no arrival
    private String arrivalTime;

    // departureTime is nullable — last stop has no departure
    private String departureTime;

    @NotNull(message = "Distance from origin is required")
    @Min(value = 0, message = "Distance cannot be negative")
    private Integer distanceFromOrigin;

    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }

    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public Integer getDistanceFromOrigin() { return distanceFromOrigin; }
    public void setDistanceFromOrigin(Integer distanceFromOrigin) { this.distanceFromOrigin = distanceFromOrigin; }
}
