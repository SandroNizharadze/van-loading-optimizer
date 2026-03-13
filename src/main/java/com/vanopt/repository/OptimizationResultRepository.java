package com.vanopt.repository;

import com.vanopt.model.OptimizationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OptimizationResultRepository extends JpaRepository<OptimizationResult, UUID> {

    List<OptimizationResult> findAllByOrderByCreatedAtDesc();
}
