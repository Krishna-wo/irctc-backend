package com.irctc.booking;

import com.irctc.booking.dto.CancellationResponse;
import com.irctc.common.response.ApiResponse;
import com.irctc.user.User;
import com.irctc.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
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


    @Transactional
    public ApiResponse<CancellationResponse> cancelBooking(
            Long bookingId, String userEmail) {

        // Step 1
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        // Step 2
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ApiResponse.error("Booking not found");
        }

        // Step 3
        if (!booking.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("You are not authorized to cancel this booking");
        }

        // Step 4
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return ApiResponse.error("Booking is already cancelled");
        }

        // Step 5
        List<BookedSeat> activeSeats = bookedSeatRepository
                .findByBookingId(bookingId)
                .stream()
                .filter(bs -> "CONFIRMED".equals(bs.getStatus()))
                .toList();


        BigDecimal totalRefund = BigDecimal.ZERO;
        for (BookedSeat seat : activeSeats) {
            BigDecimal refund = refundCalculator.calculate(
                    seat.getFare(), booking.getJourneyDate());
            totalRefund = totalRefund.add(refund);

            // Mark each seat as cancelled
            seat.setStatus("CANCELLED");
            bookedSeatRepository.save(seat);
        }

        // Step 6

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking {} cancelled by user: {}. Refund: {}",
                booking.getPnrNumber(), userEmail, totalRefund);
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


    @Transactional
    public ApiResponse<CancellationResponse> cancelSeat(
            Long bookingId, Long bookedSeatId, String userEmail) {

        // Step 1
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        // Step 2
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ApiResponse.error("Booking not found");
        }

        // Step 3
        if (!booking.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("You are not authorized to cancel this booking");
        }

        // Step 4
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return ApiResponse.error("Booking is already cancelled");
        }

        // Step 5
        BookedSeat bookedSeat = bookedSeatRepository
                .findById(bookedSeatId).orElse(null);
        if (bookedSeat == null) {
            return ApiResponse.error("Booked seat not found");
        }

        // Step 6
        if (!bookedSeat.getBooking().getId().equals(bookingId)) {
            return ApiResponse.error("This seat does not belong to this booking");
        }

        // Step 7
        if ("CANCELLED".equals(bookedSeat.getStatus())) {
            return ApiResponse.error("This seat is already cancelled");
        }

        // Step 8
        BigDecimal refund = refundCalculator.calculate(
                bookedSeat.getFare(), booking.getJourneyDate());

        // Step 9
        bookedSeat.setStatus("CANCELLED");
        bookedSeatRepository.save(bookedSeat);
        log.warn("Seat {} cancelled from booking {}. Refund: {}",
                bookedSeat.getSeat().getSeatNumber(), booking.getPnrNumber(), refund);

        // Step 10
        booking.setTotalFare(
                booking.getTotalFare().subtract(bookedSeat.getFare()));
        // Subtract the cancelled seat's fare from total

        // Step 11
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