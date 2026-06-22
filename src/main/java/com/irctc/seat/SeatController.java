package com.irctc.seat;

import com.irctc.common.response.ApiResponse;
import com.irctc.seat.dto.SeatRequest;
import com.irctc.seat.dto.SeatResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/seats")
// /api/admin/** → ADMIN only via SecurityConfig
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    // POST /api/admin/seats
    @PostMapping
    public ResponseEntity<ApiResponse<SeatResponse>> addSeat(
            @Valid @RequestBody SeatRequest request) {
        ApiResponse<SeatResponse> response = seatService.addSeat(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/admin/seats/coach/1
    @GetMapping("/coach/{coachId}")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByCoach(
            @PathVariable Long coachId) {
        ApiResponse<List<SeatResponse>> response = seatService.getSeatsByCoach(coachId);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }
}