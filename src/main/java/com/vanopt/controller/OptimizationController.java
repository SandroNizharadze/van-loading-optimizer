package com.vanopt.controller;

import com.vanopt.dto.OptimizationRequest;
import com.vanopt.dto.OptimizationResponse;
import com.vanopt.service.OptimizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/optimizations")
@RequiredArgsConstructor
public class OptimizationController {

    private final OptimizationService optimizationService;

    @PostMapping
    public ResponseEntity<OptimizationResponse> optimize(
            @Valid @RequestBody OptimizationRequest request) {
        return ResponseEntity.ok(optimizationService.optimize(request));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<OptimizationResponse> getById(@PathVariable UUID requestId) {
        return ResponseEntity.ok(optimizationService.getById(requestId));
    }

    @GetMapping
    public ResponseEntity<List<OptimizationResponse>> getAll() {
        return ResponseEntity.ok(optimizationService.getAll());
    }
}
