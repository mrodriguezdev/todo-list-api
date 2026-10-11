package tech.mrodriguez.todo.model.dto;

import jakarta.validation.constraints.Min;

public record TaskFilterRequest (
        @Min(value = 1, message = "Page must be greater than or equal to 1") Integer page,
        @Min(value = 1, message = "Limit must be greater than or equal to 1") Integer limit,
        String title
) {
    public TaskFilterRequest {
        page = page == null ? 1 : page;
        limit = limit == null ? 5 : limit;
    }
}
