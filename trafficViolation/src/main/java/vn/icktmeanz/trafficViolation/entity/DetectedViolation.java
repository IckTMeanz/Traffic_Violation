package vn.icktmeanz.trafficViolation.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "detected_violations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectedViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(ListArrayType.class)
    @Column(
            name = "violation_types",
            columnDefinition = "text[]"
    )
    private List<String> violationTypes;

    private Double confidence;

    @Column(columnDefinition = "jsonb")
    private String boundingBox;

    private Integer frameNumber;

    private Integer objectTrackingId;

    private LocalDateTime detectedAt;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @PrePersist
    public void prePersist() {

        detectedAt = LocalDateTime.now();

        if(violationTypes == null) {
            violationTypes = new ArrayList<>();
        }
    }
}