package com.irctc.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
// @Component — Spring manages this bean
// Combined with @EnableScheduling — Spring will automatically
// call methods annotated with @Scheduled at the right time
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final RedisLockService redisLockService;

    // Payment must be completed within this many minutes
    // After this → booking auto-cancelled

    private static final int PAYMENT_TIMEOUT_MINUTES = 2;



    public BookingScheduler(BookingRepository bookingRepository,
                            BookedSeatRepository bookedSeatRepository,
                            RedisLockService redisLockService) {
        this.bookingRepository = bookingRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.redisLockService = redisLockService;
    }

    @Scheduled(fixedRate = 30000)
    // fixedRate = 60000 milliseconds = 60 seconds = 1 minute
    // Spring calls this method every 60 seconds automatically
    // fixedRate means: run every 60 seconds regardless of how long the method takes
    // Alternative: fixedDelay = wait 60 seconds AFTER method finishes
    // We use fixedRate for predictable intervals
    @Transactional
    public void cancelExpiredBookings() {

        // Calculate the cutoff time
        // Any PAYMENT_PENDING booking created before this time is expired
        LocalDateTime cutoffTime = LocalDateTime.now()
                .minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        // Example: now is 10:30 AM → cutoffTime is 10:20 AM
        // Any booking created before 10:20 AM that is still PAYMENT_PENDING → cancel it

        log.info("Scheduler running — checking for expired bookings older than {}",
                cutoffTime);

        // Find all expired pending bookings
        List<Booking> expiredBookings =
                bookingRepository.findExpiredPendingBookings(cutoffTime);

        if (expiredBookings.isEmpty()) {
            log.info("Scheduler — no expired bookings found");
            return;
        }

        log.warn("Scheduler — found {} expired bookings to cancel",
                expiredBookings.size());

        // Process each expired booking
        for (Booking booking : expiredBookings) {

            try {
                // Step 1: Get all seats for this booking
                List<BookedSeat> bookedSeats =
                        bookedSeatRepository.findByBookingId(booking.getId());

                // Step 2: Release Redis locks for each seat
                // Even if TTL already expired on Redis side — safe to call
                // releaseLock on non-existent key — it just logs a warning
                String journeyDateStr = booking.getJourneyDate().toString();
                for (BookedSeat bs : bookedSeats) {
                    redisLockService.releaseLock(
                            bs.getSeat().getId(), journeyDateStr);
                }

                // Step 3: Cancel each booked seat
                for (BookedSeat bs : bookedSeats) {
                    bs.setStatus("CANCELLED");
                    bookedSeatRepository.save(bs);
                }

                // Step 4: Cancel the booking
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);

                log.warn("Scheduler — AUTO CANCELLED booking. PNR: {} CreatedAt: {}",
                        booking.getPnrNumber(), booking.getCreatedAt());

            } catch (Exception e) {
                // If one booking fails to cancel — log and continue
                // Don't let one failure stop processing of other bookings
                log.error("Scheduler — failed to cancel booking ID: {} Error: {}",
                        booking.getId(), e.getMessage());
            }
        }

        log.info("Scheduler — completed. Cancelled {} bookings",
                expiredBookings.size());
    }
}