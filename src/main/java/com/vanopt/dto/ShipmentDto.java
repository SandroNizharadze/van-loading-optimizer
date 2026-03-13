package com.vanopt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDto {

    @NotBlank(message = "Shipment name is required")
    private String name;

    @NotNull(message = "Volume is required")
    @Positive(message = "Volume must be positive")
    private Double volume;

    @NotNull(message = "Revenue is required")
    @Positive(message = "Revenue must be positive")
    private Double revenue;
}
