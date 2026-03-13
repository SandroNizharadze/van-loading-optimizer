package com.vanopt.service;

import com.vanopt.algorithm.KnapsackSolver;
import com.vanopt.dto.OptimizationRequest;
import com.vanopt.dto.OptimizationResponse;
import com.vanopt.dto.ShipmentDto;
import com.vanopt.exception.ResourceNotFoundException;
import com.vanopt.model.OptimizationResult;
import com.vanopt.model.SelectedShipment;
import com.vanopt.repository.OptimizationResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptimizationService {

    private final KnapsackSolver knapsackSolver;
    private final OptimizationResultRepository repository;

    @Transactional
    public OptimizationResponse optimize(OptimizationRequest request) {
        List<ShipmentDto> shipments = request.getAvailableShipments();

        List<String> names = shipments.stream().map(ShipmentDto::getName).toList();
        List<Double> volumes = shipments.stream().map(ShipmentDto::getVolume).toList();
        List<Double> revenues = shipments.stream().map(ShipmentDto::getRevenue).toList();

        List<Integer> selectedIndices = knapsackSolver.solve(names, volumes, revenues, request.getMaxVolume());

        double totalVolume = selectedIndices.stream().mapToDouble(volumes::get).sum();
        double totalRevenue = selectedIndices.stream().mapToDouble(revenues::get).sum();

        OptimizationResult result = OptimizationResult.builder()
                .maxVolume(request.getMaxVolume())
                .totalVolume(totalVolume)
                .totalRevenue(totalRevenue)
                .build();

        for (int idx : selectedIndices) {
            SelectedShipment shipment = SelectedShipment.builder()
                    .name(names.get(idx))
                    .volume(volumes.get(idx))
                    .revenue(revenues.get(idx))
                    .build();
            result.addShipment(shipment);
        }

        OptimizationResult saved = repository.save(result);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OptimizationResponse getById(UUID requestId) {
        OptimizationResult result = repository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Optimization result not found: " + requestId));
        return toResponse(result);
    }

    @Transactional(readOnly = true)
    public List<OptimizationResponse> getAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private OptimizationResponse toResponse(OptimizationResult result) {
        List<ShipmentDto> shipmentDtos = result.getSelectedShipments().stream()
                .map(s -> ShipmentDto.builder()
                        .name(s.getName())
                        .volume(s.getVolume())
                        .revenue(s.getRevenue())
                        .build())
                .collect(Collectors.toList());

        return OptimizationResponse.builder()
                .requestId(result.getId())
                .selectedShipments(shipmentDtos)
                .totalVolume(result.getTotalVolume())
                .totalRevenue(result.getTotalRevenue())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
