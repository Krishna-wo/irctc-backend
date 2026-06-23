package com.irctc.booking.dto;

import com.irctc.booking.BookingStatus;
import java.math.BigDecimal;

public class CancellationResponse {

    private Long bookingId;
    private String pnrNumber;
    private BookingStatus bookingStatus;
    private String message;
    private BigDecimal refundAmount;

    public CancellationResponse(Long bookingId, String pnrNumber,
                                BookingStatus bookingStatus,
                                String message, BigDecimal refundAmount) {
        this.bookingId = bookingId;
        this.pnrNumber = pnrNumber;
        this.bookingStatus = bookingStatus;
        this.message = message;
        this.refundAmount = refundAmount;
    }

    public Long getBookingId() { return bookingId; }
    public String getPnrNumber() { return pnrNumber; }
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public String getMessage() { return message; }
    public BigDecimal getRefundAmount() { return refundAmount; }
}