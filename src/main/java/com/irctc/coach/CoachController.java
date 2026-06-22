package com.irctc.coach;

import com.irctc.coach.dto.CoachRequest;
import com.irctc.coach.dto.CoachResponse;
import com.irctc.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coaches")
// /api/admin/** → ADMIN only via SecurityConfig
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    // POST /api/admin/coaches
    @PostMapping
    public ResponseEntity<ApiResponse<CoachResponse>> addCoach(
            @Valid @RequestBody CoachRequest request) {
        ApiResponse<CoachResponse> response = coachService.addCoach(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/admin/coaches/train/1
    @GetMapping("/train/{trainId}")
    public ResponseEntity<ApiResponse<List<CoachResponse>>> getCoachesByTrain(
            @PathVariable Long trainId) {
        ApiResponse<List<CoachResponse>> response = coachService.getCoachesByTrain(trainId);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }
}