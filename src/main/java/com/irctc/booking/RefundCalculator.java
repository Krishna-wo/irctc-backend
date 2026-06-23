package com.irctc.booking;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class RefundCalculator {

    // Based on real IRCTC cancellation policy
    public BigDecimal calculate(BigDecimal originalFare, LocalDate journeyDate) {

        // How many days until the journey from today
        long daysUntilJourney = ChronoUnit.DAYS.between(
                LocalDate.now(), journeyDate);
        // ChronoUnit.DAYS.between(start, end) → number of days from start to end
        // If journey is tomorrow → 1
        // If journey is today → 0
        // If journey already passed → negative

        // Journey already happened — no refund
        if (daysUntilJourney < 0) {
            return BigDecimal.ZERO;
        }

        // Less than 12 hours (same day) — no refund
        if (daysUntilJourney == 0) {
            return BigDecimal.ZERO;
        }

        // Less than 2 days — 50% refund
        if (daysUntilJourney < 2) {
            return originalFare
                    .multiply(new BigDecimal("0.50"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Less than 7 days — 75% refund
        if (daysUntilJourney < 7) {
            return originalFare
                    .multiply(new BigDecimal("0.75"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // More than 7 days — 90% refund
        // 10% is the base cancellation charge
        return originalFare
                .multiply(new BigDecimal("0.90"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}