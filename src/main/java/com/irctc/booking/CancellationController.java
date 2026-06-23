package com.irctc.booking;

import com.irctc.booking.dto.CancellationResponse;
import com.irctc.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class CancellationController {

    private final CancellationService cancellationService;

    public CancellationController(CancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    // POST /api/bookings/1/cancel — cancel entire booking
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal String userEmail) {

        ApiResponse<CancellationResponse> response =
                cancellationService.cancelBooking(bookingId, userEmail);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // POST /api/bookings/1/cancel-seat/2 — cancel one seat from booking
    @PostMapping("/{bookingId}/cancel-seat/{bookedSeatId}")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancelSeat(
            @PathVariable Long bookingId,
            @PathVariable Long bookedSeatId,
            @AuthenticationPrincipal String userEmail) {

        ApiResponse<CancellationResponse> response =
                cancellationService.cancelSeat(bookingId, bookedSeatId, userEmail);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}