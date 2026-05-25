package vn.icktmeanz.trafficViolation.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.icktmeanz.trafficViolation.constant.SessionStatus;
import vn.icktmeanz.trafficViolation.constant.UploadType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "upload_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_type", nullable = false, length = 50)
    private UploadType uploadType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SessionStatus status = SessionStatus.PROCESSING;

    @Column(name = "video_url", length = 500)
    private String videoUrl; // Lưu link video gốc lưu trên Drive nếu upload_type là VIDEO

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "uploadSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MediaFile> mediaFiles;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
