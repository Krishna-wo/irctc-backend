package com.irctc.coach.dto;

import com.irctc.coach.Coach;
import com.irctc.coach.CoachType;

public class CoachResponse {

    private Long id;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String coachNumber;
    private CoachType coachType;
    private Integer totalSeats;

    // Called inside @Transactional so LAZY loading is safe
    public CoachResponse(Coach coach) {
        this.id = coach.getId();
        // Accessing coach.getTrain() triggers LAZY load — safe inside transaction
        this.trainId = coach.getTrain().getId();
        this.trainNumber = coach.getTrain().getTrainNumber();
        this.trainName = coach.getTrain().getName();
        this.coachNumber = coach.getCoachNumber();
        this.coachType = coach.getCoachType();
        this.totalSeats = coach.getTotalSeats();
    }

    public Long getId() { return id; }
    public Long getTrainId() { return trainId; }
    public String getTrainNumber() { return trainNumber; }
    public String getTrainName() { return trainName; }
    public String getCoachNumber() { return coachNumber; }
    public CoachType getCoachType() { return coachType; }
    public Integer getTotalSeats() { return totalSeats; }
}