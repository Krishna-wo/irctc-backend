package com.irctc.train;

import com.irctc.common.response.ApiResponse;
import com.irctc.train.dto.TrainRequest;
import com.irctc.train.dto.TrainResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/trains")
// /api/admin/** → SecurityConfig enforces ADMIN role automatically
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    // POST /api/admin/trains
    @PostMapping
    public ResponseEntity<ApiResponse<TrainResponse>> addTrain(
            @Valid @RequestBody TrainRequest request) {
        ApiResponse<TrainResponse> response = trainService.addTrain(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/admin/trains
    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainResponse>>> getAllTrains() {
        return ResponseEntity.ok(trainService.getAllTrains());
    }

    // GET /api/admin/trains/12951
    @GetMapping("/{trainNumber}")
    public ResponseEntity<ApiResponse<TrainResponse>> getTrainByNumber(
            @PathVariable String trainNumber) {
        ApiResponse<TrainResponse> response = trainService.getTrainByNumber(trainNumber);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
