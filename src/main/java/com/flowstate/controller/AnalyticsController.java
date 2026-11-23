package com.flowstate.controller;

import com.flowstate.dto.AnalyticsOverviewResponse;
import com.flowstate.dto.CategoryAnalyticsResponse;
import com.flowstate.dto.DailyProductivityResponse;
import com.flowstate.dto.WeeklyProductivityResponse;
import com.flowstate.service.AnalyticsService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Overall productivity statistics

    @GetMapping("/overview")
    public AnalyticsOverviewResponse getOverview() {
        return analyticsService.getOverview();
    }

    // Daily productivity statistics

    @GetMapping("/daily")
    public List<DailyProductivityResponse> getDailyProductivity(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date cannot be after end date"
            );
        }

        if (endDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be in the future"
            );
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);

        if (days > 89) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date range cannot exceed 90 days"
            );
        }

        return analyticsService.getDailyProductivity(
                startDate, endDate);
    }

    // Weekly productivity statistics

    @GetMapping("/weekly")
    public WeeklyProductivityResponse getWeeklyProductivity(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate requestedDate =
                date != null ? date : LocalDate.now();

        if (requestedDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date cannot be in the future"
            );
        }

        return analyticsService.getWeeklyProductivity(requestedDate);
    }

    // Category-based productivity statistics

    @GetMapping("/categories")
    public List<CategoryAnalyticsResponse> getCategoryAnalytics() {
        return analyticsService.getCategoryAnalytics();
    }
}
