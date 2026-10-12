package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequest(
        @NotBlank(message = "Username is required")
        @Schema(description = "Nombre de usuario registrado", example = "mario")
        String username,

        @NotBlank(message = "Password is required")
        @Schema(description = "Contraseña del usuario", example = "Ejemplo2026!", accessMode = Schema.AccessMode.WRITE_ONLY)
        String password) { }
