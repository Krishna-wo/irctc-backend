package com.irctc.booking.dto;

import com.irctc.booking.BookedSeat;
import com.irctc.seat.SeatType;
import java.math.BigDecimal;

public class BookedSeatResponse {

    private Long bookedSeatId;
    private Long seatId;
    private String coachNumber;
    private String seatNumber;
    private SeatType seatType;
    private String passengerName;
    private Integer passengerAge;
    private String passengerGender;
    private BigDecimal fare;

    public BookedSeatResponse(BookedSeat bookedSeat) {
        this.bookedSeatId = bookedSeat.getId();
        this.seatId = bookedSeat.getSeat().getId();
        this.coachNumber = bookedSeat.getSeat().getCoach().getCoachNumber();
        this.seatNumber = bookedSeat.getSeat().getSeatNumber();
        this.seatType = bookedSeat.getSeat().getSeatType();
        this.passengerName = bookedSeat.getPassengerName();
        this.passengerAge = bookedSeat.getPassengerAge();
        this.passengerGender = bookedSeat.getPassengerGender();
        this.fare = bookedSeat.getFare();
    }

    public Long getBookedSeatId() { return bookedSeatId; }
    public Long getSeatId() { return seatId; }
    public String getCoachNumber() { return coachNumber; }
    public String getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }
    public String getPassengerName() { return passengerName; }
    public Integer getPassengerAge() { return passengerAge; }
    public String getPassengerGender() { return passengerGender; }
    public BigDecimal getFare() { return fare; }
}