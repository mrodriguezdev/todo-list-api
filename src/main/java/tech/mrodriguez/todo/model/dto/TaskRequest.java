package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar una tarea")
public record TaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        @Schema(description = "Título obligatorio, máximo 200 caracteres", example = "Comprar comida")
        String title,
        @Schema(description = "Descripción opcional de la tarea", example = "Completar antes del viernes")
        String description) { }
