package vn.icktmeanz.trafficViolation.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.hypersistence.utils.hibernate.type.json.JsonType;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private MediaFile mediaFile;

    // Map trực tiếp với kiểu mảng dữ liệu text[] của PostgreSQL bằng Hypersistence Utils
    @Type(ListArrayType.class)
    @Column(
            name = "violation_types",
            columnDefinition = "text[]"
    )
    private List<String> violationTypes;

    // Lưu chuỗi cấu trúc JSON tọa độ bounding box: {"xmin":10, "ymin":20, ...}
    @Column(name = "bounding_box", columnDefinition = "jsonb", nullable = false)
    @Type(JsonType.class)
    private String boundingBox;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "frame_number")
    private Integer frameNumber; // Lưu vị trí frame nếu bóc tách từ video 

    @Column(name = "is_authority_corrected", nullable = false)
    private boolean isAuthorityCorrected = false;

    @Column(name = "crop_uuid", length = 36)
    private String cropUuid;
}