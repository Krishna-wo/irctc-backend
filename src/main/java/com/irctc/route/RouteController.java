package com.irctc.route;

import com.irctc.common.response.ApiResponse;
import com.irctc.route.dto.RouteRequest;
import com.irctc.route.dto.RouteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/routes")
// /api/admin/** → SecurityConfig enforces ADMIN role automatically
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // POST /api/admin/routes — add a stop to a train's route
    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> addRouteStop(
            @Valid @RequestBody RouteRequest request) {
        ApiResponse<RouteResponse> response = routeService.addRouteStop(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/admin/routes/train/1 — get full schedule for a train
    @GetMapping("/train/{trainId}")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getRouteByTrain(
            @PathVariable Long trainId) {
        ApiResponse<List<RouteResponse>> response = routeService.getRouteByTrain(trainId);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
