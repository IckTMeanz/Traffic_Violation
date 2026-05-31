package vn.icktmeanz.trafficViolation.service.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.icktmeanz.trafficViolation.dto.response.StatisticsMonthlyResponse;
import vn.icktmeanz.trafficViolation.entity.StatisticsMonthly;
import vn.icktmeanz.trafficViolation.entity.UploadSession;
import vn.icktmeanz.trafficViolation.repository.StatisticsMonthlyRepository;
import vn.icktmeanz.trafficViolation.repository.UploadSessionRepository;
import vn.icktmeanz.trafficViolation.service.StatisticsService;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatisticsServiceImpl implements StatisticsService {
    private final StatisticsMonthlyRepository statisticsRepository;
    private final UploadSessionRepository uploadSessionRepository;

    @Override
    @Transactional(readOnly = true)
    public StatisticsMonthlyResponse getCurrentMonthStatistics() {
        log.info("Fetching current month statistics");
        StatisticsMonthly stats = statisticsRepository.findCurrentMonthStatistics()
                .orElseThrow(() -> new IllegalArgumentException("No statistics found for current month"));
        return mapToResponse(stats);
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsMonthlyResponse getStatisticsByMonthYear(Integer month, Integer year) {
        log.info("Fetching statistics for {}/{}", month, year);
        StatisticsMonthly stats = statisticsRepository.findByStatisticMonthAndStatisticYear(month, year)
                .orElseThrow(() -> new IllegalArgumentException("No statistics found for " + month + "/" + year));
        return mapToResponse(stats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatisticsMonthlyResponse> getAllStatistics() {
        log.info("Fetching all statistics");
        return statisticsRepository.findAllByOrderByStatisticYearDescStatisticMonthDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatisticsMonthlyResponse> getStatisticsForYear(Integer year) {
        log.info("Fetching statistics for year: {}", year);
        return statisticsRepository.findByStatisticYearOrderByStatisticMonthDesc(year)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StatisticsMonthlyResponse calculateAndUpdateStatistics(Integer month, Integer year) {
        log.info("Calculating statistics for {}/{}", month, year);

        // Get or create statistics record
        StatisticsMonthly stats = statisticsRepository
                .findByStatisticMonthAndStatisticYear(month, year)
                .orElseGet(() -> StatisticsMonthly.builder()
                        .statisticMonth(month)
                        .statisticYear(year)
                        .build());

        // Get all upload sessions for this month
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        List<UploadSession> sessions = uploadSessionRepository.findAll()
                .stream()
                .filter(session -> session.getCreatedAt() != null &&
                        session.getCreatedAt().isAfter(startOfMonth) &&
                        session.getCreatedAt().isBefore(endOfMonth.plusSeconds(1)))
                .collect(Collectors.toList());

        // Calculate statistics
        int totalReports = sessions.size();
        int[] counters = {0, 0, 0}; // {noHelmet, usingPhone, tripleRiding}
        int approvedReports = 0;
        int rejectedReports = 0;

        // Count violations (simplified - in real scenario would query detected_violations)
        for (UploadSession session : sessions) {
            // This is a basic calculation - in production, query actual violations
            if (session.getMediaFiles() != null) {
                session.getMediaFiles().forEach(mediaFile -> {
                    if (mediaFile.getDetectedViolations() != null) {
                        mediaFile.getDetectedViolations().forEach(violation -> {
                            if (violation.getViolationTypes() != null) {
                                if (violation.getViolationTypes().contains("NO_HELMET")) {
                                    counters[0]++;
                                }
                                if (violation.getViolationTypes().contains("USING_PHONE")) {
                                    counters[1]++;
                                }
                                if (violation.getViolationTypes().contains("TRIPLE_RIDING")) {
                                    counters[2]++;
                                }
                            }
                        });
                    }
                });
            }
        }

        // Update statistics
        stats.setTotalReports(totalReports);
        stats.setTotalViolations(counters[0] + counters[1] + counters[2]);
        stats.setNoHelmetCount(counters[0]);
        stats.setUsingPhoneCount(counters[1]);
        stats.setTripleRidingCount(counters[2]);
        stats.setApprovedReports(approvedReports);
        stats.setRejectedReports(rejectedReports);
        stats.setCreatedAt(LocalDateTime.now());

        StatisticsMonthly saved = statisticsRepository.save(stats);
        log.info("Statistics calculated and saved for {}/{}", month, year);

        return mapToResponse(saved);
    }

    /**
     * Convert StatisticsMonthly entity to response DTO
     */
    private StatisticsMonthlyResponse mapToResponse(StatisticsMonthly stats) {
        return StatisticsMonthlyResponse.builder()
                .id(stats.getId())
                .month(stats.getStatisticMonth())
                .year(stats.getStatisticYear())
                .totalReports(stats.getTotalReports() != null ? stats.getTotalReports() : 0)
                .totalViolations(stats.getTotalViolations() != null ? stats.getTotalViolations() : 0)
                .noHelmetCount(stats.getNoHelmetCount() != null ? stats.getNoHelmetCount() : 0)
                .usingPhoneCount(stats.getUsingPhoneCount() != null ? stats.getUsingPhoneCount() : 0)
                .tripleRidingCount(stats.getTripleRidingCount() != null ? stats.getTripleRidingCount() : 0)
                .approvedReports(stats.getApprovedReports() != null ? stats.getApprovedReports() : 0)
                .rejectedReports(stats.getRejectedReports() != null ? stats.getRejectedReports() : 0)
                .build();
    }
}
