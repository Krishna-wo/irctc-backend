package com.irctc.train.dto;

import com.irctc.train.TrainType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TrainRequest {

    // String not Integer — "00101" would lose leading zeros as int
    @NotBlank(message = "Train number is required")
    private String trainNumber;

    // Matches entity field: name (not trainName)
    @NotBlank(message = "Train name is required")
    private String name;

    // TrainType lives in com.irctc.train — NOT in common.enums (that package doesn't exist)
    // Matches entity field: type (not trainType)
    @NotNull(message = "Train type is required")
    private TrainType type;

    // Entity has totalDistanceKm as NOT NULL — must be in request
    @NotNull(message = "Total distance in km is required")
    @Min(value = 1, message = "Distance must be at least 1 km")
    private Integer totalDistanceKm;

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TrainType getType() { return type; }
    public void setType(TrainType type) { this.type = type; }

    public Integer getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(Integer totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
}
