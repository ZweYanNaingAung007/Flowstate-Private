package com.flowstate.service;

import com.flowstate.dto.TaskRequest;
import com.flowstate.dto.TaskResponse;
import com.flowstate.entity.Task;
import com.flowstate.entity.User;
import com.flowstate.exception.TaskNotFoundException;
import com.flowstate.mapper.TaskMapper;
import com.flowstate.repository.TaskRepository;
import com.flowstate.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
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

    public Page<Task> searchTasksByTitle(
            String title, Pageable pageable) {

        User user = getCurrentUser();

        return taskRepository
                .findByTitleContainingIgnoreCaseAndUser_IdAndDeletedFalse(
                        title, user.getId(), pageable);
    }

    public Page<Task> searchTasksByTitleAndCompletion(
            String title, Boolean completed, Pageable pageable) {

        User user = getCurrentUser();

        return taskRepository
                .findByTitleContainingIgnoreCaseAndCompletedAndUser_IdAndDeletedFalse(
                        title, completed, user.getId(), pageable);
    }

    public Page<Task> getTasksByCompletion(
            Boolean completed, Pageable pageable) {

        User user = getCurrentUser();

        return taskRepository.findByCompletedAndUser_IdAndDeletedFalse(
                completed, user.getId(), pageable);
    }

    public List<Task> getAllTasks() {

        User user = getCurrentUser();

        return taskRepository.findAllByUser_IdAndDeletedFalse(
                user.getId());
    }

    public Page<Task> getAllTasks(Pageable pageable) {

        User user = getCurrentUser();

        return taskRepository.findAllByUser_IdAndDeletedFalse(
                user.getId(), pageable);
    }

    public TaskResponse getTaskById(Long id) {

        User user = getCurrentUser();

        Task task = taskRepository
                .findByIdAndUser_IdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new TaskNotFoundException(id));

        return taskMapper.toResponse(task);
    }

    public TaskResponse createTask(TaskRequest request) {

        User user = getCurrentUser();

        Task task = taskMapper.toEntity(request);

        task.setUser(user);

        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    public TaskResponse updateTask(Long id, TaskRequest updatedTask) {

        User user = getCurrentUser();

        Task task = taskRepository
                .findByIdAndUser_IdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new TaskNotFoundException(id));

        Boolean previousCompletionStatus = task.getCompleted();

        taskMapper.updateEntityFromRequest(task, updatedTask);

        Boolean newCompletionStatus = task.getCompleted();

        if (Boolean.TRUE.equals(newCompletionStatus)
                && !Boolean.TRUE.equals(previousCompletionStatus)) {

            task.setCompletedAt(LocalDateTime.now());

        } else if (!Boolean.TRUE.equals(newCompletionStatus)) {

            task.setCompletedAt(null);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {

        User user = getCurrentUser();

        Task task = taskRepository
                .findByIdAndUser_IdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setDeleted(true);

        taskRepository.save(task);
    }

    public List<TaskResponse> getTasksByCompletionStatus(boolean status) {

        User user = getCurrentUser();

        List<Task> tasks =
                taskRepository.findByCompletedAndUser_IdAndDeletedFalse(
                        status, user.getId());

        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    public Page<TaskResponse> getTasksByCompletionStatus(
            boolean status, Pageable pageable) {

        User user = getCurrentUser();

        Page<Task> tasks =
                taskRepository.findByCompletedAndUser_IdAndDeletedFalse(
                        status, user.getId(), pageable);

        return tasks.map(taskMapper::toResponse);
    }

    public List<TaskResponse> searchTasksByTitle(String title) {

        User user = getCurrentUser();

        List<Task> tasks =
                taskRepository.findByTitleContainingIgnoreCaseAndUser_IdAndDeletedFalse(
                        title, user.getId());

        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }
}
