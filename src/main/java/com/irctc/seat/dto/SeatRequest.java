package com.irctc.seat.dto;

import com.irctc.seat.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SeatRequest {

    @NotNull(message = "Coach ID is required")
    private Long coachId;

    // "1", "23", "72" — String not Integer
    // Seat numbers can have suffixes in some coach types
    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    // Matches entity field: seatType
    @NotNull(message = "Seat type is required")
    private SeatType seatType;

    public Long getCoachId() { return coachId; }
    public void setCoachId(Long coachId) { this.coachId = coachId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }
}