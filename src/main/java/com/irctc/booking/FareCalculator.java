package com.irctc.booking;

import com.irctc.coach.CoachType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
// @Component — Spring manages this bean
// Single responsibility: given distance + coach type → return fare
// Extracted into its own class so it can be tested independently
// and changed without touching BookingService
public class FareCalculator {

    // Base fare per km for each coach type
    // Real IRCTC uses a complex dynamic pricing model
    // This is a simplified but realistic version
    private static final BigDecimal SLEEPER_RATE = new BigDecimal("0.50");
    private static final BigDecimal AC_3_TIER_RATE = new BigDecimal("1.20");
    private static final BigDecimal AC_2_TIER_RATE = new BigDecimal("1.80");
    private static final BigDecimal AC_FIRST_CLASS_RATE = new BigDecimal("3.00");

    // Minimum fare regardless of distance
    private static final BigDecimal MINIMUM_FARE = new BigDecimal("50.00");

    public BigDecimal calculate(Integer distanceKm, CoachType coachType) {

        // Get rate for this coach type
        BigDecimal ratePerKm = getRateForCoachType(coachType);

        // fare = distance * rate per km
        BigDecimal fare = new BigDecimal(distanceKm)
                .multiply(ratePerKm)
                .setScale(2, RoundingMode.HALF_UP);
        // setScale(2) — always 2 decimal places, standard for money
        // RoundingMode.HALF_UP — 0.5 rounds up (standard banking rounding)

        // Apply minimum fare
        // Even a 10km journey has a floor price
        if (fare.compareTo(MINIMUM_FARE) < 0) {
            return MINIMUM_FARE;
        }

        return fare;
    }

    private BigDecimal getRateForCoachType(CoachType coachType) {
        return switch (coachType) {
            case SLEEPER -> SLEEPER_RATE;
            case AC_3_TIER -> AC_3_TIER_RATE;
            case AC_2_TIER -> AC_2_TIER_RATE;
            case AC_FIRST_CLASS -> AC_FIRST_CLASS_RATE;
        };
        // switch expression — Java 14+, exhaustive (compiler forces all cases)
        // No default needed — if a new CoachType is added, compiler will warn
    }
}