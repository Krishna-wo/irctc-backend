package com.irctc.search;

import com.irctc.common.config.CacheKeys;
import com.irctc.common.response.ApiResponse;
import com.irctc.search.dto.SearchRequest;
import com.irctc.search.dto.TrainSearchResult;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;
    private final RedisTemplate<String, Object> redisTemplate;

    public SearchController(SearchService searchService,
                            RedisTemplate<String, Object> redisTemplate) {
        this.searchService = searchService;
        this.redisTemplate = redisTemplate;
    }

    // POST /api/search/trains — same as before
    @PostMapping("/trains")
    public ResponseEntity<ApiResponse<List<TrainSearchResult>>> searchTrains(
            @Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.searchTrains(request));
    }

    // DELETE /api/admin/search/cache — admin clears search cache
    // Called when new train or route is added so stale data is removed
    @DeleteMapping("/api/admin/search/cache")
    public ResponseEntity<ApiResponse<Void>> clearSearchCache() {

        // Find all keys matching "search:*" pattern
        Set<String> keys = redisTemplate.keys(CacheKeys.SEARCH_PREFIX + "*");
        // KEYS search:* returns all search cache keys
        // Example: ["search:NDLS:BCT", "search:BCT:ALD", "search:NDLS:ALD"]

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Search cache cleared. {} keys deleted", keys.size());
            return ResponseEntity.ok(
                    ApiResponse.success("Search cache cleared. " + keys.size() + " keys deleted", null));
        }

        return ResponseEntity.ok(ApiResponse.success("Cache was already empty", null));
    }
}