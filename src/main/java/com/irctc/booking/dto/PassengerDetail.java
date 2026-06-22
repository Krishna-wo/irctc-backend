package com.irctc.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PassengerDetail {

    @NotBlank(message = "Passenger name is required")
    private String passengerName;

    @NotNull(message = "Passenger age is required")
    @Min(value = 1, message = "Age must be at least 1")
    private Integer passengerAge;

    @NotBlank(message = "Passenger gender is required")
    private String passengerGender; // "MALE", "FEMALE", "OTHER"

    @NotNull(message = "Seat ID is required")
    private Long seatId; // which specific seat this passenger wants

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public Integer getPassengerAge() { return passengerAge; }
    public void setPassengerAge(Integer passengerAge) { this.passengerAge = passengerAge; }

    public String getPassengerGender() { return passengerGender; }
    public void setPassengerGender(String passengerGender) { this.passengerGender = passengerGender; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }
}