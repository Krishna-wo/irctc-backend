package com.irctc.train;

import com.irctc.common.response.ApiResponse;
import com.irctc.train.dto.TrainRequest;
import com.irctc.train.dto.TrainResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainService {

    private final TrainRepository trainRepository;

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    @Transactional
    public ApiResponse<TrainResponse> addTrain(TrainRequest request) {

        // Business rule: train numbers must be unique system-wide
        if (trainRepository.existsByTrainNumber(request.getTrainNumber())) {
            return ApiResponse.error("Train with number " + request.getTrainNumber() + " already exists");
        }

        // Map request → entity using ACTUAL entity field names: name, type, totalDistanceKm
        Train train = new Train();
        train.setTrainNumber(request.getTrainNumber().trim());
        train.setName(request.getName().trim());
        train.setType(request.getType());
        train.setTotalDistanceKm(request.getTotalDistanceKm());

        Train saved = trainRepository.save(train);
        return ApiResponse.success("Train added successfully", new TrainResponse(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TrainResponse>> getAllTrains() {
        List<TrainResponse> trains = trainRepository.findAll()
                .stream()
                .map(TrainResponse::new)
                .collect(Collectors.toList());
        return ApiResponse.success("Trains fetched successfully", trains);
    }

    @Transactional(readOnly = true)
    public ApiResponse<TrainResponse> getTrainByNumber(String trainNumber) {
        Train train = trainRepository.findByTrainNumber(trainNumber).orElse(null);
        if (train == null) {
            return ApiResponse.error("Train " + trainNumber + " not found");
        }
        return ApiResponse.success("Train found", new TrainResponse(train));
    }
}
