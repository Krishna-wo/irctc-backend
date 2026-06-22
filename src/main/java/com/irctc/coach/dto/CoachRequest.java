package com.irctc.coach.dto;

import com.irctc.coach.CoachType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CoachRequest {

    @NotNull(message = "Train ID is required")
    private Long trainId;

    // "S1", "S2", "A1", "B1" — identifier, not a number
    @NotBlank(message = "Coach number is required")
    private String coachNumber;

    // Matches entity field: coachType
    @NotNull(message = "Coach type is required")
    private CoachType coachType;

    // Matches entity field: totalSeats
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Coach must have at least 1 seat")
    private Integer totalSeats;

    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }

    public String getCoachNumber() { return coachNumber; }
    public void setCoachNumber(String coachNumber) { this.coachNumber = coachNumber; }

    public CoachType getCoachType() { return coachType; }
    public void setCoachType(CoachType coachType) { this.coachType = coachType; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
}