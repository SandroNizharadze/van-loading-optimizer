package com.vanopt.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnapsackSolverTest {

    private KnapsackSolver solver;

    @BeforeEach
    void setUp() {
        solver = new KnapsackSolver();
    }

    @Test
    void shouldReturnEmptyListWhenNoShipments() {
        List<Integer> result = solver.solve(List.of(), List.of(), List.of(), 10.0);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSelectAllWhenAllFit() {
        List<String> names = List.of("A", "B");
        List<Double> volumes = List.of(3.0, 4.0);
        List<Double> revenues = List.of(100.0, 200.0);

        List<Integer> result = solver.solve(names, volumes, revenues, 10.0);

        assertThat(result).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    void shouldReturnEmptyWhenNoShipmentFits() {
        List<String> names = List.of("A", "B");
        List<Double> volumes = List.of(20.0, 30.0);
        List<Double> revenues = List.of(100.0, 200.0);

        List<Integer> result = solver.solve(names, volumes, revenues, 5.0);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSelectOptimalSubset() {
        List<String> names = List.of("Parcel A", "Parcel B", "Parcel C", "Parcel D");
        List<Double> volumes = List.of(5.0, 10.0, 3.0, 8.0);
        List<Double> revenues = List.of(120.0, 200.0, 80.0, 160.0);

        List<Integer> result = solver.solve(names, volumes, revenues, 15.0);

        double totalRevenue = result.stream().mapToDouble(revenues::get).sum();
        double totalVolume = result.stream().mapToDouble(volumes::get).sum();

        assertThat(totalRevenue).isEqualTo(320.0);
        assertThat(totalVolume).isLessThanOrEqualTo(15.0);
    }

    @Test
    void shouldHandleSingleItemThatFits() {
        List<Integer> result = solver.solve(
                List.of("A"), List.of(5.0), List.of(100.0), 5.0);

        assertThat(result).containsExactly(0);
    }

    @Test
    void shouldHandleItemExactlyAtCapacity() {
        List<String> names = List.of("A", "B");
        List<Double> volumes = List.of(10.0, 10.0);
        List<Double> revenues = List.of(50.0, 100.0);

        List<Integer> result = solver.solve(names, volumes, revenues, 10.0);

        assertThat(result).containsExactly(1);
    }

    @Test
    void shouldHandleDecimalVolumes() {
        List<String> names = List.of("A", "B", "C");
        List<Double> volumes = List.of(2.5, 3.5, 4.0);
        List<Double> revenues = List.of(60.0, 80.0, 90.0);

        List<Integer> result = solver.solve(names, volumes, revenues, 6.0);

        double totalVolume = result.stream().mapToDouble(volumes::get).sum();
        double totalRevenue = result.stream().mapToDouble(revenues::get).sum();

        assertThat(totalVolume).isLessThanOrEqualTo(6.0);
        assertThat(totalRevenue).isEqualTo(140.0);
    }
}
