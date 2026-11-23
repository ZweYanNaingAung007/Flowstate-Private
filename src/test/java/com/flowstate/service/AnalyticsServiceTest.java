package com.flowstate.service;

import com.flowstate.dto.*;
import com.flowstate.entity.Category;
import com.flowstate.entity.Task;
import com.flowstate.entity.User;
import com.flowstate.repository.TaskRepository;
import com.flowstate.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private final String email = "user@example.com";
    private User user;

    @BeforeEach
    void setUp() {

        user = mock(User.class);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        email, null, List.of()
                )
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnZeroOverviewWhenNoTasksExist() {

        when(taskRepository.findAllByUser_IdAndDeletedFalse(1L))
                .thenReturn(List.of());

        AnalyticsOverviewResponse result =
                analyticsService.getOverview();

        assertEquals(0, result.totalTasks());
        assertEquals(0, result.completedTasks());
        assertEquals(0, result.pendingTasks());
        assertEquals(0.0, result.completionRate());
    }

    @Test
    void shouldCalculateOverviewCorrectly() {

        Task completed1 = mock(Task.class);
        Task completed2 = mock(Task.class);
        Task pending = mock(Task.class);

        when(completed1.getCompleted()).thenReturn(true);
        when(completed2.getCompleted()).thenReturn(true);
        when(pending.getCompleted()).thenReturn(false);

        when(taskRepository.findAllByUser_IdAndDeletedFalse(1L))
                .thenReturn(List.of(completed1, completed2, pending));

        AnalyticsOverviewResponse result =
                analyticsService.getOverview();

        assertEquals(3, result.totalTasks());
        assertEquals(2, result.completedTasks());
        assertEquals(1, result.pendingTasks());
        assertEquals(66.67, result.completionRate());
    }

    @Test
    void shouldCalculateDailyProductivityCorrectly() {

        LocalDate firstDay = LocalDate.of(2026, 11, 20);
        LocalDate secondDay = LocalDate.of(2026, 11, 21);

        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);
        Task task3 = mock(Task.class);

        when(task1.getCompletedAt())
                .thenReturn(firstDay.atTime(10, 0));

        when(task2.getCompletedAt())
                .thenReturn(firstDay.atTime(15, 0));

        when(task3.getCompletedAt())
                .thenReturn(secondDay.atTime(12, 0));

        when(taskRepository
                .findAllByUser_IdAndDeletedFalseAndCompletedTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(task1, task2, task3));

        List<DailyProductivityResponse> result =
                analyticsService.getDailyProductivity(
                        firstDay, secondDay
                );

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).completedTasks());
        assertEquals(1, result.get(1).completedTasks());
    }

    @Test
    void shouldReturnZeroForDaysWithoutCompletedTasks() {

        LocalDate start = LocalDate.of(2026, 11, 20);
        LocalDate end = LocalDate.of(2026, 11, 22);

        when(taskRepository
                .findAllByUser_IdAndDeletedFalseAndCompletedTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<DailyProductivityResponse> result =
                analyticsService.getDailyProductivity(start, end);

        assertEquals(3, result.size());

        for (DailyProductivityResponse daily : result) {
            assertEquals(0, daily.completedTasks());
        }
    }

    @Test
    void shouldCalculateWeeklyProductivityCorrectly() {

        LocalDate date = LocalDate.of(2026, 11, 24);

        LocalDate monday = LocalDate.of(2026, 11, 23);
        LocalDate sunday = LocalDate.of(2026, 11, 29);

        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);

        when(task1.getCompletedAt())
                .thenReturn(monday.atTime(10, 0));

        when(task2.getCompletedAt())
                .thenReturn(sunday.atTime(15, 0));

        when(taskRepository
                .findAllByUser_IdAndDeletedFalseAndCompletedTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(task1, task2));

        WeeklyProductivityResponse result =
                analyticsService.getWeeklyProductivity(date);

        assertEquals(monday, result.weekStart());
        assertEquals(sunday, result.weekEnd());
        assertEquals(2, result.totalCompletedTasks());
        assertEquals(7, result.dailyBreakdown().size());
    }

    @Test
    void shouldGroupUncategorizedTasksCorrectly() {

        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);

        when(task1.getCategory()).thenReturn(null);
        when(task2.getCategory()).thenReturn(null);

        when(task1.getCompleted()).thenReturn(true);
        when(task2.getCompleted()).thenReturn(false);

        when(taskRepository.findAllByUser_IdAndDeletedFalse(1L))
                .thenReturn(List.of(task1, task2));

        List<CategoryAnalyticsResponse> result =
                analyticsService.getCategoryAnalytics();

        assertEquals(1, result.size());

        CategoryAnalyticsResponse uncategorized = result.get(0);

        assertNull(uncategorized.categoryId());
        assertEquals("Uncategorized", uncategorized.categoryName());
        assertEquals(2, uncategorized.totalTasks());
        assertEquals(1, uncategorized.completedTasks());
        assertEquals(1, uncategorized.pendingTasks());
        assertEquals(50.0, uncategorized.completionRate());
    }

    @Test
    void shouldCalculateCategoryAnalyticsCorrectly() {

        Category category = new Category();
        category.setId(1L);
        category.setName("Programming");

        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);

        when(task1.getCategory()).thenReturn(category);
        when(task2.getCategory()).thenReturn(category);

        when(task1.getCompleted()).thenReturn(true);
        when(task2.getCompleted()).thenReturn(true);

        when(taskRepository.findAllByUser_IdAndDeletedFalse(1L))
                .thenReturn(List.of(task1, task2));

        List<CategoryAnalyticsResponse> result =
                analyticsService.getCategoryAnalytics();

        assertEquals(1, result.size());

        CategoryAnalyticsResponse analytics = result.get(0);

        assertEquals(1L, analytics.categoryId());
        assertEquals("Programming", analytics.categoryName());
        assertEquals(2, analytics.totalTasks());
        assertEquals(2, analytics.completedTasks());
        assertEquals(0, analytics.pendingTasks());
        assertEquals(100.0, analytics.completionRate());
    }

    @Test
    void shouldReturnEmptyCategoryAnalyticsWhenNoTasksExist() {

        when(taskRepository.findAllByUser_IdAndDeletedFalse(1L))
                .thenReturn(List.of());

        List<CategoryAnalyticsResponse> result =
                analyticsService.getCategoryAnalytics();

        assertTrue(result.isEmpty());
    }
}