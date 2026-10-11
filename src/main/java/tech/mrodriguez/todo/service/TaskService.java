package tech.mrodriguez.todo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.mrodriguez.todo.exception.CustomException;
import tech.mrodriguez.todo.mapper.TaskMapper;
import tech.mrodriguez.todo.model.dto.PageResponse;
import tech.mrodriguez.todo.model.dto.TaskFilterRequest;
import tech.mrodriguez.todo.model.dto.TaskRequest;
import tech.mrodriguez.todo.model.dto.TaskResponse;
import tech.mrodriguez.todo.model.entity.Task;
import tech.mrodriguez.todo.model.entity.User;
import tech.mrodriguez.todo.repository.TaskRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public PageResponse<List<TaskResponse>> list(Long userId, TaskFilterRequest filter) {
        User user = this.userService.findById(userId);
        Pageable pageable = PageRequest.of(filter.page() - 1, filter.limit());
        Page<Task> pageTask = filter.title() == null || filter.title().isBlank()
                ? this.taskRepository.findByUserId(user.getId(), pageable)
                : this.taskRepository.findByUserIdAndTitleLike(user.getId(), "%" + filter.title() + "%", pageable);
        List<TaskResponse> tasks = this.taskMapper.toTaskResponses(pageTask.toList());
        return new PageResponse<>(tasks, filter.page(), filter.limit(), pageTask.getTotalElements());
    }

    public TaskResponse findById(Long userId, Long taskId) {
        User user = this.userService.findById(userId);
        Task task = this.findTaskOwnedByUser(user.getId(), taskId);
        return this.taskMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse create(Long userId, TaskRequest request) {
        User user = this.userService.findById(userId);
        Task task = this.taskMapper.toTask(request, user);
        Task savedTask = this.taskRepository.save(task);
        return this.taskMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse update(Long userId, Long taskId, TaskRequest request) {
        User user = this.userService.findById(userId);
        Task task = this.findTaskOwnedByUser(user.getId(), taskId);
        this.taskMapper.merge(task, request);
        return this.taskMapper.toTaskResponse(task);
    }

    @Transactional
    public void delete(Long userId, Long taskId) {
        User user = this.userService.findById(userId);
        Task task = this.findTaskOwnedByUser(user.getId(), taskId);
        this.taskRepository.delete(task);
    }

    private Task findTaskOwnedByUser(Long userId, Long taskId) {
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!Objects.equals(task.getUser().getId(), userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "You do not have permission to access this task");
        }
        return task;
    }
}
