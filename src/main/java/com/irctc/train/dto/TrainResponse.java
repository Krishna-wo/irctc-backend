package com.irctc.train.dto;

import com.irctc.train.Train;
import com.irctc.train.TrainType;

public class TrainResponse {

    private Long id;
    private String trainNumber;
    private String name;
    private TrainType type;
    private Integer totalDistanceKm;

    // Constructor maps directly from the Train entity
    // Field names match the actual Train entity: name, type, totalDistanceKm
    public TrainResponse(Train train) {
        this.id = train.getId();
        this.trainNumber = train.getTrainNumber();
        this.name = train.getName();
        this.type = train.getType();
        this.totalDistanceKm = train.getTotalDistanceKm();
    }

    public Long getId() { return id; }
    public String getTrainNumber() { return trainNumber; }
    public String getName() { return name; }
    public TrainType getType() { return type; }
    public Integer getTotalDistanceKm() { return totalDistanceKm; }
}
