package com.vanopt.algorithm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KnapsackSolver {

    private static final int SCALE = 100;

    /**
     * Solves the 0/1 knapsack problem using bottom-up dynamic programming.
     * Volumes are scaled to integers to avoid floating-point precision issues.
     *
     * @param names    shipment names
     * @param volumes  shipment volumes (dm³)
     * @param revenues shipment revenues
     * @param maxVolume van capacity (dm³)
     * @return indices of selected shipments
     */
    public List<Integer> solve(List<String> names, List<Double> volumes,
                               List<Double> revenues, double maxVolume) {
        int n = names.size();
        if (n == 0) {
            return List.of();
        }

        int capacity = (int) Math.round(maxVolume * SCALE);
        int[] scaledVolumes = new int[n];
        for (int i = 0; i < n; i++) {
            scaledVolumes[i] = (int) Math.round(volumes.get(i) * SCALE);
        }

        double[][] dp = new double[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            int vol = scaledVolumes[i - 1];
            double rev = revenues.get(i - 1);
            for (int w = 0; w <= capacity; w++) {
                dp[i][w] = dp[i - 1][w];
                if (vol <= w && dp[i - 1][w - vol] + rev > dp[i][w]) {
                    dp[i][w] = dp[i - 1][w - vol] + rev;
                }
            }
        }

        List<Integer> selected = new ArrayList<>();
        int w = capacity;
        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                selected.add(i - 1);
                w -= scaledVolumes[i - 1];
            }
        }

        return selected;
    }
}
