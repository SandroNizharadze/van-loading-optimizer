package com.vanopt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "selected_shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectedShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optimization_result_id", nullable = false)
    private OptimizationResult optimizationResult;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double volume;

    @Column(nullable = false)
    private double revenue;
}
