package tech.mrodriguez.todo.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
}