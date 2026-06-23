package com.irctc.user.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    // Both fields optional — user may want to update only name or only phone
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(min = 10, max = 15, message = "Invalid phone number")
    private String phone;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}