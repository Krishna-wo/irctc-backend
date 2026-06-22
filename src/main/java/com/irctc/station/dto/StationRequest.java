package com.irctc.station.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StationRequest {

    @NotBlank(message = "Station name is required")
    @Size(max = 100, message = "Station name must be under 100 characters")
    private String name;

    @NotBlank(message = "Station code is required")
    @Size(min = 2, max = 10, message = "Station code must be 2-10 characters")
    private String code;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
