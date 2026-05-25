package vn.icktmeanz.trafficViolation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "media_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private UploadSession uploadSession;

    @Column(name = "original_url", nullable = false, length = 500)
    private String originalUrl;

    @Column(name = "processed_url", length = 500)
    private String processedUrl;

    @Column(name = "ai_status", nullable = false, length = 50)
    private String aiStatus = "UNCHECKED"; // UNCHECKED, CORRECT, INCORRECT

    @OneToMany(mappedBy = "mediaFile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetectedViolation> detectedViolations;
}