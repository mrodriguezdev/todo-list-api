package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos públicos de un usuario")
public class UserResponse {
    @Schema(description = "ID del usuario", example = "1")
    private Long id;
    @Schema(description = "Nombre de usuario", example = "mario")
    private String username;
    @Schema(description = "Nombre completo", example = "Mario Rodríguez")
    private String fullName;
    @Schema(description = "Correo del usuario", example = "mario@example.com")
    private String email;

    public UserResponse() {
    }
}
