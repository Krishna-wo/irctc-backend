package com.irctc.user;

import com.irctc.booking.Booking;
import com.irctc.common.response.ApiResponse;
import com.irctc.user.dto.StatsResponse;
import com.irctc.user.dto.UpdateProfileRequest;
import com.irctc.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users/profile — any logged in user
    @GetMapping("/api/users/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(userService.getProfile(userEmail));
    }

    // PUT /api/users/profile — any logged in user
    @PutMapping("/api/users/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal String userEmail,
            @Valid @RequestBody UpdateProfileRequest request) {
        ApiResponse<UserResponse> response =
                userService.updateProfile(userEmail, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // GET /api/admin/users — admin only
    @GetMapping("/api/admin/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/admin/bookings — admin only
    @GetMapping("/api/admin/bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
        return ResponseEntity.ok(userService.getAllBookings());
    }

    // GET /api/admin/stats — admin only
    @GetMapping("/api/admin/stats")
    public ResponseEntity<ApiResponse<StatsResponse>> getStats() {
        return ResponseEntity.ok(userService.getStats());
    }
}