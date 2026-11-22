package com.flowstate.service;

import com.flowstate.dto.TaskRequest;
import com.flowstate.dto.TaskResponse;
import com.flowstate.entity.Task;
import com.flowstate.exception.TaskNotFoundException;
import com.flowstate.mapper.TaskMapper;
import com.flowstate.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public Page<Task> searchTasksByTitle(String title, Pageable pageable) {
        return taskRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(title, pageable);
    }

    public Page<Task> searchTasksByTitleAndCompletion(String title,
                                                      Boolean completed,
                                                      Pageable pageable) {
        return taskRepository.findByTitleContainingAndCompleted(title,
                completed, pageable);
    }

    public Page<Task> getTasksByCompletion(Boolean completed, Pageable pageable) {
        return taskRepository.findByCompletedAndDeletedFalse(completed, pageable);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAllByDeletedFalse();
    }

    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAllByDeletedFalse(pageable);
    }

    public TaskResponse getTaskById(Long id) {
        Task retrievedTask = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toResponse(retrievedTask);
    }

    public TaskResponse createTask(TaskRequest task) {
        Task entityTask = taskMapper.toEntity(task);
        Task savedTask = taskRepository.save(entityTask);
        return taskMapper.toResponse(savedTask);
    }

    public TaskResponse updateTask(Long id, TaskRequest updatedTask) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskMapper.updateEntityFromRequest(task, updatedTask);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setDeleted(true);
        taskRepository.save(task);
}

    public List<TaskResponse> getTasksByCompletionStatus(boolean status) {
        final List<Task> completedTasks = taskRepository.findByCompletedAndDeletedFalse(status);
        return completedTasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    public Page<TaskResponse> getTasksByCompletionStatus(boolean status,
                                                         Pageable pageable) {
        final Page<Task> completedTasks = taskRepository.findByCompletedAndDeletedFalse(status, pageable);
        return completedTasks.map(taskMapper::toResponse);
    }

    public List<TaskResponse> searchTasksByTitle(String title) {
        List<Task> tasks = taskRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(title);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    

}
