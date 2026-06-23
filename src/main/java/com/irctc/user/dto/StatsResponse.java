package com.irctc.user.dto;

import java.math.BigDecimal;

public class StatsResponse {

    private Long totalUsers;
    private Long totalBookings;
    private BigDecimal totalRevenue;
    private String mostBookedTrain;

    public StatsResponse(Long totalUsers, Long totalBookings,
                         BigDecimal totalRevenue, String mostBookedTrain) {
        this.totalUsers = totalUsers;
        this.totalBookings = totalBookings;
        this.totalRevenue = totalRevenue;
        this.mostBookedTrain = mostBookedTrain;
    }

    public Long getTotalUsers() { return totalUsers; }
    public Long getTotalBookings() { return totalBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public String getMostBookedTrain() { return mostBookedTrain; }
}