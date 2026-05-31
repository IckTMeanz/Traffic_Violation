package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.icktmeanz.trafficViolation.entity.StatisticsMonthly;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticsMonthlyRepository extends JpaRepository<StatisticsMonthly, Long> {
    /**
     * Find statistics by month and year
     */
    Optional<StatisticsMonthly> findByStatisticMonthAndStatisticYear(Integer month, Integer year);

    /**
     * Find statistics for current month
     */
    @Query(value = "SELECT * FROM statistics_monthly ORDER BY statistic_year DESC, statistic_month DESC LIMIT 1", nativeQuery = true)
    Optional<StatisticsMonthly> findCurrentMonthStatistics();

    /**
     * Find all statistics ordered by year and month
     */
    List<StatisticsMonthly> findAllByOrderByStatisticYearDescStatisticMonthDesc();

    /**
     * Find statistics for a specific year
     */
    List<StatisticsMonthly> findByStatisticYearOrderByStatisticMonthDesc(Integer year);
}
