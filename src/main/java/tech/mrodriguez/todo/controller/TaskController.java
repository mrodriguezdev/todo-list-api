package tech.mrodriguez.todo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tech.mrodriguez.todo.model.dto.PageResponse;
import tech.mrodriguez.todo.model.dto.TaskFilterRequest;
import tech.mrodriguez.todo.model.dto.TaskRequest;
import tech.mrodriguez.todo.model.dto.TaskResponse;
import tech.mrodriguez.todo.service.TaskService;

import java.util.List;

@RestController
@RequestMapping(value = "tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponse<List<TaskResponse>>> list(Authentication authentication,
                                                                 @Valid @ModelAttribute TaskFilterRequest filter) {
        return ResponseEntity.ok()
                .body(this.taskService.list(Long.parseLong(authentication.getName()), filter));
    }

    @GetMapping(value = "{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> findById(Authentication authentication, @PathVariable Long taskId) {
        return ResponseEntity.ok()
                .body(this.taskService.findById(Long.parseLong(authentication.getName()), taskId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> create(Authentication authentication, @RequestBody @Valid TaskRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.taskService.create(Long.parseLong(authentication.getName()), request));
    }

    @PutMapping(value = "{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> update(Authentication authentication, @PathVariable Long taskId,
                                               @RequestBody @Valid TaskRequest request) {
        return ResponseEntity
                .ok()
                .body(this.taskService.update(Long.parseLong(authentication.getName()), taskId, request));
    }

    @DeleteMapping(value = "{taskId}")
    public ResponseEntity<Object> delete(Authentication authentication, @PathVariable Long taskId) {
        this.taskService.delete(Long.parseLong(authentication.getName()), taskId);
        return ResponseEntity
                .noContent()
                .build();
    }
}
