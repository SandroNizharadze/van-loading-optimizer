package com.vanopt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationResponse {

    private UUID requestId;
    private List<ShipmentDto> selectedShipments;
    private double totalVolume;
    private double totalRevenue;
    private Instant createdAt;
}
