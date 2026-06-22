package com.irctc.coach;

import com.irctc.coach.dto.CoachRequest;
import com.irctc.coach.dto.CoachResponse;
import com.irctc.common.response.ApiResponse;
import com.irctc.train.Train;
import com.irctc.train.TrainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoachService {

    private final CoachRepository coachRepository;
    private final TrainRepository trainRepository;

    public CoachService(CoachRepository coachRepository,
                        TrainRepository trainRepository) {
        this.coachRepository = coachRepository;
        this.trainRepository = trainRepository;
    }

    @Transactional
    public ApiResponse<CoachResponse> addCoach(CoachRequest request) {

        // Step 1: Validate train exists
        Train train = trainRepository.findById(request.getTrainId()).orElse(null);
        if (train == null) {
            return ApiResponse.error("Train with ID " + request.getTrainId() + " not found");
        }

        // Step 2: Normalize coach number — always uppercase
        // "s1" and "S1" are the same coach
        String coachNumber = request.getCoachNumber().toUpperCase().trim();

        // Step 3: Check unique constraint (train_id, coach_number)
        // Mirrors the DB constraint — fail fast with clean message
        if (coachRepository.existsByTrainIdAndCoachNumber(train.getId(), coachNumber)) {
            return ApiResponse.error("Coach " + coachNumber
                    + " already exists on train " + train.getTrainNumber());
        }

        // Step 4: Build entity using ACTUAL field names from Coach entity
        Coach coach = Coach.builder()
                .train(train)
                .coachNumber(coachNumber)
                .coachType(request.getCoachType())
                .totalSeats(request.getTotalSeats())
                .build();
        // We use @Builder from Lombok since entity has @Builder annotation

        Coach saved = coachRepository.save(coach);
        return ApiResponse.success("Coach added successfully", new CoachResponse(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<CoachResponse>> getCoachesByTrain(Long trainId) {

        // Validate train exists first
        if (!trainRepository.existsById(trainId)) {
            return ApiResponse.error("Train with ID " + trainId + " not found");
        }

        List<CoachResponse> coaches = coachRepository.findByTrainId(trainId)
                .stream()
                .map(CoachResponse::new)
                .collect(Collectors.toList());

        if (coaches.isEmpty()) {
            return ApiResponse.error("No coaches found for this train");
        }

        return ApiResponse.success("Coaches fetched successfully", coaches);
    }
}