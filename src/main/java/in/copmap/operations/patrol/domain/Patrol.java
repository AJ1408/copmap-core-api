package in.copmap.operations.patrol.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patrols")
@Getter
@Setter
@NoArgsConstructor
public class Patrol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PatrolType type;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PatrolStatus status;

    @Column(name = "shift_start", nullable = false)
    private Instant shiftStart;

    @Column(name = "shift_end", nullable = false)
    private Instant shiftEnd;

    @Column(name = "geofence_radius_m")
    private Integer geofenceRadiusM;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "close_remarks")
    private String closeRemarks;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "patrol", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq ASC")
    private List<PatrolWaypoint> waypoints = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void addWaypoint(PatrolWaypoint waypoint) {
        waypoints.add(waypoint);
        waypoint.setPatrol(this);
    }
}
