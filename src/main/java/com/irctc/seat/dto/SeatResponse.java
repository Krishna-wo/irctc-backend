package com.irctc.seat.dto;

import com.irctc.seat.Seat;
import com.irctc.seat.SeatType;

public class SeatResponse {

    private Long id;
    private Long coachId;
    private String coachNumber;
    private String coachType;
    private String seatNumber;
    private SeatType seatType;

    // Called inside @Transactional — LAZY loading on coach is safe
    public SeatResponse(Seat seat) {
        this.id = seat.getId();
        // seat.getCoach() triggers LAZY load — safe inside transaction
        this.coachId = seat.getCoach().getId();
        this.coachNumber = seat.getCoach().getCoachNumber();
        // CoachType is an enum — .name() gives us the String
        this.coachType = seat.getCoach().getCoachType().name();
        this.seatNumber = seat.getSeatNumber();
        this.seatType = seat.getSeatType();
    }

    public Long getId() { return id; }
    public Long getCoachId() { return coachId; }
    public String getCoachNumber() { return coachNumber; }
    public String getCoachType() { return coachType; }
    public String getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }
}