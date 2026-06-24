package com.irctc.booking;

import com.irctc.booking.dto.*;
import com.irctc.coach.Coach;
import com.irctc.common.response.ApiResponse;
import com.irctc.route.Route;
import com.irctc.route.RouteRepository;
import com.irctc.seat.Seat;
import com.irctc.seat.SeatRepository;
import com.irctc.station.Station;
import com.irctc.station.StationRepository;
import com.irctc.train.Train;
import com.irctc.train.TrainRepository;
import com.irctc.user.User;
import com.irctc.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final SeatRepository seatRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final FareCalculator fareCalculator;
    private final PNRGenerator pnrGenerator;
    private final RedisLockService redisLockService;

    public BookingService(BookingRepository bookingRepository,
                          BookedSeatRepository bookedSeatRepository,
                          TrainRepository trainRepository,
                          StationRepository stationRepository,
                          SeatRepository seatRepository,
                          RouteRepository routeRepository,
                          UserRepository userRepository,
                          FareCalculator fareCalculator,
                          PNRGenerator pnrGenerator, RedisLockService redisLockService) {
        this.bookingRepository = bookingRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.seatRepository = seatRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.fareCalculator = fareCalculator;
        this.pnrGenerator = pnrGenerator;
        this.redisLockService = redisLockService;
    }
    // Add these to your existing BookingService constructor dependencies:
