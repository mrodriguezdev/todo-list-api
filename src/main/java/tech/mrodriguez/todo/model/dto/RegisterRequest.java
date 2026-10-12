package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "Datos para registrar un usuario")
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must contain between 3 and 50 characters")
        @Schema(description = "Nombre único de usuario, entre 3 y 50 caracteres", example = "mario")
        String username,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Schema(description = "Nombre, máximo 100 caracteres", example = "Mario")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @Schema(description = "Apellido opcional, máximo 100 caracteres", example = "Rodríguez")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        @Schema(description = "Correo único y válido, máximo 254 caracteres", example = "mario@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        @Schema(description = "Contraseña de al menos 8 caracteres y máximo 72 bytes UTF-8", example = "Ejemplo2026!", accessMode = Schema.AccessMode.WRITE_ONLY)
        String password) {
}
