package vn.icktmeanz.trafficViolation.service;

import vn.icktmeanz.trafficViolation.dto.MonthlyStatisticProjection;
import vn.icktmeanz.trafficViolation.dto.response.StatisticsMonthlyDTO;

import java.util.List;

public interface StatisticsService {
    public List<StatisticsMonthlyDTO> getMonthlyStats(int month, int year);
}
