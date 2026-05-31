package vn.icktmeanz.trafficViolation.service;

import vn.icktmeanz.trafficViolation.dto.response.StatisticsMonthlyResponse;

import java.util.List;

public interface StatisticsService {
    /**
     * Get statistics for current month
     */
    StatisticsMonthlyResponse getCurrentMonthStatistics();

    /**
     * Get statistics by month and year
     */
    StatisticsMonthlyResponse getStatisticsByMonthYear(Integer month, Integer year);

    /**
     * Get all statistics ordered by year and month (DESC)
     */
    List<StatisticsMonthlyResponse> getAllStatistics();

    /**
     * Get statistics for a specific year
     */
    List<StatisticsMonthlyResponse> getStatisticsForYear(Integer year);

    /**
     * Calculate and update monthly statistics
     */
    StatisticsMonthlyResponse calculateAndUpdateStatistics(Integer month, Integer year);
}
