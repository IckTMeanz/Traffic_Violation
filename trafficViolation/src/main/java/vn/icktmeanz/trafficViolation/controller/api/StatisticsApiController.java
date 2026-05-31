package vn.icktmeanz.trafficViolation.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.icktmeanz.trafficViolation.dto.response.StatisticsMonthlyResponse;
import vn.icktmeanz.trafficViolation.service.StatisticsService;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsApiController {
    private final StatisticsService statisticsService;

    /**
     * Get statistics for current month
     * GET /api/statistics/current
     *
     * @return statistics response for current month
     */
    @GetMapping("/current")
    public ResponseEntity<StatisticsMonthlyResponse> getCurrentMonthStatistics() {
        log.info("Fetching current month statistics");
        try {
            StatisticsMonthlyResponse response = statisticsService.getCurrentMonthStatistics();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Current month statistics not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching current statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics by month and year
     * GET /api/statistics/{month}/{year}
     *
     * @param month month (1-12)
     * @param year year (e.g., 2026)
     * @return statistics response
     */
    @GetMapping("/{month}/{year}")
    public ResponseEntity<StatisticsMonthlyResponse> getStatisticsByMonthYear(
            @PathVariable Integer month,
            @PathVariable Integer year) {
        log.info("Fetching statistics for {}/{}", month, year);
        try {
            StatisticsMonthlyResponse response = statisticsService.getStatisticsByMonthYear(month, year);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Statistics not found for {}/{}", month, year);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all statistics (ordered DESC by year and month)
     * GET /api/statistics/all
     *
     * @return list of all statistics
     */
    @GetMapping("/all")
    public ResponseEntity<List<StatisticsMonthlyResponse>> getAllStatistics() {
        log.info("Fetching all statistics");
        try {
            List<StatisticsMonthlyResponse> responses = statisticsService.getAllStatistics();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching all statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics for a specific year
     * GET /api/statistics/year/{year}
     *
     * @param year year (e.g., 2026)
     * @return list of statistics for that year
     */
    @GetMapping("/year/{year}")
    public ResponseEntity<List<StatisticsMonthlyResponse>> getStatisticsForYear(@PathVariable Integer year) {
        log.info("Fetching statistics for year: {}", year);
        try {
            List<StatisticsMonthlyResponse> responses = statisticsService.getStatisticsForYear(year);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching statistics for year {}: {}", year, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Calculate and update monthly statistics
     * POST /api/statistics/calculate/{month}/{year}
     *
     * @param month month to calculate (1-12)
     * @param year year to calculate (e.g., 2026)
     * @return calculated statistics response
     */
    @PostMapping("/calculate/{month}/{year}")
    public ResponseEntity<StatisticsMonthlyResponse> calculateStatistics(
            @PathVariable Integer month,
            @PathVariable Integer year) {
        log.info("Calculating statistics for {}/{}", month, year);
        try {
            StatisticsMonthlyResponse response = statisticsService.calculateAndUpdateStatistics(month, year);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
