package vn.icktmeanz.trafficViolation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.icktmeanz.trafficViolation.dto.MonthlyStatisticProjection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsMonthlyDTO {
    private String violationName;
    private Long violationCount;

    // Constructor chuyển đổi từ Interface sang Class DTO nhanh
    public StatisticsMonthlyDTO(MonthlyStatisticProjection projection) {
        this.violationName = projection.getViolationName();
        this.violationCount = projection.getViolationCount();
    }

    public String getViolationName() { return violationName; }
    public void setViolationName(String violationName) { this.violationName = violationName; }

    public Long getViolationCount() { return violationCount; }
    public void setViolationCount(Long violationCount) { this.violationCount = violationCount; }
}
