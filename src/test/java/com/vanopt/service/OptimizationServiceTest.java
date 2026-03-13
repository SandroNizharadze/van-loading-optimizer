package com.vanopt.service;

import com.vanopt.algorithm.KnapsackSolver;
import com.vanopt.dto.OptimizationRequest;
import com.vanopt.dto.OptimizationResponse;
import com.vanopt.dto.ShipmentDto;
import com.vanopt.exception.ResourceNotFoundException;
import com.vanopt.model.OptimizationResult;
import com.vanopt.model.SelectedShipment;
import com.vanopt.repository.OptimizationResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OptimizationServiceTest {

    @Mock
    private KnapsackSolver knapsackSolver;

    @Mock
    private OptimizationResultRepository repository;

    @InjectMocks
    private OptimizationService service;

    @Test
    void optimizeShouldPersistAndReturnResult() {
        OptimizationRequest request = OptimizationRequest.builder()
                .maxVolume(15.0)
                .availableShipments(List.of(
                        ShipmentDto.builder().name("A").volume(5.0).revenue(120.0).build(),
                        ShipmentDto.builder().name("B").volume(10.0).revenue(200.0).build()
                ))
                .build();

        when(knapsackSolver.solve(any(), any(), any(), eq(15.0)))
                .thenReturn(List.of(0, 1));

        when(repository.save(any(OptimizationResult.class))).thenAnswer(invocation -> {
            OptimizationResult r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            r.setCreatedAt(Instant.now());
            return r;
        });

        OptimizationResponse response = service.optimize(request);

        assertThat(response.getRequestId()).isNotNull();
        assertThat(response.getTotalRevenue()).isEqualTo(320.0);
        assertThat(response.getTotalVolume()).isEqualTo(15.0);
        assertThat(response.getSelectedShipments()).hasSize(2);
        verify(repository).save(any(OptimizationResult.class));
    }

    @Test
    void getByIdShouldReturnResponseWhenFound() {
        UUID id = UUID.randomUUID();
        OptimizationResult result = OptimizationResult.builder()
                .id(id)
                .maxVolume(10.0)
                .totalVolume(5.0)
                .totalRevenue(100.0)
                .createdAt(Instant.now())
                .build();
        SelectedShipment shipment = SelectedShipment.builder()
                .name("A").volume(5.0).revenue(100.0)
                .optimizationResult(result)
                .build();
        result.getSelectedShipments().add(shipment);

        when(repository.findById(id)).thenReturn(Optional.of(result));

        OptimizationResponse response = service.getById(id);

        assertThat(response.getRequestId()).isEqualTo(id);
        assertThat(response.getSelectedShipments()).hasSize(1);
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllShouldReturnAllResults() {
        OptimizationResult r1 = OptimizationResult.builder()
                .id(UUID.randomUUID()).maxVolume(10.0).totalVolume(5.0)
                .totalRevenue(100.0).createdAt(Instant.now()).build();
        OptimizationResult r2 = OptimizationResult.builder()
                .id(UUID.randomUUID()).maxVolume(20.0).totalVolume(15.0)
                .totalRevenue(300.0).createdAt(Instant.now()).build();

        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(r1, r2));

        List<OptimizationResponse> responses = service.getAll();

        assertThat(responses).hasSize(2);
        verify(repository).findAllByOrderByCreatedAtDesc();
    }
}
