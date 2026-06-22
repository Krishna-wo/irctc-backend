package com.irctc.booking;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
// Single responsibility: generate a unique PNR string
// Extracted so the generation strategy can change without touching BookingService
public class PNRGenerator {

    public String generate() {
        // UUID gives us a universally unique identifier
        // Example UUID: "a3f2b1c9-4d5e-6f7a-8b9c-0d1e2f3a4b5c"
        // We take first 8 chars and uppercase: "A3F2B1C9"
        // Collision probability is astronomically low for our scale
        return UUID.randomUUID()
                .toString()
                .replace("-", "")  // remove dashes
                .substring(0, 10)  // take first 10 chars
                .toUpperCase();    // "A3F2B1C9D0"
    }
}