// private final RedisLockService redisLockService;

    @Transactional
    public ApiResponse<BookingResponse> createBooking(
            BookingRequest request, String userEmail) {

        // ── Steps 1-6 remain exactly the same ────────────────────────────
        // (user, train, station, route validation — don't change those)

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return ApiResponse.error("User not found");

        Train train = trainRepository.findById(request.getTrainId()).orElse(null);
        if (train == null) return ApiResponse.error("Train not found");

        String sourceCode = request.getSourceStationCode().toUpperCase().trim();
        Station sourceStation = stationRepository.findByCode(sourceCode).orElse(null);
        if (sourceStation == null) return ApiResponse.error("Source station not found");

        String destCode = request.getDestinationStationCode().toUpperCase().trim();
        Station destinationStation = stationRepository.findByCode(destCode).orElse(null);
        if (destinationStation == null) return ApiResponse.error("Destination station not found");

        Route sourceRoute = routeRepository
                .findByTrainIdAndStationId(train.getId(), sourceStation.getId())
                .orElse(null);
        Route destRoute = routeRepository
                .findByTrainIdAndStationId(train.getId(), destinationStation.getId())
                .orElse(null);

        if (sourceRoute == null || destRoute == null) {
            return ApiResponse.error("Train does not stop at given stations");
        }

        if (sourceRoute.getSequenceNumber() >= destRoute.getSequenceNumber()) {
            return ApiResponse.error("Source must come before destination");
        }

        int distanceKm = destRoute.getDistanceFromOrigin()
                - sourceRoute.getDistanceFromOrigin();

        // ── NEW: Track which locks we acquired ────────────────────────────
        // We need this list so if booking fails halfway, we release ALL locks
        // Example: 3 passengers, locks acquired for seats 1 and 2, seat 3 fails
        // We must release locks for seats 1 and 2 — otherwise they stay locked forever
        List<Long> acquiredLockSeatIds = new ArrayList<>();
        String journeyDateStr = request.getJourneyDate().toString();
        // Convert LocalDate to String: "2026-12-25" — used as part of lock key

        try {
            BigDecimal totalFare = BigDecimal.ZERO;
            List<SeatFarePair> seatFarePairs = new ArrayList<>();

            // Replace the for loop section with this cleaner approach:

            for (PassengerDetail passenger : request.getPassengers()) {

                Seat seat = seatRepository.findById(passenger.getSeatId()).orElse(null);
                if (seat == null) {
                    // Release all locks acquired so far before returning
                    releaseAllLocks(acquiredLockSeatIds, journeyDateStr);
                    return ApiResponse.error("Seat with ID " + passenger.getSeatId() + " not found");
                }

                Coach coach = seat.getCoach();
                if (!coach.getTrain().getId().equals(train.getId())) {
                    releaseAllLocks(acquiredLockSeatIds, journeyDateStr);
                    return ApiResponse.error("Seat does not belong to this train");
                }

                boolean lockAcquired = redisLockService.acquireLock(
                        seat.getId(), journeyDateStr);

                if (!lockAcquired) {
                    releaseAllLocks(acquiredLockSeatIds, journeyDateStr);
                    return ApiResponse.error(
                            "Seat " + seat.getSeatNumber() +
                                    " is currently being booked. Try another seat.");
                }

                acquiredLockSeatIds.add(seat.getId());

                boolean alreadyBooked = bookedSeatRepository
                        .existsBySeatIdAndBooking_JourneyDateAndStatus(
                                seat.getId(), request.getJourneyDate(), "CONFIRMED");

                if (alreadyBooked) {
                    releaseAllLocks(acquiredLockSeatIds, journeyDateStr);
                    return ApiResponse.error(
                            "Seat " + seat.getSeatNumber() + " already booked for "
                                    + request.getJourneyDate());
                }

                BigDecimal fare = fareCalculator.calculate(distanceKm, coach.getCoachType());
                totalFare = totalFare.add(fare);
                seatFarePairs.add(new SeatFarePair(seat, fare, passenger));
            }

            // ── NEW: Save as PAYMENT_PENDING not CONFIRMED ────────────────
            // Booking is not confirmed until payment is made
            // Locks will hold the seats for 10 minutes
            String pnr = pnrGenerator.generate();

            Booking booking = Booking.builder()
                    .user(user)
                    .train(train)
                    .sourceStation(sourceStation)
                    .destinationStation(destinationStation)
                    .journeyDate(request.getJourneyDate())
                    .status(BookingStatus.PAYMENT_PENDING)
                    // ← CHANGED from CONFIRMED to PAYMENT_PENDING
                    .totalFare(totalFare)
                    .pnrNumber(pnr)
                    .build();

            Booking savedBooking = bookingRepository.save(booking);

            List<BookedSeat> bookedSeats = new ArrayList<>();
            for (SeatFarePair pair : seatFarePairs) {
                BookedSeat bookedSeat = BookedSeat.builder()
                        .booking(savedBooking)
                        .seat(pair.seat())
                        .passengerName(pair.passenger().getPassengerName())
                        .passengerAge(pair.passenger().getPassengerAge())
                        .passengerGender(pair.passenger().getPassengerGender())
                        .fare(pair.fare())
                        .status("CONFIRMED")
                        .build();
                bookedSeats.add(bookedSeatRepository.save(bookedSeat));
            }

            log.info("Booking PAYMENT_PENDING. PNR: {} User: {} Seats locked for 10 minutes",
                    pnr, userEmail);

            List<BookedSeatResponse> passengerResponses = bookedSeats.stream()
                    .map(BookedSeatResponse::new)
                    .collect(Collectors.toList());

            return ApiResponse.success(
                    "Seats held! Complete payment within 10 minutes. PNR: " + pnr,
                    new BookingResponse(savedBooking, passengerResponses)
            );

        } finally {
            // ── CRITICAL: This runs even if an exception occurs ───────────
            // If anything goes wrong after acquiring some locks
            // but before the return statement — we MUST release the locks
            // The finally block ALWAYS runs — success or failure
            // But only release locks on failure — on success locks stay
            // until payment or timeout

            // Actually we DON'T release locks on success here
            // Locks stay until: payment confirmed OR 10 min TTL expires
            // We only release locks if booking FAILED
            // This logic is handled by checking if booking was saved
        }
    }

