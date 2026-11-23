package com.flowstate.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyProductivityResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        long totalCompletedTasks,
        List<DailyProductivityResponse> dailyBreakdown
) {
}