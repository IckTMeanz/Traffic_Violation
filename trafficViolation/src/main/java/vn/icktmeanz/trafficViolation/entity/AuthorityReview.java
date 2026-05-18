package vn.icktmeanz.trafficViolation.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import vn.icktmeanz.trafficViolation.constant.ReviewDecision;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "authority_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorityReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReviewDecision decision;

    @Type(ListArrayType.class)
    @Column(
            name = "original_violations",
            columnDefinition = "text[]"
    )
    private List<String> originalViolations;

    @Type(ListArrayType.class)
    @Column(
            name = "updated_violations",
            columnDefinition = "text[]"
    )
    private List<String> updatedViolations;

    @Column(columnDefinition = "TEXT")
    private String reviewNote;

    private LocalDateTime reviewedAt;

    @OneToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "authority_id")
    private User authority;

    @PrePersist
    public void prePersist() {

        reviewedAt = LocalDateTime.now();
    }
}