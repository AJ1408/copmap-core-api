package in.copmap.alert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private AlertSeverity severity;

    @Column(name = "operation_type", length = 20)
    private String operationType;

    @Column(name = "operation_id")
    private Long operationId;

    @Column(name = "officer_id")
    private Long officerId;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Double lat;

    private Double lng;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private AlertStatus status;

    @Column(name = "acked_by")
    private Long ackedBy;

    @Column(name = "acked_at")
    private Instant ackedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = AlertStatus.OPEN;
        }
    }
}
