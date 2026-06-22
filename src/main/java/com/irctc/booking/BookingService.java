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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public BookingService(BookingRepository bookingRepository,
                          BookedSeatRepository bookedSeatRepository,
                          TrainRepository trainRepository,
                          StationRepository stationRepository,
                          SeatRepository seatRepository,
                          RouteRepository routeRepository,
                          UserRepository userRepository,
                          FareCalculator fareCalculator,
                          PNRGenerator pnrGenerator) {
        this.bookingRepository = bookingRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.seatRepository = seatRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.fareCalculator = fareCalculator;
        this.pnrGenerator = pnrGenerator;
    }

    @Transactional
    public ApiResponse<BookingResponse> createBooking(
            BookingRequest request, String userEmail) {

        // ── Step 1: Load the logged-in user ──────────────────────────────
        // userEmail comes from JWT — extracted in controller
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        // ── Step 2: Validate train exists ────────────────────────────────
        Train train = trainRepository.findById(request.getTrainId()).orElse(null);
        if (train == null) {
            return ApiResponse.error("Train not found");
        }

        // ── Step 3: Validate source station ──────────────────────────────
        String sourceCode = request.getSourceStationCode().toUpperCase().trim();
        Station sourceStation = stationRepository.findByCode(sourceCode).orElse(null);
        if (sourceStation == null) {
            return ApiResponse.error("Source station '" + sourceCode + "' not found");
        }

        // ── Step 4: Validate destination station ─────────────────────────
        String destCode = request.getDestinationStationCode().toUpperCase().trim();
        Station destinationStation = stationRepository.findByCode(destCode).orElse(null);
        if (destinationStation == null) {
            return ApiResponse.error("Destination station '" + destCode + "' not found");
        }

        // ── Step 5: Validate route — source must come before destination ──
        // Reuse RouteRepository to get both stops
        Route sourceRoute = routeRepository
                .findByTrainIdAndStationId(train.getId(), sourceStation.getId())
                .orElse(null);
        Route destRoute = routeRepository
                .findByTrainIdAndStationId(train.getId(), destinationStation.getId())
                .orElse(null);

        if (sourceRoute == null || destRoute == null) {
            return ApiResponse.error("Train does not stop at one or both of the given stations");
        }

        // sequenceNumber check — same logic as our search query
        if (sourceRoute.getSequenceNumber() >= destRoute.getSequenceNumber()) {
            return ApiResponse.error("Source station must come before destination on this train's route");
        }

        // ── Step 6: Calculate journey distance ───────────────────────────
        int distanceKm = destRoute.getDistanceFromOrigin()
                - sourceRoute.getDistanceFromOrigin();

        // ── Step 7: Validate each seat and check availability ────────────
        BigDecimal totalFare = BigDecimal.ZERO;
        List<SeatFarePair> seatFarePairs = new ArrayList<>();

        for (PassengerDetail passenger : request.getPassengers()) {

            // Load the seat
            Seat seat = seatRepository.findById(passenger.getSeatId()).orElse(null);
            if (seat == null) {
                return ApiResponse.error("Seat with ID " + passenger.getSeatId() + " not found");
            }

            // Verify this seat belongs to the requested train
            Coach coach = seat.getCoach();
            if (!coach.getTrain().getId().equals(train.getId())) {
                return ApiResponse.error("Seat " + seat.getSeatNumber()
                        + " does not belong to train " + train.getTrainNumber());
            }

            // THE CRITICAL CHECK — is this seat already booked on this date?
            boolean alreadyBooked = bookedSeatRepository
                    .existsBySeatIdAndBooking_JourneyDate(
                            seat.getId(), request.getJourneyDate());
            if (alreadyBooked) {
                return ApiResponse.error("Seat " + seat.getSeatNumber()
                        + " in coach " + coach.getCoachNumber()
                        + " is already booked for " + request.getJourneyDate());
            }

            // Calculate fare for this seat based on coach type and distance
            BigDecimal fare = fareCalculator.calculate(distanceKm, coach.getCoachType());
            totalFare = totalFare.add(fare);

            seatFarePairs.add(new SeatFarePair(seat, fare, passenger));
        }

        // ── Step 8: Generate PNR ─────────────────────────────────────────
        String pnr = pnrGenerator.generate();

        // ── Step 9: Save Booking ─────────────────────────────────────────
        // status defaults to CONFIRMED for simplicity
        // In production this would be PAYMENT_PENDING until payment succeeds
        Booking booking = Booking.builder()
                .user(user)
                .train(train)
                .sourceStation(sourceStation)
                .destinationStation(destinationStation)
                .journeyDate(request.getJourneyDate())
                .status(BookingStatus.CONFIRMED)
                .totalFare(totalFare)
                .pnrNumber(pnr)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // ── Step 10: Save each BookedSeat ────────────────────────────────
        List<BookedSeat> bookedSeats = new ArrayList<>();
        for (SeatFarePair pair : seatFarePairs) {
            BookedSeat bookedSeat = BookedSeat.builder()
                    .booking(savedBooking)
                    .seat(pair.seat())
                    .passengerName(pair.passenger().getPassengerName())
                    .passengerAge(pair.passenger().getPassengerAge())
                    .passengerGender(pair.passenger().getPassengerGender())
                    .fare(pair.fare())
                    .build();
            bookedSeats.add(bookedSeatRepository.save(bookedSeat));
        }

        // ── Step 11: Build response ───────────────────────────────────────
        List<BookedSeatResponse> passengerResponses = bookedSeats.stream()
                .map(BookedSeatResponse::new)
                .collect(Collectors.toList());

        return ApiResponse.success(
                "Booking confirmed! PNR: " + pnr,
                new BookingResponse(savedBooking, passengerResponses)
        );
    }

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
}