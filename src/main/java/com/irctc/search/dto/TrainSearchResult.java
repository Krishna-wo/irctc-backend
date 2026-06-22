package com.irctc.search.dto;

import com.irctc.route.Route;
import com.irctc.train.Train;
import com.irctc.train.TrainType;

public class TrainSearchResult {

    private Long trainId;
    private String trainNumber;
    private String trainName;
    private TrainType trainType;

    // Source stop details
    private String sourceStationCode;
    private String sourceStationName;
    private Integer sourceSequenceNumber;
    private String sourceDepartureTime;

    // Destination stop details
    private String destinationStationCode;
    private String destinationStationName;
    private Integer destinationSequenceNumber;
    private String destinationArrivalTime;

    // Distance between the two stops
    private Integer distanceBetweenStops;

    // Constructor takes Train + the two Route stops
    public TrainSearchResult(Train train, Route sourceStop, Route destinationStop) {
        this.trainId = train.getId();
        this.trainNumber = train.getTrainNumber();
        this.trainName = train.getName();
        this.trainType = train.getType();

        this.sourceStationCode = sourceStop.getStation().getCode();
        this.sourceStationName = sourceStop.getStation().getName();
        this.sourceSequenceNumber = sourceStop.getSequenceNumber();
        this.sourceDepartureTime = sourceStop.getDepartureTime() != null
                ? sourceStop.getDepartureTime().toString() : null;

        this.destinationStationCode = destinationStop.getStation().getCode();
        this.destinationStationName = destinationStop.getStation().getName();
        this.destinationSequenceNumber = destinationStop.getSequenceNumber();
        this.destinationArrivalTime = destinationStop.getArrivalTime() != null
                ? destinationStop.getArrivalTime().toString() : null;

        // Distance between stops = destination distance - source distance
        this.distanceBetweenStops = destinationStop.getDistanceFromOrigin()
                - sourceStop.getDistanceFromOrigin();
    }

    // All getters
    public Long getTrainId() { return trainId; }
    public String getTrainNumber() { return trainNumber; }
    public String getTrainName() { return trainName; }
    public TrainType getTrainType() { return trainType; }
    public String getSourceStationCode() { return sourceStationCode; }
    public String getSourceStationName() { return sourceStationName; }
    public Integer getSourceSequenceNumber() { return sourceSequenceNumber; }
    public String getSourceDepartureTime() { return sourceDepartureTime; }
    public String getDestinationStationCode() { return destinationStationCode; }
    public String getDestinationStationName() { return destinationStationName; }
    public Integer getDestinationSequenceNumber() { return destinationSequenceNumber; }
    public String getDestinationArrivalTime() { return destinationArrivalTime; }
    public Integer getDistanceBetweenStops() { return distanceBetweenStops; }
}