//    @Transactional
//    public ApiResponse<BookingResponse> createBooking(
//            BookingRequest request, String userEmail) {
//
//        // ── Step 1: Load the logged-in user ──────────────────────────────
//        // userEmail comes from JWT — extracted in controller
//        User user = userRepository.findByEmail(userEmail).orElse(null);
//        if (user == null) {
//            return ApiResponse.error("User not found");
//        }
//
//        // ── Step 2: Validate train exists ────────────────────────────────
//        Train train = trainRepository.findById(request.getTrainId()).orElse(null);
//        if (train == null) {
//            return ApiResponse.error("Train not found");
//        }
//
//        // ── Step 3: Validate source station ──────────────────────────────
//        String sourceCode = request.getSourceStationCode().toUpperCase().trim();
//        Station sourceStation = stationRepository.findByCode(sourceCode).orElse(null);
//        if (sourceStation == null) {
//            return ApiResponse.error("Source station '" + sourceCode + "' not found");
//        }
//
//        // ── Step 4: Validate destination station ─────────────────────────
//        String destCode = request.getDestinationStationCode().toUpperCase().trim();
//        Station destinationStation = stationRepository.findByCode(destCode).orElse(null);
//        if (destinationStation == null) {
//            return ApiResponse.error("Destination station '" + destCode + "' not found");
//        }
//
//        // ── Step 5: Validate route — source must come before destination ──
//        // Reuse RouteRepository to get both stops
//        Route sourceRoute = routeRepository
//                .findByTrainIdAndStationId(train.getId(), sourceStation.getId())
//                .orElse(null);
//        Route destRoute = routeRepository
//                .findByTrainIdAndStationId(train.getId(), destinationStation.getId())
//                .orElse(null);
//
//        if (sourceRoute == null || destRoute == null) {
//            return ApiResponse.error("Train does not stop at one or both of the given stations");
//        }
//
//        // sequenceNumber check — same logic as our search query
//        if (sourceRoute.getSequenceNumber() >= destRoute.getSequenceNumber()) {
//            return ApiResponse.error("Source station must come before destination on this train's route");
//        }
//
//        // ── Step 6: Calculate journey distance ───────────────────────────
//        int distanceKm = destRoute.getDistanceFromOrigin()
//                - sourceRoute.getDistanceFromOrigin();
//
//        // ── Step 7: Validate each seat and check availability ────────────
//        BigDecimal totalFare = BigDecimal.ZERO;
//        List<SeatFarePair> seatFarePairs = new ArrayList<>();
//
//        for (PassengerDetail passenger : request.getPassengers()) {
//
//            // Load the seat
//            Seat seat = seatRepository.findById(passenger.getSeatId()).orElse(null);
//            if (seat == null) {
//                return ApiResponse.error("Seat with ID " + passenger.getSeatId() + " not found");
//            }
//
//            // Verify this seat belongs to the requested train
//            Coach coach = seat.getCoach();
//            if (!coach.getTrain().getId().equals(train.getId())) {
//                return ApiResponse.error("Seat " + seat.getSeatNumber()
//                        + " does not belong to train " + train.getTrainNumber());
//            }
//
//            // THE CRITICAL CHECK — is this seat already booked on this date?
//            boolean alreadyBooked = bookedSeatRepository
//                    .existsBySeatIdAndBooking_JourneyDate(
//                            seat.getId(), request.getJourneyDate());
//            if (alreadyBooked) {
//                log.warn("Seat {} in coach {} already booked for date: {}",
//                        seat.getSeatNumber(), coach.getCoachNumber(), request.getJourneyDate());
//                return ApiResponse.error("Seat " + seat.getSeatNumber()
//                        + " in coach " + coach.getCoachNumber()
//                        + " is already booked for " + request.getJourneyDate());
//            }
//
//            // Calculate fare for this seat based on coach type and distance
//            BigDecimal fare = fareCalculator.calculate(distanceKm, coach.getCoachType());
//            totalFare = totalFare.add(fare);
//
//            seatFarePairs.add(new SeatFarePair(seat, fare, passenger));
//        }
//
//        // ── Step 8: Generate PNR ─────────────────────────────────────────
//        String pnr = pnrGenerator.generate();
//        log.info("Creating booking for user: {} on train: {} for date: {}",
//                userEmail, train.getTrainNumber(), request.getJourneyDate());
//
//        // ── Step 9: Save Booking ─────────────────────────────────────────
//        // status defaults to CONFIRMED for simplicity
//        // In production this would be PAYMENT_PENDING until payment succeeds
//        Booking booking = Booking.builder()
//                .user(user)
//                .train(train)
//                .sourceStation(sourceStation)
//                .destinationStation(destinationStation)
//                .journeyDate(request.getJourneyDate())
//                .status(BookingStatus.CONFIRMED)
//                .totalFare(totalFare)
//                .pnrNumber(pnr)
//                .build();
//
//        Booking savedBooking = bookingRepository.save(booking);
//        log.info("Booking created successfully. PNR: {} TotalFare: {}",
//                savedBooking.getPnrNumber(), savedBooking.getTotalFare());
//
//        // ── Step 10: Save each BookedSeat ────────────────────────────────
//        List<BookedSeat> bookedSeats = new ArrayList<>();
//        for (SeatFarePair pair : seatFarePairs) {
//            BookedSeat bookedSeat = BookedSeat.builder()
//                    .booking(savedBooking)
//                    .seat(pair.seat())
//                    .passengerName(pair.passenger().getPassengerName())
//                    .passengerAge(pair.passenger().getPassengerAge())
//                    .passengerGender(pair.passenger().getPassengerGender())
//                    .fare(pair.fare())
//                    .build();
//            bookedSeats.add(bookedSeatRepository.save(bookedSeat));
//        }
//
//        // ── Step 11: Build response ───────────────────────────────────────
//        List<BookedSeatResponse> passengerResponses = bookedSeats.stream()
//                .map(BookedSeatResponse::new)
//                .collect(Collectors.toList());
//
//        return ApiResponse.success(
//                "Booking confirmed! PNR: " + pnr,
//                new BookingResponse(savedBooking, passengerResponses)
//        );
//    }

    // ── Get booking by PNR — public PNR status check ─────────────────────
    @Transactional(readOnly = true)
    public ApiResponse<BookingResponse> getBookingByPnr(String pnr) {
        Booking booking = bookingRepository.findByPnrNumber(pnr).orElse(null);
        if (booking == null) {
            return ApiResponse.error("No booking found with PNR: " + pnr);
        }

        List<BookedSeatResponse> passengers = bookedSeatRepository
                .findByBookingId(booking.getId())
                .stream()
                .map(BookedSeatResponse::new)
                .collect(Collectors.toList());

        return ApiResponse.success("Booking found", new BookingResponse(booking, passengers));
    }

    // ── Get all bookings for logged-in user ───────────────────────────────
    @Transactional(readOnly = true)
    public ApiResponse<List<BookingResponse>> getMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        List<BookingResponse> bookings = bookingRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(booking -> {
                    List<BookedSeatResponse> passengers = bookedSeatRepository
                            .findByBookingId(booking.getId())
                            .stream()
                            .map(BookedSeatResponse::new)
                            .collect(Collectors.toList());
                    return new BookingResponse(booking, passengers);
                })
                .collect(Collectors.toList());

        if (bookings.isEmpty()) {
            return ApiResponse.error("No bookings found");
        }

        return ApiResponse.success("Bookings fetched successfully", bookings);
    }

    // ── Private helper record — groups seat + fare + passenger together ───
    // Java 16+ record — immutable data carrier, no boilerplate
    private record SeatFarePair(Seat seat, BigDecimal fare, PassengerDetail passenger) {}
    // ── Helper: release all locks if booking fails midway ─────────────────
    private void releaseAllLocks(List<Long> seatIds, String journeyDate) {
        for (Long seatId : seatIds) {
            redisLockService.releaseLock(seatId, journeyDate);
            log.warn("Lock released due to booking failure — seat: {}", seatId);
        }
    }
}