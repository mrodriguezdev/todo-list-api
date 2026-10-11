package tech.mrodriguez.todo.model.dto;

public record PageResponse<T>(T data, Integer page, Integer limit, Long total) {}
