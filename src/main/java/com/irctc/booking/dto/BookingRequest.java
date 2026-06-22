package com.irctc.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class BookingRequest {

    @NotNull(message = "Train ID is required")
    private Long trainId;

    @NotBlank(message = "Source station code is required")
    private String sourceStationCode; // "NDLS"

    @NotBlank(message = "Destination station code is required")
    private String destinationStationCode; // "BCT"

    @NotNull(message = "Journey date is required")
    @Future(message = "Journey date must be in the future")
    private LocalDate journeyDate;

    // One booking = one or more passengers
    // Each passenger has their own seat
    @NotEmpty(message = "At least one passenger is required")
    @Valid // tells Spring to validate each PassengerDetail inside the list
    private List<PassengerDetail> passengers;

    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }

    public String getSourceStationCode() { return sourceStationCode; }
    public void setSourceStationCode(String sourceStationCode) { this.sourceStationCode = sourceStationCode; }

    public String getDestinationStationCode() { return destinationStationCode; }
    public void setDestinationStationCode(String destinationStationCode) { this.destinationStationCode = destinationStationCode; }

    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }

    public List<PassengerDetail> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerDetail> passengers) { this.passengers = passengers; }
}