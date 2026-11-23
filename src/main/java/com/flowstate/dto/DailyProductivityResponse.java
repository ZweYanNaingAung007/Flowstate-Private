package com.flowstate.dto;

import java.time.LocalDate;

public record DailyProductivityResponse(
        LocalDate date,
        long completedTasks
) {
}