package com.irctc.booking;

import com.irctc.booking.dto.BookingRequest;
import com.irctc.booking.dto.BookingResponse;
import com.irctc.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
// /api/bookings — NOT /api/admin/
// Any authenticated user (PASSENGER or ADMIN) can book
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // POST /api/bookings — create a booking
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal String userEmail) {
        // @AuthenticationPrincipal extracts the principal from SecurityContext
        // In our JwtAuthFilter we set email as the principal:
        // new UsernamePasswordAuthenticationToken(email, null, authorities)
        // So @AuthenticationPrincipal gives us the email string directly
        // No need to parse the JWT again — Spring Security already did it

        ApiResponse<BookingResponse> response =
                bookingService.createBooking(request, userEmail);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/bookings/pnr/A3F2B1C9D0 — check PNR status
    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByPnr(
            @PathVariable String pnr) {
        ApiResponse<BookingResponse> response =
                bookingService.getBookingByPnr(pnr.toUpperCase());
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // GET /api/bookings/my — get all bookings for logged-in user
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal String userEmail) {
        ApiResponse<List<BookingResponse>> response =
                bookingService.getMyBookings(userEmail);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }
}