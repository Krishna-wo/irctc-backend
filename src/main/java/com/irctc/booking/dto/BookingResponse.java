package com.irctc.booking.dto;

import com.irctc.booking.Booking;
import com.irctc.booking.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {

    private Long bookingId;
    private String pnrNumber;
    private BookingStatus status;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    private LocalDate journeyDate;
    private BigDecimal totalFare;
    private LocalDateTime createdAt;
    private List<BookedSeatResponse> passengers;

    // Main constructor from Booking entity
    // passengers list added separately after building booked seats
    public BookingResponse(Booking booking, List<BookedSeatResponse> passengers) {
        this.bookingId = booking.getId();
        this.pnrNumber = booking.getPnrNumber();
        this.status = booking.getStatus();
        // LAZY loads — safe inside @Transactional
        this.trainNumber = booking.getTrain().getTrainNumber();
        this.trainName = booking.getTrain().getName();
        this.sourceStation = booking.getSourceStation().getCode()
                + " - " + booking.getSourceStation().getName();
        this.destinationStation = booking.getDestinationStation().getCode()
                + " - " + booking.getDestinationStation().getName();
        this.journeyDate = booking.getJourneyDate();
        this.totalFare = booking.getTotalFare();
        this.createdAt = booking.getCreatedAt();
        this.passengers = passengers;
    }

    public Long getBookingId() { return bookingId; }
    public String getPnrNumber() { return pnrNumber; }
    public BookingStatus getStatus() { return status; }
    public String getTrainNumber() { return trainNumber; }
    public String getTrainName() { return trainName; }
    public String getSourceStation() { return sourceStation; }
    public String getDestinationStation() { return destinationStation; }
    public LocalDate getJourneyDate() { return journeyDate; }
    public BigDecimal getTotalFare() { return totalFare; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<BookedSeatResponse> getPassengers() { return passengers; }
}