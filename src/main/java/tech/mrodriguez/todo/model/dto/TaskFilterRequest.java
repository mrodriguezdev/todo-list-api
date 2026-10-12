package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;

@Schema(description = "Parámetros de paginación y filtro de tareas")
public record TaskFilterRequest (
        @Schema(description = "Número de página, comenzando en 1", example = "1", defaultValue = "1", minimum = "1")
        @Min(value = 1, message = "Page must be greater than or equal to 1")
        Integer page,
        @Schema(description = "Cantidad de tareas por página, mínimo 1", example = "5", defaultValue = "5", minimum = "1")
        @Min(value = 1, message = "Limit must be greater than or equal to 1")
        Integer limit,
        @Schema(description = "Texto opcional que debe estar contenido en el título; vacío no filtra", example = "Comprar")
        String title
) {
    public TaskFilterRequest {
        page = page == null ? 1 : page;
        limit = limit == null ? 5 : limit;
    }
}
