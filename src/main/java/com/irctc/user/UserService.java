package com.irctc.user;

import com.irctc.booking.Booking;
import com.irctc.booking.BookingRepository;
import com.irctc.common.response.ApiResponse;
import com.irctc.user.dto.StatsResponse;
import com.irctc.user.dto.UpdateProfileRequest;
import com.irctc.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public UserService(UserRepository userRepository,
                       BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    // ── Get logged in user's profile ──────────────────────────────────────
    @Transactional(readOnly = true)
    public ApiResponse<UserResponse> getProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }
        return ApiResponse.success("Profile fetched successfully", new UserResponse(user));
    }

    // ── Update logged in user's profile ───────────────────────────────────
    @Transactional
    public ApiResponse<UserResponse> updateProfile(
            String userEmail, UpdateProfileRequest request) {

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        // Only update fields that were actually sent
        // If name is null in request — keep existing name
        // This is PATCH behavior — partial update
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            // Check if new phone is already taken by another user
            if (userRepository.existsByPhone(request.getPhone())
                    && !request.getPhone().equals(user.getPhone())) {
                return ApiResponse.error("Phone number already in use");
            }
            user.setPhone(request.getPhone().trim());
        }

        // @PreUpdate on entity handles updatedAt automatically
        User saved = userRepository.save(user);
        return ApiResponse.success("Profile updated successfully", new UserResponse(saved));
    }

    // ── Admin: get all users ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ApiResponse.success("Users fetched successfully", users);
    }

    // ── Admin: get all bookings ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public ApiResponse<List<Booking>> getAllBookings() {
        // Returns raw bookings for admin — admin sees everything
        List<Booking> bookings = bookingRepository.findAll();
        if (bookings.isEmpty()) {
            return ApiResponse.error("No bookings found");
        }
        return ApiResponse.success("Bookings fetched successfully", bookings);
    }

    // ── Admin: get system stats ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public ApiResponse<StatsResponse> getStats() {
        // COUNT(*) from users table
        Long totalUsers = userRepository.count();

        // COUNT(*) from bookings table
        Long totalBookings = bookingRepository.count();

        // SUM(total_fare) from confirmed bookings
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();

        // Train with most bookings
        String mostBookedTrain = bookingRepository.findMostBookedTrainNumber();
        if (mostBookedTrain == null) {
            mostBookedTrain = "No bookings yet";
        }

        return ApiResponse.success("Stats fetched successfully",
                new StatsResponse(totalUsers, totalBookings,
                        totalRevenue, mostBookedTrain));
    }
}