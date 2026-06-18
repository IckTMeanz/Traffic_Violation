package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.icktmeanz.trafficViolation.dto.MonthlyStatisticProjection;
import vn.icktmeanz.trafficViolation.dto.response.StatisticsMonthlyDTO;
import vn.icktmeanz.trafficViolation.entity.DetectedViolation;
import vn.icktmeanz.trafficViolation.entity.MediaFile;

import java.util.List;

public interface DetectedViolationRepository extends JpaRepository<DetectedViolation, Long> {
    List<DetectedViolation> findAllByMediaFile(MediaFile mediaFile);

    List<DetectedViolation> findAllByMediaFile_Id(Long mediaFileId);

    @Query(value = "SELECT unnest(dv.violation_types) AS violationName, COUNT(*) AS violationCount " +
            "FROM detected_violations dv " +
            "JOIN media_files mf ON dv.media_id = mf.id " +
            "JOIN upload_sessions us ON mf.session_id = us.id " +
            "WHERE EXTRACT(MONTH FROM us.created_at) = :month " +
            "  AND EXTRACT(YEAR FROM us.created_at) = :year " +
            "GROUP BY violationName", nativeQuery = true)
    List<MonthlyStatisticProjection> getViolationStatsByMonth(@Param("month") int month, @Param("year") int year);
}
