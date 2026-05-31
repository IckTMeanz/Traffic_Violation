package vn.icktmeanz.trafficViolation.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import vn.icktmeanz.trafficViolation.constant.FeedbackStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Đảm bảo ràng buộc UNIQUE: Mỗi upload_session chỉ được phép gắn với tối đa 01 feedback
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private UploadSession uploadSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FeedbackStatus status = FeedbackStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy; // Cán bộ ROLE_AUTHORITY tiếp nhận xử lý đơn

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}