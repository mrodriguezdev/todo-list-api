package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error de la API")
public record CustomExceptionResponse(
        @Schema(description = "Descripción del error", example = "Task not found")
        String message,
        @Schema(description = "Código de estado HTTP", example = "404")
        int status
) { }
