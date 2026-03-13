package com.vanopt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationRequest {

    @NotNull(message = "maxVolume is required")
    @Positive(message = "maxVolume must be positive")
    private Double maxVolume;

    @NotNull(message = "availableShipments is required")
    @NotEmpty(message = "availableShipments must not be empty")
    private List<@Valid ShipmentDto> availableShipments;
}
