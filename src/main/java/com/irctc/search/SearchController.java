package com.irctc.search;

import com.irctc.common.response.ApiResponse;
import com.irctc.search.dto.SearchRequest;
import com.irctc.search.dto.TrainSearchResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/search")
// NOTE: /api/search — NOT /api/admin/search
// Search is a PUBLIC endpoint — no login required
// Any user (even unauthenticated) can search for trains
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // POST /api/search/trains
    // Why POST and not GET?
    // GET requests shouldn't have a body — some clients strip it
    // Our search has parameters that will grow (date, class, quota)
    // POST keeps it clean and extensible
    @PostMapping("/trains")
    public ResponseEntity<ApiResponse<List<TrainSearchResult>>> searchTrains(
            @Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.searchTrains(request));
    }
}