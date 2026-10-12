package tech.mrodriguez.todo.mapper;

import org.springframework.stereotype.Component;
import tech.mrodriguez.todo.model.dto.TaskRequest;
import tech.mrodriguez.todo.model.dto.TaskResponse;
import tech.mrodriguez.todo.model.entity.Task;
import tech.mrodriguez.todo.model.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TaskMapper {

    public TaskResponse toTaskResponse(Task task) {
        if (Objects.isNull(task)) return null;
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(task.getId());
        taskResponse.setTitle(task.getTitle());
        taskResponse.setDescription(task.getDescription());
        return taskResponse;
    }

    public List<TaskResponse> toTaskResponses(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return new ArrayList<>();
        return tasks.stream()
                .map(this::toTaskResponse)
                .toList();
    }

    public Task toTask(TaskRequest request, User user) {
        if (Objects.isNull(request)) return null;
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setUser(user);
        return task;
    }

    public void merge(Task task, TaskRequest request) {
        if (Objects.isNull(task) || Objects.isNull(request)) return;
        task.setTitle(request.title());
        task.setDescription(request.description());
    }
}
