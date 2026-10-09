package tech.mrodriguez.todo.model.dto;

public record AuthResponse(Long id, String username, String fullName, String token) { }
