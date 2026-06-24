package com.irctc.search;

import com.irctc.common.config.CacheKeys;
import com.irctc.common.response.ApiResponse;
import com.irctc.route.Route;
import com.irctc.search.dto.SearchRequest;
import com.irctc.search.dto.TrainSearchResult;
import com.irctc.station.StationRepository;
import com.irctc.train.Train;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SearchService {

    private final SearchRepository searchRepository;
    private final StationRepository stationRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    // RedisTemplate injected — same bean we configured in RedisConfig.java

    public SearchService(SearchRepository searchRepository,
                         StationRepository stationRepository,
                         RedisTemplate<String, Object> redisTemplate) {
        this.searchRepository = searchRepository;
        this.stationRepository = stationRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TrainSearchResult>> searchTrains(SearchRequest request) {

        String sourceCode = request.getSourceCode().toUpperCase().trim();
        String destinationCode = request.getDestinationCode().toUpperCase().trim();

        // ── Validation (same as before) ───────────────────────────────────
        if (sourceCode.equals(destinationCode)) {
            return ApiResponse.error("Source and destination cannot be the same");
        }

        if (!stationRepository.existsByCode(sourceCode)) {
            return ApiResponse.error("Source station '" + sourceCode + "' not found");
        }

        if (!stationRepository.existsByCode(destinationCode)) {
            return ApiResponse.error("Destination station '" + destinationCode + "' not found");
        }

        // ── Build cache key ───────────────────────────────────────────────
        String cacheKey = CacheKeys.SEARCH_PREFIX + sourceCode + ":" + destinationCode;
        // Example: "search:NDLS:BCT"

        // ── Start timing ──────────────────────────────────────────────────
        long startTime = System.currentTimeMillis();

        // ── Step 1: Check Redis first ─────────────────────────────────────
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            // opsForValue() = operations for simple key-value pairs
            // .get(cacheKey) = retrieve value for this key

            if (cached != null) {
                // Cache HIT — Redis had the answer
                long timeTaken = System.currentTimeMillis() - startTime;
                log.info("CACHE HIT — key: {} | time: {}ms", cacheKey, timeTaken);
                // This log will show you the Redis speed vs DB speed

                // Cast the cached object back to List<TrainSearchResult>
                @SuppressWarnings("unchecked")
                List<TrainSearchResult> cachedResults = (List<TrainSearchResult>) cached;

                return ApiResponse.success(
                        "Found " + cachedResults.size() + " train(s) — served from cache",
                        cachedResults
                );
            }
        } catch (Exception e) {
            // CRITICAL: if Redis is down, we don't crash — we fall through to DB
            // This is called "cache-aside with fallback" pattern
            // Redis is a speed layer — not a critical dependency
            log.warn("Redis unavailable, falling back to database: {}", e.getMessage());
        }

        // ── Step 2: Cache MISS — query the database ───────────────────────
        log.info("CACHE MISS — key: {} | querying database", cacheKey);

        List<Train> trains = searchRepository.findTrainsBetweenStations(
                sourceCode, destinationCode);

        if (trains.isEmpty()) {
            return ApiResponse.error("No trains found from " + sourceCode + " to " + destinationCode);
        }

        // ── Step 3: Build enriched results (same as before) ───────────────
        List<TrainSearchResult> results = new ArrayList<>();

        for (Train train : trains) {
            Route sourceStop = searchRepository
                    .findStopByTrainAndStation(train.getId(), sourceCode)
                    .orElse(null);
            Route destinationStop = searchRepository
                    .findStopByTrainAndStation(train.getId(), destinationCode)
                    .orElse(null);

            if (sourceStop != null && destinationStop != null) {
                results.add(new TrainSearchResult(train, sourceStop, destinationStop));
            }
        }

        // ── Step 4: Store result in Redis ─────────────────────────────────
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,           // key: "search:NDLS:BCT"
                    results,            // value: List<TrainSearchResult> as JSON
                    CacheKeys.SEARCH_TTL_MINUTES,  // TTL: 10
                    TimeUnit.MINUTES    // TTL unit: minutes
            );
            // After 10 minutes Redis automatically deletes this key
            // Next request after 10 min will hit DB again — fresh data

            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("CACHE STORED — key: {} | time: {}ms | trains: {}",
                    cacheKey, timeTaken, results.size());

        } catch (Exception e) {
            // If storing in Redis fails — still return results to user
            // User never knows Redis had a problem
            log.warn("Failed to store in Redis cache: {}", e.getMessage());
        }

        return ApiResponse.success(
                "Found " + results.size() + " train(s) from " + sourceCode + " to " + destinationCode,
                results
        );
    }
}