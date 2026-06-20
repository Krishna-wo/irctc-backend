package com.irctc.train.dto;

import com.irctc.common.enums.TrainType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TrainRequest {

    @NotBlank(message = "Train number is required")
    private String trainNumber; // "12301" — always a string, never an int

    // Why String and not Integer for train number?
    // Because "00101" would lose its leading zeros as Integer
    // Train numbers are identifiers, not numbers you do math on

    @NotBlank(message = "Train name is required")
    private String trainName; // "Howrah Rajdhani Express"

    @NotNull(message = "Train type is required")
    private TrainType trainType; // RAJDHANI, SHATABDI, EXPRESS, PASSENGER

    // Getters and setters
    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public TrainType getTrainType() { return trainType; }
    public void setTrainType(TrainType trainType) { this.trainType = trainType; }
}