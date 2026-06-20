package com.irctc.station.dto;

import com.irctc.station.Station;

// This is what we SEND BACK to the client
// Notice: no sensitive fields, clean structure
public class StationResponse {

    private Long id;
    private String name;
    private String code;  // "NDLS"
    private String city;  // "New Delhi"
    private String state; // "Delhi"

    // We need a constructor to easily build this from an Entity
    public StationResponse(Station station) {
        this.id = station.getId();
        this.name = station.getName();
        this.code = station.getCode();
        this.city = station.getCity();
        this.state = station.getState();
    }

    // Getters only — response objects are read-only, no setters needed
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getCity() { return city; }
    public String getState() { return state; }
}