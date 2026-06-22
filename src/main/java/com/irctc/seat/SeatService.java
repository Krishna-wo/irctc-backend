package com.irctc.seat;

import com.irctc.coach.Coach;
import com.irctc.coach.CoachRepository;
import com.irctc.common.response.ApiResponse;
import com.irctc.seat.dto.SeatRequest;
import com.irctc.seat.dto.SeatResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final CoachRepository coachRepository;

    public SeatService(SeatRepository seatRepository,
                       CoachRepository coachRepository) {
        this.seatRepository = seatRepository;
        this.coachRepository = coachRepository;
    }

    @Transactional
    public ApiResponse<SeatResponse> addSeat(SeatRequest request) {

        // Step 1: Validate coach exists
        Coach coach = coachRepository.findById(request.getCoachId()).orElse(null);
        if (coach == null) {
            return ApiResponse.error("Coach with ID " + request.getCoachId() + " not found");
        }

        // Step 2: Check unique constraint (coach_id, seat_number)
        String seatNumber = request.getSeatNumber().trim();
        if (seatRepository.existsByCoachIdAndSeatNumber(coach.getId(), seatNumber)) {
            return ApiResponse.error("Seat " + seatNumber
                    + " already exists in coach " + coach.getCoachNumber());
        }

        // Step 3: Business rule — can't exceed coach's totalSeats capacity
        long currentSeatCount = seatRepository.countByCoachId(coach.getId());
        if (currentSeatCount >= coach.getTotalSeats()) {
            return ApiResponse.error("Coach " + coach.getCoachNumber()
                    + " is already full. Total capacity: " + coach.getTotalSeats());
        }

        // Step 4: Build entity using ACTUAL field names: coach, seatNumber, seatType
        Seat seat = Seat.builder()
                .coach(coach)
                .seatNumber(seatNumber)
                .seatType(request.getSeatType())
                .build();

        Seat saved = seatRepository.save(seat);
        return ApiResponse.success("Seat added successfully", new SeatResponse(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SeatResponse>> getSeatsByCoach(Long coachId) {

        if (!coachRepository.existsById(coachId)) {
            return ApiResponse.error("Coach with ID " + coachId + " not found");
        }

        List<SeatResponse> seats = seatRepository.findByCoachId(coachId)
                .stream()
                .map(SeatResponse::new)
                .collect(Collectors.toList());

        if (seats.isEmpty()) {
            return ApiResponse.error("No seats found for this coach");
        }

        return ApiResponse.success("Seats fetched successfully", seats);
    }
}