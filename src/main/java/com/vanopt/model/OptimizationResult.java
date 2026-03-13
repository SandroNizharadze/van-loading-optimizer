package com.vanopt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "optimization_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "max_volume", nullable = false)
    private double maxVolume;

    @Column(name = "total_volume", nullable = false)
    private double totalVolume;

    @Column(name = "total_revenue", nullable = false)
    private double totalRevenue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "optimizationResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SelectedShipment> selectedShipments = new ArrayList<>();

    public void addShipment(SelectedShipment shipment) {
        selectedShipments.add(shipment);
        shipment.setOptimizationResult(this);
    }
}
