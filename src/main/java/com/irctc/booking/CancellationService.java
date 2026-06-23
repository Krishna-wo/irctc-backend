package com.irctc.booking;

import com.irctc.booking.dto.CancellationResponse;
import com.irctc.common.response.ApiResponse;
import com.irctc.user.User;
import com.irctc.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CancellationService {

    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final UserRepository userRepository;
    private final RefundCalculator refundCalculator;

    public CancellationService(BookingRepository bookingRepository,
                               BookedSeatRepository bookedSeatRepository,
                               UserRepository userRepository,
                               RefundCalculator refundCalculator) {
        this.bookingRepository = bookingRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.userRepository = userRepository;
        this.refundCalculator = refundCalculator;
    }

    // ── Full Booking Cancellation ─────────────────────────────────────────
    @Transactional
    public ApiResponse<CancellationResponse> cancelBooking(
            Long bookingId, String userEmail) {

        // Step 1: Load user
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        // Step 2: Load booking
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ApiResponse.error("Booking not found");
        }

        // Step 3: Security check — booking must belong to this user
        // This prevents user A from cancelling user B's booking
        // Never trust the client — always verify ownership server-side
        if (!booking.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("You are not authorized to cancel this booking");
        }

        // Step 4: Check if already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return ApiResponse.error("Booking is already cancelled");
        }

        // Step 5: Calculate total refund across all active seats
        List<BookedSeat> activeSeats = bookedSeatRepository
                .findByBookingId(bookingId)
                .stream()
                .filter(bs -> "CONFIRMED".equals(bs.getStatus()))
                .toList();
        // .toList() — Java 16+ immutable list, cleaner than .collect(Collectors.toList())

        BigDecimal totalRefund = BigDecimal.ZERO;
        for (BookedSeat seat : activeSeats) {
            BigDecimal refund = refundCalculator.calculate(
                    seat.getFare(), booking.getJourneyDate());
            totalRefund = totalRefund.add(refund);

            // Mark each seat as cancelled
            seat.setStatus("CANCELLED");
            bookedSeatRepository.save(seat);
        }

        // Step 6: Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return ApiResponse.success(
                "Booking cancelled successfully",
                new CancellationResponse(
                        booking.getId(),
                        booking.getPnrNumber(),
                        BookingStatus.CANCELLED,
                        "Full booking cancelled. Refund: ₹" + totalRefund,
                        totalRefund
                )
        );
    }

    // ── Partial Cancellation — cancel one seat from a group booking ───────
    @Transactional
    public ApiResponse<CancellationResponse> cancelSeat(
            Long bookingId, Long bookedSeatId, String userEmail) {

        // Step 1: Load user
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        // Step 2: Load booking
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ApiResponse.error("Booking not found");
        }

        // Step 3: Ownership check
        if (!booking.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("You are not authorized to cancel this booking");
        }

        // Step 4: Check booking not already fully cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return ApiResponse.error("Booking is already cancelled");
        }

        // Step 5: Load the specific booked seat
        BookedSeat bookedSeat = bookedSeatRepository
                .findById(bookedSeatId).orElse(null);
        if (bookedSeat == null) {
            return ApiResponse.error("Booked seat not found");
        }

        // Step 6: Verify this seat belongs to this booking
        if (!bookedSeat.getBooking().getId().equals(bookingId)) {
            return ApiResponse.error("This seat does not belong to this booking");
        }

        // Step 7: Check seat not already cancelled
        if ("CANCELLED".equals(bookedSeat.getStatus())) {
            return ApiResponse.error("This seat is already cancelled");
        }

        // Step 8: Calculate refund for this seat
        BigDecimal refund = refundCalculator.calculate(
                bookedSeat.getFare(), booking.getJourneyDate());

        // Step 9: Cancel the seat
        bookedSeat.setStatus("CANCELLED");
        bookedSeatRepository.save(bookedSeat);

        // Step 10: Update total fare on the booking
        booking.setTotalFare(
                booking.getTotalFare().subtract(bookedSeat.getFare()));
        // Subtract the cancelled seat's fare from total

        // Step 11: Check if ALL seats are now cancelled
        // If yes — mark the whole booking as CANCELLED too
        boolean anyActiveSeats = bookedSeatRepository
                .existsByBookingIdAndStatus(bookingId, "CONFIRMED");

        if (!anyActiveSeats) {
            // All seats cancelled — cancel the whole booking
            booking.setStatus(BookingStatus.CANCELLED);
        }

        bookingRepository.save(booking);

        return ApiResponse.success(
                "Seat cancelled successfully",
                new CancellationResponse(
                        booking.getId(),
                        booking.getPnrNumber(),
                        booking.getStatus(),
                        "Seat cancelled. Refund: ₹" + refund,
                        refund
                )
        );
    }
}