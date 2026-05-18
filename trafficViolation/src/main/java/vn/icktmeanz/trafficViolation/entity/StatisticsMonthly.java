package vn.icktmeanz.trafficViolation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "statistics_monthly",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"statistic_month", "statistic_year"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsMonthly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "statistic_month")
    private Integer statisticMonth;

    @Column(name = "statistic_year")
    private Integer statisticYear;

    @Column(name = "total_reports")
    private Integer totalReports;

    @Column(name = "total_violations")
    private Integer totalViolations;

    @Column(name = "no_helmet_count")
    private Integer noHelmetCount;

    @Column(name = "using_phone_count")
    private Integer usingPhoneCount;

    @Column(name = "triple_riding_count")
    private Integer tripleRidingCount;

    @Column(name = "approved_reports")
    private Integer approvedReports;

    @Column(name = "rejected_reports")
    private Integer rejectedReports;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}