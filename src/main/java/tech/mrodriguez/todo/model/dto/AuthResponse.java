package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acceso emitido al registrarse o iniciar sesión")
public record AuthResponse(
        @Schema(description = "JWT que se envía en Authorization: Bearer <token>", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.firma")
        String token
) { }
