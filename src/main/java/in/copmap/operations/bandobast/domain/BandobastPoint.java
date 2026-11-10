package in.copmap.operations.bandobast.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bandobast_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BandobastPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private BandobastOperation operation;

    @Column(length = 120)
    private String name;

    private Double lat;

    private Double lng;

    @Column(name = "radius_m")
    private Integer radiusM = 100;

    @Column(name = "required_strength")
    private Integer requiredStrength = 1;
}

