package vn.icktmeanz.trafficViolation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsMonthlyResponse {
    private Long id;
    private Integer month;
    private Integer year;
    private Integer totalReports;
    private Integer totalViolations;
    private Integer noHelmetCount;
    private Integer usingPhoneCount;
    private Integer tripleRidingCount;
    private Integer approvedReports;
    private Integer rejectedReports;
}
