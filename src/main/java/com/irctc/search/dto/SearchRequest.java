package com.irctc.search.dto;

import jakarta.validation.constraints.NotBlank;

public class SearchRequest {

    @NotBlank(message = "Source station code is required")
    private String sourceCode;  // "NDLS"

    @NotBlank(message = "Destination station code is required")
    private String destinationCode;  // "BCT"

    // We'll add date here in Day 8 when we do seat availability
    // For now, search just finds which trains run between stations

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }
}