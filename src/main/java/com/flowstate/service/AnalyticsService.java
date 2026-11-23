package com.flowstate.service;

import com.flowstate.dto.AnalyticsOverviewResponse;
import com.flowstate.dto.CategoryAnalyticsResponse;
import com.flowstate.dto.DailyProductivityResponse;
import com.flowstate.dto.WeeklyProductivityResponse;
import com.flowstate.entity.Task;
import com.flowstate.entity.User;
import com.flowstate.repository.TaskRepository;
import com.flowstate.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public AnalyticsService(
            TaskRepository taskRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            throw new BadCredentialsException("Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new BadCredentialsException("Authenticated user not found"));
    }

    private List<Task> getUserTasks() {

        User user = getCurrentUser();

        return taskRepository.findAllByUser_IdAndDeletedFalse(
                user.getId());
    }

    // Overall productivity statistics

    public AnalyticsOverviewResponse getOverview() {

        List<Task> tasks = getUserTasks();

        long totalTasks = tasks.size();

        long completedTasks = tasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.getCompleted()))
                .count();

        long pendingTasks = totalTasks - completedTasks;

        double completionRate = totalTasks == 0
                ? 0.0
                : (double) completedTasks / totalTasks * 100;

        return new AnalyticsOverviewResponse(
                totalTasks,
                completedTasks,
                pendingTasks,
                Math.round(completionRate * 100.0) / 100.0
        );
    }

    // Daily productivity statistics

    public List<DailyProductivityResponse> getDailyProductivity(
            LocalDate startDate,
            LocalDate endDate) {

        User user = getCurrentUser();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Task> completedTasks =
                taskRepository
                        .findAllByUser_IdAndDeletedFalseAndCompletedTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                                user.getId(), start, end);

        Map<LocalDate, Long> dailyCounts = new HashMap<>();

        for (Task task : completedTasks) {

            LocalDate completionDate = task.getCompletedAt().toLocalDate();

            long currentCount = dailyCounts.getOrDefault(completionDate, 0L);
            dailyCounts.put(completionDate, currentCount + 1L);
        }

        List<DailyProductivityResponse> result = new ArrayList<>();

        for (LocalDate date = startDate;
             !date.isAfter(endDate);
             date = date.plusDays(1)) {

            result.add(new DailyProductivityResponse(
                    date,
                    dailyCounts.getOrDefault(date, 0L)
            ));
        }

        return result;
    }

    // Weekly productivity statistics

    public WeeklyProductivityResponse getWeeklyProductivity(
            LocalDate date) {

        LocalDate weekStart = date.with(
                DayOfWeek.MONDAY);

        LocalDate weekEnd = weekStart.plusDays(6);

        List<DailyProductivityResponse> dailyBreakdown =
                getDailyProductivity(weekStart, weekEnd);

        long totalCompletedTasks = 0;

        for (DailyProductivityResponse daily : dailyBreakdown) {
                totalCompletedTasks += daily.completedTasks();
}

        return new WeeklyProductivityResponse(
                weekStart,
                weekEnd,
                totalCompletedTasks,
                dailyBreakdown
        );
    }

    // Category-based productivity statistics

    public List<CategoryAnalyticsResponse> getCategoryAnalytics() {

        List<Task> tasks = getUserTasks();

        Map<Long, List<Task>> groupedTasks = new HashMap<>();

        for (Task task : tasks) {

            Long categoryId = task.getCategory() == null
                    ? null
                    : task.getCategory().getId();

            groupedTasks.computeIfAbsent(
                    categoryId,
                    key -> new ArrayList<>()
            ).add(task);
        }

        List<CategoryAnalyticsResponse> result = new ArrayList<>();

        for (Map.Entry<Long, List<Task>> entry :
                groupedTasks.entrySet()) {

            Long categoryId = entry.getKey();

            List<Task> categoryTasks = entry.getValue();

            String categoryName = categoryId == null
                    ? "Uncategorized"
                    : categoryTasks.get(0).getCategory().getName();

            long totalTasks = categoryTasks.size();

            long completedTasks = categoryTasks.stream()
                    .filter(task ->
                            Boolean.TRUE.equals(task.getCompleted()))
                    .count();

            long pendingTasks = totalTasks - completedTasks;

            double completionRate = totalTasks == 0
                    ? 0.0
                    : (double) completedTasks / totalTasks * 100;

            result.add(new CategoryAnalyticsResponse(
                    categoryId,
                    categoryName,
                    totalTasks,
                    completedTasks,
                    pendingTasks,
                    Math.round(completionRate * 100.0) / 100.0
            ));
        }

        result.sort((first, second) ->
                first.categoryName().compareTo(second.categoryName()));

        return result;
    }
}