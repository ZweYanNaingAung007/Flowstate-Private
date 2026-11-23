package com.flowstate.dto;

public record CategoryAnalyticsResponse(
        Long categoryId,
        String categoryName,
        long totalTasks,
        long completedTasks,
        long pendingTasks,
        double completionRate
) {
}