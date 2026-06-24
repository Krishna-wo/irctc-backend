package com.irctc.booking;

import com.irctc.booking.dto.BookingResponse;
import com.irctc.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST /api/payments/confirm/{bookingId}
    // Simulates successful payment → moves booking to CONFIRMED
    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmPayment(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal String userEmail) {
        ApiResponse<BookingResponse> response =
                paymentService.confirmPayment(bookingId, userEmail);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // POST /api/payments/fail/{bookingId}
    // Simulates failed payment → cancels booking → releases locks
    @PostMapping("/fail/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> failPayment(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal String userEmail) {
        ApiResponse<Void> response =
                paymentService.failPayment(bookingId, userEmail);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}