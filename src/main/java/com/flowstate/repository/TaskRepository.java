package com.flowstate.repository;

import com.flowstate.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Paginated queries

    Page<Task> findAllByUser_IdAndDeletedFalse(
            Long userId, Pageable pageable);

    Page<Task> findByCompletedAndUser_IdAndDeletedFalse(
            boolean completed, Long userId, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCaseAndUser_IdAndDeletedFalse(
            String title, Long userId, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCaseAndCompletedAndUser_IdAndDeletedFalse(
            String title, boolean completed, Long userId, Pageable pageable);


    // Non-paginated queries

    List<Task> findAllByUser_IdAndDeletedFalse(Long userId);

    List<Task> findByCompletedAndUser_IdAndDeletedFalse(
            boolean completed, Long userId);

    List<Task> findByTitleContainingIgnoreCaseAndUser_IdAndDeletedFalse(
            String title, Long userId);


    // Individual task lookup

    Optional<Task> findByIdAndUser_IdAndDeletedFalse(
            Long id, Long userId);


    // Analytics: completed tasks within a date range

    List<Task> findAllByUser_IdAndDeletedFalseAndCompletedTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
