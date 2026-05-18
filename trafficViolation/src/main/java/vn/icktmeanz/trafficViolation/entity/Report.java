package vn.icktmeanz.trafficViolation.entity;

import vn.icktmeanz.trafficViolation.constant.MediaType;
import vn.icktmeanz.trafficViolation.constant.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;

    @Column(name = "ai_processing_time")
    private Double aiProcessingTime;

    @Column(name = "total_violations")
    private Integer totalViolations;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL)
    private MediaFile mediaFile;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<DetectedViolation> detectedViolations;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL)
    private AuthorityReview authorityReview;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL)
    private Appeal appeal;
}