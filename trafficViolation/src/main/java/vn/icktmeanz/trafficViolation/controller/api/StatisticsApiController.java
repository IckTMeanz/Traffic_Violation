package vn.icktmeanz.trafficViolation.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.icktmeanz.trafficViolation.dto.response.StatisticsMonthlyDTO;
import vn.icktmeanz.trafficViolation.service.StatisticsService;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsApiController {
    private final StatisticsService statisticsService;

    @GetMapping("/monthly")
    public ResponseEntity<List<StatisticsMonthlyDTO>> getMonthlyStats(@RequestParam int month, @RequestParam int year) {
        List<StatisticsMonthlyDTO> monthlyStats = statisticsService.getMonthlyStats(month, year);
        return ResponseEntity.ok(monthlyStats);
    }

}
