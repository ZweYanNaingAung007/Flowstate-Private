package com.flowstate.dto;

public record AnalyticsOverviewResponse(
        long totalTasks,
        long completedTasks,
        long pendingTasks,
        double completionRate
) {
}