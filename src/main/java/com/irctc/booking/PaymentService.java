package com.irctc.booking;

import com.irctc.booking.dto.BookedSeatResponse;
import com.irctc.booking.dto.BookingResponse;
import com.irctc.common.response.ApiResponse;
import com.irctc.user.User;
import com.irctc.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final UserRepository userRepository;
    private final RedisLockService redisLockService;

    public PaymentService(BookingRepository bookingRepository,
                          BookedSeatRepository bookedSeatRepository,
                          UserRepository userRepository,
                          RedisLockService redisLockService) {
        this.bookingRepository = bookingRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.userRepository = userRepository;
        this.redisLockService = redisLockService;
    }

    // ── Confirm payment → CONFIRMED ───────────────────────────────────────
    @Transactional
    public ApiResponse<BookingResponse> confirmPayment(
            Long bookingId, String userEmail) {

        // Load user
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return ApiResponse.error("User not found");

        // Load booking
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return ApiResponse.error("Booking not found");

        // Ownership check
        if (!booking.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("Not authorized");
        }

        // Must be in PAYMENT_PENDING to confirm
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            return ApiResponse.error(
                    "Cannot confirm payment. Booking status is: " + booking.getStatus());
        }

        // Move to CONFIRMED
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Release Redis locks — seats are now permanently booked in DB
        // Lock is no longer needed — DB record is the source of truth
        String journeyDateStr = booking.getJourneyDate().toString();
        List<BookedSeat> bookedSeats =
                bookedSeatRepository.findByBookingId(bookingId);

        for (BookedSeat bs : bookedSeats) {
            redisLockService.releaseLock(bs.getSeat().getId(), journeyDateStr);
        }

        log.info("Payment CONFIRMED. PNR: {} User: {}", booking.getPnrNumber(), userEmail);

        List<BookedSeatResponse> passengerResponses = bookedSeats.stream()
                .map(BookedSeatResponse::new)
                .collect(Collectors.toList());

        return ApiResponse.success(
                "Payment confirmed! Booking CONFIRMED. PNR: " + booking.getPnrNumber(),
                new BookingResponse(booking, passengerResponses)
        );
    }

    // ── Fail payment → CANCELLED + release locks ──────────────────────────
    @Transactional
    public ApiResponse<Void> failPayment(Long bookingId, String userEmail) {

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return ApiResponse.error("User not found");

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return ApiResponse.error("Booking not found");

        if (!booking.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("Not authorized");
        }

        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            return ApiResponse.error("Booking is not in PAYMENT_PENDING state");
        }

        // Cancel booking
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release all Redis locks immediately
        String journeyDateStr = booking.getJourneyDate().toString();
        List<BookedSeat> bookedSeats =
                bookedSeatRepository.findByBookingId(bookingId);

        for (BookedSeat bs : bookedSeats) {
            redisLockService.releaseLock(bs.getSeat().getId(), journeyDateStr);
        }

        log.warn("Payment FAILED. Booking CANCELLED. PNR: {}", booking.getPnrNumber());

        return ApiResponse.success(
                "Payment failed. Booking cancelled. Seats released.", null);
    }
}