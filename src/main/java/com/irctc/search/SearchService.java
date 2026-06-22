package com.irctc.search;

import com.irctc.common.response.ApiResponse;
import com.irctc.route.Route;
import com.irctc.search.dto.SearchRequest;
import com.irctc.search.dto.TrainSearchResult;
import com.irctc.station.StationRepository;
import com.irctc.train.Train;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final SearchRepository searchRepository;
    private final StationRepository stationRepository;

    public SearchService(SearchRepository searchRepository,
                         StationRepository stationRepository) {
        this.searchRepository = searchRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TrainSearchResult>> searchTrains(SearchRequest request) {

        // Normalize — always uppercase, always trimmed
        String sourceCode = request.getSourceCode().toUpperCase().trim();
        String destinationCode = request.getDestinationCode().toUpperCase().trim();

        // Business rule: source and destination must be different
        if (sourceCode.equals(destinationCode)) {
            return ApiResponse.error("Source and destination stations cannot be the same");
        }

        // Validate source station exists
        boolean sourceExists = stationRepository.existsByCode(sourceCode);
        if (!sourceExists) {
            return ApiResponse.error("Source station '" + sourceCode + "' not found");
        }

        // Validate destination station exists
        boolean destinationExists = stationRepository.existsByCode(destinationCode);
        if (!destinationExists) {
            return ApiResponse.error("Destination station '" + destinationCode + "' not found");
        }

        // Run the core search query
        List<Train> trains = searchRepository.findTrainsBetweenStations(
                sourceCode, destinationCode);

        if (trains.isEmpty()) {
            return ApiResponse.error("No trains found from " + sourceCode + " to " + destinationCode);
        }

        // For each train found, enrich the result with stop details
        // This is where the N+1 problem lives — we discuss it below
        List<TrainSearchResult> results = new ArrayList<>();

        for (Train train : trains) {

            // Get the source stop for this train
            Route sourceStop = searchRepository
                    .findStopByTrainAndStation(train.getId(), sourceCode)
                    .orElse(null);

            // Get the destination stop for this train
            Route destinationStop = searchRepository
                    .findStopByTrainAndStation(train.getId(), destinationCode)
                    .orElse(null);

            // Both stops must exist (they should since our main query found them)
            if (sourceStop != null && destinationStop != null) {
                results.add(new TrainSearchResult(train, sourceStop, destinationStop));
            }
        }

        return ApiResponse.success(
                "Found " + results.size() + " train(s) from " + sourceCode + " to " + destinationCode,
                results
        );
    }
}