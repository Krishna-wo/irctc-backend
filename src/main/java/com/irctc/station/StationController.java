package com.irctc.station;

import com.irctc.common.response.ApiResponse;
import com.irctc.station.dto.StationRequest;
import com.irctc.station.dto.StationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
// @RestController = @Controller + @ResponseBody
// Every method return value is automatically serialized to JSON

@RequestMapping("/api/admin/stations")
// ALL endpoints in this controller start with /api/admin/stations
// Because it starts with /api/admin/, our SecurityConfig enforces ADMIN role
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    // POST /api/admin/stations — add a new station
    public ResponseEntity<ApiResponse<StationResponse>> addStation(
            @Valid @RequestBody StationRequest request) {
        // @Valid tells Spring to run all the @NotBlank, @Size validations
        // on the StationRequest before this method even executes
        // If validation fails, Spring throws MethodArgumentNotValidException
        // which our GlobalExceptionHandler from Day 4 catches

        // @RequestBody tells Spring: parse the JSON body into StationRequest

        ApiResponse<StationResponse> response = stationService.addStation(request);

        // If the service returned an error, send 400 Bad Request
        // Otherwise send 201 Created (correct HTTP status for resource creation)
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    // GET /api/admin/stations — get all stations
    // Note: In a real system, this would be a PUBLIC endpoint
    // We'll move read endpoints to a public controller in Day 6
    public ResponseEntity<ApiResponse<List<StationResponse>>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/{code}")
    // GET /api/admin/stations/NDLS — get station by code
    // {code} is a path variable — Spring extracts it from the URL
    public ResponseEntity<ApiResponse<StationResponse>> getStationByCode(
            @PathVariable String code) {
        // @PathVariable binds the {code} from URL to this parameter
        ApiResponse<StationResponse> response = stationService.getStationByCode(code);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }
}