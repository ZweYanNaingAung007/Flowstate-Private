package com.flowstate.repository;

import com.flowstate.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// @Repository // Not necessary, JpaRepository already has it
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Method Name Query
    // SELECT * FROM tasks WHERE completed = :completed Dnd deleted = false; (soft deletion)
    List<Task> findByCompletedAndDeletedFalse(boolean completed);
    List<Task> findByTitleContainingIgnoreCaseAndDeletedFalse(String title);
    @Query("SELECT t FROM Task t WHERE t.completed = :completed AND t.deleted = false")
    List<Task> findTasksByCompletionStatus(@Param("completed") boolean completed);

    // New paginated methods
    Page<Task> findByCompletedAndDeletedFalse(boolean completed, Pageable pageable);
    Page<Task> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);
    @Query("SELECT t FROM Task t WHERE t.completed = :completed AND t.deleted = false")
    Page<Task> findTasksByCompletionStatus(@Param("completed") boolean completed,
                                       Pageable pageable);

    @Query("SELECT t " +
            "FROM Task t " +
            "WHERE LOWER(t.title) LIKE " +
            "LOWER(CONCAT('%', :title, '%')) AND t.completed = :completed AND t.deleted = false")
    Page<Task> findByTitleContainingAndCompleted(String title,
                                                 boolean completed,
                                                 Pageable pageable);
    List<Task> findAllByDeletedFalse();

    Page<Task> findAllByDeletedFalse(Pageable pageable);

    Optional<Task> findByIdAndDeletedFalse(Long id);
}
