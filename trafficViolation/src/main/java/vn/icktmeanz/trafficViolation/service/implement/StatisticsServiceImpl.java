package vn.icktmeanz.trafficViolation.service.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.icktmeanz.trafficViolation.dto.MonthlyStatisticProjection;
import vn.icktmeanz.trafficViolation.dto.response.StatisticsMonthlyDTO;
import vn.icktmeanz.trafficViolation.entity.StatisticsMonthly;
import vn.icktmeanz.trafficViolation.entity.UploadSession;
import vn.icktmeanz.trafficViolation.repository.DetectedViolationRepository;
import vn.icktmeanz.trafficViolation.repository.StatisticsMonthlyRepository;
import vn.icktmeanz.trafficViolation.repository.UploadSessionRepository;
import vn.icktmeanz.trafficViolation.service.StatisticsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private DetectedViolationRepository repository;
    @Override
    public List<StatisticsMonthlyDTO> getMonthlyStats(int month, int year) {
        List<MonthlyStatisticProjection> rawData = repository.getViolationStatsByMonth(month, year);

        // Map từ danh sách Interface sang danh sách DTO để trả về cho Frontend
        return rawData.stream()
                .map(StatisticsMonthlyDTO::new)
                .collect(Collectors.toList());
    }
}
